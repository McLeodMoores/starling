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
 *
 */
public final class CogdaLiveDataBuilderUtil {
  private CogdaLiveDataBuilderUtil() {
  }

  public static void addExternalId(final MutableFudgeMsg msg, final ExternalId subscriptionId, final String normalizationScheme) {
    msg.add("subscriptionIdScheme", subscriptionId.getScheme().getName());
    msg.add("subscriptionIdValue", subscriptionId.getValue());
    if (normalizationScheme != null) {
      msg.add("normalizationScheme", normalizationScheme);
    }
  }

  public static void addResponseFields(final MutableFudgeMsg msg, final CogdaLiveDataCommandResponseMessage response) {
    msg.add("correlationId", response.getCorrelationId());
    addExternalId(msg, response.getSubscriptionId(), response.getNormalizationScheme());

    msg.add("genericResult", response.getGenericResult().name());
    msg.add("userMessage", response.getUserMessage());

  }

  public static ExternalId parseExternalId(final FudgeMsg msg) {
    final ExternalId externalId = ExternalId.of(msg.getString("subscriptionIdScheme"), msg.getString("subscriptionIdValue"));
    return externalId;
  }

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

  public static FudgeMsg buildCommandResponseMessage(final FudgeContext fudgeContext, final CogdaLiveDataCommandResponseMessage responseMessage) {
    if (responseMessage instanceof CogdaLiveDataSubscriptionResponseMessage) {
      return CogdaLiveDataSubscriptionResponseBuilder.buildMessageStatic(new FudgeSerializer(fudgeContext), (CogdaLiveDataSubscriptionResponseMessage) responseMessage);
    } else if (responseMessage instanceof CogdaLiveDataSnapshotResponseMessage) {
      return CogdaLiveDataSnapshotResponseBuilder.buildMessageStatic(new FudgeSerializer(fudgeContext), (CogdaLiveDataSnapshotResponseMessage) responseMessage);
    }
    return null;
  }

}
