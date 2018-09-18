/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalId;

/**
 * Utility methods for constructing response and request messages.
 */
public final class CogdaLiveDataBuilderUtil {

  private CogdaLiveDataBuilderUtil() {
  }

  /**
   * Adds an external id and a normalization scheme, if not null to a message.
   *
   * @param msg  the message, not null
   * @param subscriptionId  the subscription id, not null
   * @param normalizationScheme  the normalization scheme, can be null
   */
  public static void addExternalId(final MutableFudgeMsg msg, final ExternalId subscriptionId, final String normalizationScheme) {
    msg.add("subscriptionIdScheme", subscriptionId.getScheme().getName());
    msg.add("subscriptionIdValue", subscriptionId.getValue());
    if (normalizationScheme != null) {
      msg.add("normalizationScheme", normalizationScheme);
    }
  }

  /**
   * Adds the correlation id, result type, subscription id, normalization scheme and user message to a message.
   *
   * @param msg  the message, not null
   * @param response  the response, not null
   */
  public static void addResponseFields(final MutableFudgeMsg msg, final CogdaLiveDataCommandResponseMessage response) {
    msg.add("correlationId", response.getCorrelationId());
    addExternalId(msg, response.getSubscriptionId(), response.getNormalizationScheme());

    msg.add("genericResult", response.getGenericResult().name());
    msg.add("userMessage", response.getUserMessage());

  }

  /**
   * Parses response or request external id.
   *
   * @param msg  the message, not null
   * @return  the response, not null
   */
  public static ExternalId parseExternalId(final FudgeMsg msg) {
    final ExternalId externalId = ExternalId.of(msg.getString("subscriptionIdScheme"), msg.getString("subscriptionIdValue"));
    return externalId;
  }

  /**
   * Sets the values for a response. If there is no response id in the message, the value is set to -1.
   *
   * @param msg  the message, not null
   * @param response  the response, not null
   */
  public static void setResponseFields(final FudgeMsg msg, final CogdaLiveDataCommandResponseMessage response) {
    if (!msg.hasField("correlationId")) {
      response.setCorrelationId(-1L);
    } else {
      response.setCorrelationId(msg.getLong("correlationId"));
    }
    response.setSubscriptionId(parseExternalId(msg));
    response.setNormalizationScheme(msg.getString("normalizationScheme"));
    response.setGenericResult(CogdaCommandResponseResult.valueOf(msg.getString("genericResult")));
    response.setUserMessage(msg.getString("userMessage"));
  }

  /**
   * Builds a response message. If the message is not {@link CogdaLiveDataSubscriptionResponseMessage} or
   * {@link CogdaLiveDataSnapshotResponseMessage}, returns null.
   *
   * @param fudgeContext  the Fudge context, not null
   * @param responseMessage  the response message, not null
   * @return  the message, or null
   */
  public static FudgeMsg buildCommandResponseMessage(final FudgeContext fudgeContext, final CogdaLiveDataCommandResponseMessage responseMessage) {
    if (responseMessage instanceof CogdaLiveDataSubscriptionResponseMessage) {
      return CogdaLiveDataSubscriptionResponseBuilder.buildMessageStatic(new FudgeSerializer(fudgeContext),
          (CogdaLiveDataSubscriptionResponseMessage) responseMessage);
    } else if (responseMessage instanceof CogdaLiveDataSnapshotResponseMessage) {
      return CogdaLiveDataSnapshotResponseBuilder.buildMessageStatic(new FudgeSerializer(fudgeContext), (CogdaLiveDataSnapshotResponseMessage) responseMessage);
    }
    return null;
  }

}
