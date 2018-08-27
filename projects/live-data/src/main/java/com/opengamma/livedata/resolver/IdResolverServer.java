/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.ResolveRequest;
import com.opengamma.livedata.msg.ResolveResponse;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives <code>ResolveRequests</code>, passes them onto a delegate <code>IdResolver</code>,
 * and returns <code>ResolveResponses</code>.
 *
 * @author pietari
 */
public class IdResolverServer implements FudgeRequestReceiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdResolverServer.class);
  private final IdResolver _delegate;

  public IdResolverServer(final IdResolver delegate) {
    ArgumentChecker.notNull(delegate, "Delegate specification resolver");
    _delegate = delegate;
  }

  @Override
  public FudgeMsg requestReceived(final FudgeDeserializer deserializer, final FudgeMsgEnvelope requestEnvelope) {
    final FudgeMsg requestFudgeMsg = requestEnvelope.getMessage();
    final ResolveRequest resolveRequest = ResolveRequest.fromFudgeMsg(deserializer, requestFudgeMsg);
    LOGGER.debug("Received resolve request for {}", resolveRequest.getRequestedSpecification());

    final LiveDataSpecification requestedSpec = resolveRequest.getRequestedSpecification();
    final ExternalId resolvedId = _delegate.resolve(requestedSpec.getIdentifiers());
    final LiveDataSpecification resolvedSpec = new LiveDataSpecification(
        requestedSpec.getNormalizationRuleSetId(),
        resolvedId);

    final ResolveResponse response = new ResolveResponse(resolvedSpec);
    final FudgeMsg responseFudgeMsg = response.toFudgeMsg(new FudgeSerializer(deserializer.getFudgeContext()));
    return responseFudgeMsg;
  }

}
