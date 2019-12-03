/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.jms.support.JmsUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

/**
 * A JMS LiveData client. This client is implemented using JMS's asynchronous onMessage() notification capability. The client creates
 * 10 JMS sessions by default. New market data subscriptions are assigned to sessions in round-robin fashion.
 */
@PublicAPI
public class JmsLiveDataClient extends DistributedLiveDataClient implements Lifecycle {

  /**
   * How many JMS sessions the client will create by default.
   */
  public static final int DEFAULT_NUM_SESSIONS = 10;

  private static final Logger LOGGER = LoggerFactory.getLogger(JmsLiveDataClient.class);

  private final JmsConnector _jmsConnector;
  private volatile Connection _connection;

  private final Map<String, Runnable> _closeRunnableBySpec =
      new HashMap<>();

  /**
   * A list of JMS sessions created so far. The size of the list will not exceed _maxSessions.
   */
  private final List<Session> _sessions = new ArrayList<>();

  /**
   * How many JMS sessions the client will create. Must be positive.
   */
  private final int _maxSessions;

  /**
   * This needs to be in [0, _maxSessions - 1] at all times. When it reaches _maxSessions, it will be reset back to 0.
   */
  private int _currentSessionIndex; // = 0

  private final AtomicBoolean _running = new AtomicBoolean(false);

  private ExecutorService _executor;

  public JmsLiveDataClient(final FudgeRequestSender subscriptionRequestSender,
      final FudgeRequestSender entitlementRequestSender,
      final JmsConnector jmsConnector) {
    this(subscriptionRequestSender,
        entitlementRequestSender,
        jmsConnector,
        OpenGammaFudgeContext.getInstance(),
        DEFAULT_NUM_SESSIONS);
  }

  public JmsLiveDataClient(final FudgeRequestSender subscriptionRequestSender,
      final FudgeRequestSender entitlementRequestSender,
      final JmsConnector jmsConnector,
      final FudgeContext fudgeContext) {
    this(subscriptionRequestSender,
        entitlementRequestSender,
        jmsConnector,
        fudgeContext,
        DEFAULT_NUM_SESSIONS);
  }

