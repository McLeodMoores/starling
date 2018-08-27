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
public class CogdaLiveDataUpdateBuilder implements FudgeBuilder<CogdaLiveDataUpdateMessage> {

  public static MutableFudgeMsg buildMessageStatic(final FudgeSerializer serializer, final CogdaLiveDataUpdateMessage update) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("MESSAGE_TYPE", CogdaMessageType.LIVE_DATA_UPDATE.name());

    CogdaLiveDataBuilderUtil.addExternalId(msg, update.getSubscriptionId(), update.getNormalizationScheme());
    msg.add("values", update.getValues());

    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CogdaLiveDataUpdateMessage object) {
    return buildMessageStatic(serializer, object);
  }

  public static CogdaLiveDataUpdateMessage buildObjectStatic(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final CogdaLiveDataUpdateMessage update = new CogdaLiveDataUpdateMessage();
    update.setSubscriptionId(CogdaLiveDataBuilderUtil.parseExternalId(message));
    update.setNormalizationScheme(message.getString("normalizationScheme"));
    update.setValues(message.getMessage("values"));
    return update;
  }

  @Override
  public CogdaLiveDataUpdateMessage buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectStatic(deserializer, message);
  }

}
