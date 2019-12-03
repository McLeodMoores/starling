/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 *
 */
public class CogdaLiveDataUnsubscribeBuilder implements FudgeBuilder<CogdaLiveDataUnsubscribeMessage> {

  public static MutableFudgeMsg buildMessageStatic(final FudgeSerializer serializer, final CogdaLiveDataUnsubscribeMessage request) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("MESSAGE_TYPE", CogdaMessageType.SUBSCRIPTION_REQUEST.name());
    msg.add("correlationId", request.getCorrelationId());
    CogdaLiveDataBuilderUtil.addExternalId(msg, request.getSubscriptionId(), request.getNormalizationScheme());
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CogdaLiveDataUnsubscribeMessage object) {
    return buildMessageStatic(serializer, object);
  }

  public static CogdaLiveDataUnsubscribeMessage buildObjectStatic(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final CogdaLiveDataUnsubscribeMessage request = new CogdaLiveDataUnsubscribeMessage();

    if (message.hasField("correlationId")) {
      request.setCorrelationId(message.getLong("correlationId"));
    } else {
      request.setCorrelationId(-1L);
    }

    request.setSubscriptionId(CogdaLiveDataBuilderUtil.parseExternalId(message));
    request.setNormalizationScheme(message.getString("normalizationScheme"));

    return request;
  }

  @Override
  public CogdaLiveDataUnsubscribeMessage buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectStatic(deserializer, message);
  }

}