  public JmsLiveDataClient(final FudgeRequestSender subscriptionRequestSender,
      final FudgeRequestSender entitlementRequestSender,
      final JmsConnector jmsConnector,
      final FudgeContext fudgeContext,
      final int maxSessions) {
    super(subscriptionRequestSender, entitlementRequestSender, fudgeContext);
    ArgumentChecker.notNull(jmsConnector, "jmsConnector");
    _jmsConnector = jmsConnector;

    if (maxSessions <= 0) {
      throw new IllegalArgumentException("Max sessions must be positive");
    }
    _maxSessions = maxSessions;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS connector.
   *
   * @return the JMS connector
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  @Override
  public synchronized void startReceivingTicks(final Collection<String> tickDistributionSpecifications) {
    super.startReceivingTicks(tickDistributionSpecifications);

    LOGGER.info("Starting listening to tick distribution specifications {}", tickDistributionSpecifications);

    final List<List<String>> specsBySessionIndex = new ArrayList<>(_maxSessions);

    for (final String tickDistributionSpecification : tickDistributionSpecifications) {
      if (_closeRunnableBySpec.containsKey(tickDistributionSpecification)) {
        // Already receiving for that tick. Ignore it.
        continue;
      }

      while (specsBySessionIndex.size() <= _currentSessionIndex) {
        specsBySessionIndex.add(new ArrayList<String>());
      }

      try {
        Session session;
        if (_sessions.size() <= _currentSessionIndex) {
          session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          _sessions.add(session);
        } else {
          session = _sessions.get(_currentSessionIndex);
        }
      } catch (final JMSException e) {
        throw new OpenGammaRuntimeException("Failed to create subscription to JMS topic " + tickDistributionSpecification, e);
      }

      specsBySessionIndex.get(_currentSessionIndex).add(tickDistributionSpecification);

      // round-robin logic here
      _currentSessionIndex++;
      if (_currentSessionIndex >= _maxSessions) {
        _currentSessionIndex = 0;
      }
    }
    final List<Future<Map<String, Runnable>>> futures = new ArrayList<>();
    for (int i = 0; i < specsBySessionIndex.size(); i++) {
      final Callable<Map<String, Runnable>> c = getStartReceivingCallable(specsBySessionIndex.get(i), i);
      futures.add(_executor.submit(c));
    }
    for (final Future<Map<String, Runnable>> future : futures) {
      try {
        final Map<String, Runnable> consumers = future.get();
        _closeRunnableBySpec.putAll(consumers);
      } catch (final ExecutionException ex) {
        throw new OpenGammaRuntimeException("Failed to start receiving ticks", ex);
      } catch (final InterruptedException ex) {
        throw new OpenGammaRuntimeException("Failed to start receiving ticks", ex);
      }
    }
  }

  private Callable<Map<String, Runnable>> getStartReceivingCallable(final List<String> specs, final int sessionIndex) {
    return new Callable<Map<String, Runnable>>() {
      @Override
      public Map<String, Runnable> call() {
        final Session session = _sessions.get(sessionIndex);

        final ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(JmsLiveDataClient.this, getFudgeContext());
        final JmsByteArrayMessageDispatcher jmsDispatcher = new JmsByteArrayMessageDispatcher(fudgeReceiver);

        return startReceivingTicks(specs, session, jmsDispatcher);
      }
    };
  }

  protected Map<String, Runnable> startReceivingTicks(final List<String> specs, final Session session,
      final JmsByteArrayMessageDispatcher jmsDispatcher) {
    final Map<String, Runnable> ret = new HashMap<>();
    for (final String tickDistributionSpecification : specs) {
      try {
        final Topic topic = session.createTopic(tickDistributionSpecification);

        final MessageConsumer messageConsumer = session.createConsumer(topic);
        messageConsumer.setMessageListener(jmsDispatcher);
        ret.put(tickDistributionSpecification, getCloseAction(messageConsumer));
      } catch (final JMSException e) {
        throw new OpenGammaRuntimeException("Failed to create subscription to JMS topic " + tickDistributionSpecification, e);
      }
    }
    return ret;
  }

  private static Runnable getCloseAction(final MessageConsumer messageConsumer) {
    return new Runnable() {
      @Override
      public void run() {
        JmsUtils.closeMessageConsumer(messageConsumer);
      }
    };
  }

  @Override
  public synchronized void stopReceivingTicks(final String tickDistributionSpecification) {
    final Runnable close = _closeRunnableBySpec.get(tickDistributionSpecification);
    if (close == null) {
      return;
    }

    close.run();

    _closeRunnableBySpec.remove(tickDistributionSpecification);
  }

  @Override
  public boolean isRunning() {
    return _running.get();
  }

  @Override
  public synchronized void start() {
    try {
      _connection = _jmsConnector.getConnectionFactory().createConnection();
      _connection.start();
    } catch (final JMSException e) {
      throw new OpenGammaRuntimeException("Failed to create JMS connection", e);
    }
    _executor = NamedThreadPoolFactory.newCachedThreadPool(toString(), true);
    _running.set(true);
  }

  @Override
  public synchronized void close() {
    try {
      for (final Session session : _sessions) {
        LOGGER.info("Shutting down session {}", session);
        session.close();
      }
      _sessions.clear();
      for (final Runnable close : _closeRunnableBySpec.values()) {
        close.run(); // [PLAT-1809]  Must close these as well
      }
      _closeRunnableBySpec.clear();

      if (_connection != null) {
        _connection.close();
        _connection = null;
      }
    } catch (final JMSException e) {
      throw new OpenGammaRuntimeException("Failed to close JMS connection", e);
    }
    _executor.shutdown();
    super.close();
  }

  @Override
  public synchronized void stop() {
    close();
    _running.set(false);
  }

}
