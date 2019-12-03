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
public class CogdaLiveDataSnapshotResponseBuilder implements FudgeBuilder<CogdaLiveDataSnapshotResponseMessage> {

  public static MutableFudgeMsg buildMessageStatic(final FudgeSerializer serializer, final CogdaLiveDataSnapshotResponseMessage response) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("MESSAGE_TYPE", CogdaMessageType.SNAPSHOT_RESPONSE.name());
    CogdaLiveDataBuilderUtil.addResponseFields(msg, response);
    msg.add("values", response.getValues());
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CogdaLiveDataSnapshotResponseMessage object) {
    return buildMessageStatic(serializer, object);
  }

  public static CogdaLiveDataSnapshotResponseMessage buildObjectStatic(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final CogdaLiveDataSnapshotResponseMessage response = new CogdaLiveDataSnapshotResponseMessage();
    CogdaLiveDataBuilderUtil.setResponseFields(message, response);
    response.setValues(message.getMessage("values"));
    return response;
  }

  @Override
  public CogdaLiveDataSnapshotResponseMessage buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectStatic(deserializer, message);
  }

}
