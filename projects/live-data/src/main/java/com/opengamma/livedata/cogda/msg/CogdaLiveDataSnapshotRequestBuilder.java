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
public class CogdaLiveDataSnapshotRequestBuilder implements FudgeBuilder<CogdaLiveDataSnapshotRequestMessage> {

  public static MutableFudgeMsg buildMessageStatic(final FudgeSerializer serializer, final CogdaLiveDataSnapshotRequestMessage request) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("MESSAGE_TYPE", CogdaMessageType.SNAPSHOT_REQUEST.name());

    msg.add("correlationId", request.getCorrelationId());
    CogdaLiveDataBuilderUtil.addExternalId(msg, request.getSubscriptionId(), request.getNormalizationScheme());

    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CogdaLiveDataSnapshotRequestMessage object) {
    return buildMessageStatic(serializer, object);
  }

  public static CogdaLiveDataSnapshotRequestMessage buildObjectStatic(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final CogdaLiveDataSnapshotRequestMessage request = new CogdaLiveDataSnapshotRequestMessage();
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
  public CogdaLiveDataSnapshotRequestMessage buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectStatic(deserializer, message);
  }

}
