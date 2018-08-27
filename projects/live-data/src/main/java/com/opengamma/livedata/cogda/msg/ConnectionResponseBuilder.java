/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 *
 */
public class ConnectionResponseBuilder implements FudgeBuilder<ConnectionResponseMessage> {

  public static MutableFudgeMsg buildMessageStatic(final FudgeSerializer serializer, final ConnectionResponseMessage response) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("MESSAGE_TYPE", CogdaMessageType.CONNECTION_RESPONSE.name());

    msg.add("result", response.getResult().name());

    for (final String availableServer : response.getAvailableServers()) {
      msg.add("availableServer", availableServer);
    }

    msg.add("capabilities", response.getCapabilities());

    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ConnectionResponseMessage object) {
    return buildMessageStatic(serializer, object);
  }

  public static ConnectionResponseMessage buildObjectStatic(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final ConnectionResponseMessage response = new ConnectionResponseMessage();
    response.setResult(ConnectionResult.valueOf(message.getString("result")));

    for (final FudgeField field : message.getAllByName("availableServer")) {
      response.getAvailableServers().add((String) field.getValue());
    }

    response.applyCapabilities(message.getMessage("capabilities"));

    return response;
  }

  @Override
  public ConnectionResponseMessage buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectStatic(deserializer, message);
  }

}
