/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.rest;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.transport.jms.JmsTemporaryQueueHost;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Base class for a remote consumer which uses a REST+JMS pattern to access streaming results.
 * <p>
 * Provides heartbeating and control of the JMS stream.
 *
 * @param <L>
 *          the type of the listener which will receive the results from the consumer.
 */
public abstract class AbstractRestfulJmsResultConsumer<L> {

  private static final long START_JMS_RESULT_STREAM_TIMEOUT_MILLIS = 10000;
  private static final int HEARTBEAT_RETRIES = 3;

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestfulJmsResultConsumer.class);

  /**
   * The base URI
   */
  private final URI _baseUri;
  /**
   * The REST client
   */
  private final FudgeRestClient _client;
  /**
   * The heartbeat
   */
  private final ScheduledFuture<?> _scheduledHeartbeat;
  /**
   * The JMS connector
   */
  private final JmsConnector _jmsConnector;
  /**
   * The Fudge context
   */
  private final FudgeContext _fudgeContext;
  /**
   * The demand of listeners
   */
  private long _listenerDemand;
  /**
   * The temporary queue host
   */
  private JmsTemporaryQueueHost _queueHost;
  /**
   * The started signal latch
   */
  private CountDownLatch _startedSignalLatch;

  protected AbstractRestfulJmsResultConsumer(final URI baseUri, final FudgeContext fudgeContext, final JmsConnector jmsConnector,
      final ScheduledExecutorService scheduler, final long heartbeatPeriodMillis) {
    _baseUri = baseUri;
    _jmsConnector = jmsConnector;
    _fudgeContext = fudgeContext;
    _client = FudgeRestClient.create();
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        heartbeat();
      }
    };
    _scheduledHeartbeat = scheduler.scheduleAtFixedRate(runnable, heartbeatPeriodMillis, heartbeatPeriodMillis, TimeUnit.MILLISECONDS);
  }

  // -------------------------------------------------------------------------
  protected void onStartResultStream() {
  }

  protected void onEndResultStream() {
  }

  protected abstract void dispatchListenerCall(Function<L, ?> listenerCall);

  // -------------------------------------------------------------------------
  protected URI getBaseUri() {
    return _baseUri;
  }

  protected FudgeRestClient getClient() {
    return _client;
  }

  // -------------------------------------------------------------------------
  /**
   * Externally visible for testing.
   */
  public void heartbeat() {
    final URI heartbeatUri = getUri(getBaseUri(), AbstractRestfulJmsResultPublisher.PATH_HEARTBEAT);
    heartbeat(heartbeatUri);
  }

  /**
   * Externally visible for testing.
   *
   * @param heartbeatUri
   *          the heartbeat URI, not null
   */
  public void heartbeat(final URI heartbeatUri) {
    ArgumentChecker.notNull(heartbeatUri, "heartbeatUri");
    for (int i = 1; i <= HEARTBEAT_RETRIES; i++) {
      try {
        _client.accessFudge(heartbeatUri).post();
        return;
      } catch (final Exception ex) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Heartbeat attempt " + i + " of " + HEARTBEAT_RETRIES + " failed", ex);
        } else {
          LOGGER.warn("Heartbeat attempt " + i + " of " + HEARTBEAT_RETRIES + " failed" + ex.toString());
        }
        if (i == HEARTBEAT_RETRIES) {
          heartbeatFailed(ex);
        }
      }
    }
  }

  /**
   * Called when heartbeating has failed, indicating that the remote resource has been discarded or the connection to the remote host has been lost. This is
   * intended to be overridden to add custom error handling.
   * <p>
   * Externally visible for testing.
   *
   * @param ex
   *          an exception associated with the failed heartbeat, may be null
   */
  public void heartbeatFailed(final Exception ex) {
    LOGGER.error("Heartbeating failed for resource " + getBaseUri() + " failed", ex);
  }

  /**
   * Externally visible for testing
   */
  public void stopHeartbeating() {
    if (!_scheduledHeartbeat.isCancelled()) {
      _scheduledHeartbeat.cancel(true);
    }
  }

  // -------------------------------------------------------------------------
  /**
   * Increments the listener demand, starting the underlying subscription if this is the first listener.
   * <p>
   * If an exception is thrown then the listener state remains unchanged.
   *
   * @throws InterruptedException
   *           if the thread is interrupted while starting the subscription
   * @throws JMSException
   *           if a JMS error occurs while starting the subscription
   */
  protected void incrementListenerDemand() throws InterruptedException, JMSException {
    _listenerDemand++;
    try {
      configureResultListener();
    } catch (JMSException | InterruptedException e) {
      _listenerDemand--;
      throw e;
    }
  }

  /**
   * Decrements the listener demand, stopping the underlying subscription if the last listener is removed.
   * <p>
   * If an exception is thrown then the listener state remains unchanged.
   *
   * @throws InterruptedException
   *           if the thread is interrupted while stopping the subscription
   * @throws JMSException
   *           if a JMS error occurs while stopping the subscription
   */
  protected void decrementListenerDemand() throws InterruptedException, JMSException {
    _listenerDemand--;
    try {
      configureResultListener();
    } catch (JMSException | InterruptedException e) {
      _listenerDemand--;
      throw e;
    }
  }

  /**
   * Configures the underlying subscription if required.
   * <p>
   * If an exception is thrown then the listener state remains unchanged.
   *
   * @throws InterruptedException
   *           if the thread is interrupted while configuring the subscription
   * @throws JMSException
   *           if a JMS error occurs while configuring the subscription
   */
  private void configureResultListener() throws InterruptedException, JMSException {
    if (_listenerDemand == 0) {
      final URI uri = getUri(getBaseUri(), AbstractRestfulJmsResultPublisher.PATH_STOP_JMS_RESULT_STREAM);
      getClient().accessFudge(uri).post();
      closeJms();
      onEndResultStream();
    } else if (_listenerDemand == 1) {
      final String destination = startJms();
      final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
      msg.add(AbstractRestfulJmsResultPublisher.DESTINATION_FIELD, destination);
      final URI uri = getUri(getBaseUri(), AbstractRestfulJmsResultPublisher.PATH_START_JMS_RESULT_STREAM);
      getClient().accessFudge(uri).post(msg);
      try {
        if (!_startedSignalLatch.await(START_JMS_RESULT_STREAM_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
          LOGGER.error("Timed out after {} ms waiting for JMS result stream to be started", START_JMS_RESULT_STREAM_TIMEOUT_MILLIS);
          closeJms();
          throw new OpenGammaRuntimeException("Timed out after " + START_JMS_RESULT_STREAM_TIMEOUT_MILLIS + " ms waiting for JMS result stream to be started");
        }
      } catch (final InterruptedException e) {
        LOGGER.warn("Interrupted while starting JMS result stream");
        closeJms();
        throw e;
      }
      onStartResultStream();
    }
  }

  private String startJms() throws JMSException {
    try {
      _startedSignalLatch = new CountDownLatch(1);
      final ByteArrayFudgeMessageReceiver bafmr = new ByteArrayFudgeMessageReceiver(new FudgeMessageReceiver() {
        @Override
        public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
          LOGGER.debug("Result listener call received");
          Function<L, ?> listenerCall;
          try {
            if (msgEnvelope.getMessage().getNumFields() == 0) {
              // Empty message = started signal, should never occur at other times
              LOGGER.debug("Received started signal");
              _startedSignalLatch.countDown();
              return;
            }
            listenerCall = fudgeContext.fromFudgeMsg(Function.class, msgEnvelope.getMessage());
          } catch (final Throwable t) {
            LOGGER.error("Couldn't parse message {}", t.getMessage());
            LOGGER.warn("Caught exception parsing message", t);
            LOGGER.debug("Couldn't parse message {}", msgEnvelope.getMessage());
            return;
          }
          try {
            dispatchListenerCall(listenerCall);
          } catch (final Throwable t) {
            LOGGER.error("Error dispatching " + listenerCall + " to listener", t);
          }
        }
      }, _fudgeContext);
      _queueHost = new JmsTemporaryQueueHost(_jmsConnector, new JmsByteArrayMessageDispatcher(bafmr));

      LOGGER.info("Set up result JMS subscription to {}", _queueHost.getQueueName());
      return _queueHost.getQueueName();
    } catch (final JMSException e) {
      LOGGER.error("Exception setting up JMS result listener", e);
      closeJms();
      throw e;
    }
  }

  private void closeJms() {
    if (_queueHost != null) {
      try {
        _queueHost.close();
        _startedSignalLatch = null;
      } catch (final Exception e) {
        LOGGER.error("Error closing JMS queue host", e);
      }
    }
  }

  // -------------------------------------------------------------------------
  protected static URI getUri(final URI baseUri, final String path) {
    return UriBuilder.fromUri(baseUri).path(path).build();
  }

}
