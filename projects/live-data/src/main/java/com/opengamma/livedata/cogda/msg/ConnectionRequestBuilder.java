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
public class ConnectionRequestBuilder implements FudgeBuilder<ConnectionRequestMessage> {

  public static MutableFudgeMsg buildMessageStatic(final FudgeSerializer serializer, final ConnectionRequestMessage request) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("MESSAGE_TYPE", CogdaMessageType.CONNECTION_REQUEST.name());

    msg.add("userName", request.getUserName());
    if (request.getPassword() != null) {
      msg.add("password", request.getPassword());
    }

    msg.add("capabilities", request.getCapabilities());

    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ConnectionRequestMessage object) {
    return buildMessageStatic(serializer, object);
  }

  public static ConnectionRequestMessage buildObjectStatic(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final ConnectionRequestMessage request = new ConnectionRequestMessage();
    request.setUserName(message.getString("userName"));
    final String passwordFromMessage = message.getString("password");
    if (passwordFromMessage != null) {
      request.setPassword(passwordFromMessage);
    }
    request.applyCapabilities(message.getMessage("capabilities"));
    return request;
  }

  @Override
  public ConnectionRequestMessage buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectStatic(deserializer, message);
  }

}
