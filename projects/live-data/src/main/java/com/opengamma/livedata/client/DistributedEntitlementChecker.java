/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.EntitlementRequest;
import com.opengamma.livedata.msg.EntitlementResponse;
import com.opengamma.livedata.msg.EntitlementResponseMsg;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Checks entitlements against a LiveData server by sending the server a Fudge message.
 */
public class DistributedEntitlementChecker {

  /**
   * If no response from server is received within this period of time, throw exception.
   */
  public static final long TIMEOUT_MS = 5000;

  private static final Logger LOGGER = LoggerFactory.getLogger(DistributedEntitlementChecker.class);
  private final FudgeRequestSender _requestSender;
  private final FudgeContext _fudgeContext;

  public DistributedEntitlementChecker(final FudgeRequestSender requestSender) {
    this(requestSender, OpenGammaFudgeContext.getInstance());
  }

  public DistributedEntitlementChecker(final FudgeRequestSender requestSender, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(requestSender, "Request Sender");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    _requestSender = requestSender;
    _fudgeContext = fudgeContext;
  }

  public Map<LiveDataSpecification, Boolean> isEntitled(final UserPrincipal user,
      final Collection<LiveDataSpecification> specifications) {
    LOGGER.info("Checking entitlements by {} to {}", user, specifications);

    final Map<LiveDataSpecification, Boolean> returnValue = new HashMap<>();

    // User null indicates that we are an internal client that doesn't need permissioning (e.g JMX)
    if (specifications.isEmpty() || user == null) {
      // Nothing to check
      return returnValue;
    }

    final FudgeMsg requestMessage = composeRequestMessage(user, specifications);

    final CountDownLatch latch = new CountDownLatch(1);

    _requestSender.sendRequest(requestMessage, new FudgeMessageReceiver() {

      @Override
      public void messageReceived(final FudgeContext fudgeContext,
          final FudgeMsgEnvelope msgEnvelope) {

        final FudgeMsg msg = msgEnvelope.getMessage();
        final EntitlementResponseMsg responseMsg = EntitlementResponseMsg.fromFudgeMsg(new FudgeDeserializer(fudgeContext), msg);
        for (final EntitlementResponse response : responseMsg.getResponses()) {
          returnValue.put(response.getLiveDataSpecification(), response.getIsEntitled());
        }
        latch.countDown();
      }
    });

    boolean success;
    try {
      success = latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
    } catch (final InterruptedException e) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Interrupted", e);
    }

    if (!success) {
      throw new OpenGammaRuntimeException("Timeout. Waited for entitlement response for " + TIMEOUT_MS + " with no response.");
    }

    LOGGER.info("Got entitlement response {}", returnValue);
    return returnValue;
  }

  public boolean isEntitled(final UserPrincipal user,
      final LiveDataSpecification specification) {
    final Map<LiveDataSpecification, Boolean> entitlements = isEntitled(user, Collections.singleton(specification));
    return entitlements.get(specification);
  }

  private FudgeMsg composeRequestMessage(final UserPrincipal user,
      final Collection<LiveDataSpecification> specifications) {
    final EntitlementRequest request = new EntitlementRequest(user, specifications);
    return request.toFudgeMsg(new FudgeSerializer(_fudgeContext));
  }

}
