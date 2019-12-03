/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Fudge message builder for {@code UserPrincipal}.
 */
@FudgeBuilderFor(UserPrincipal.class)
public class UserPrincipalFudgeBuilder implements FudgeBuilder<UserPrincipal> {

  /** Field name. */
  public static final String USER_NAME_FIELD_NAME = "userName";
  /** Field name. */
  public static final String IP_ADDRESS_FIELD_NAME = "ipAddress";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final UserPrincipal object) {
    return UserPrincipalFudgeBuilder.toFudgeMsg(serializer, object);
  }

  /**
   * Creates a new message and serializes a UserPrincipal.
   *
   * @param serializer
   *          the serializer, not null
   * @param object
   *          the UserPrincipal, not null
   * @return a Fudge message
   */
  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final UserPrincipal object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    UserPrincipalFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Adds UserPrincipal fields to the Fudge message.
   *
   * @param serializer
   *          the serializer, not null
   * @param object
   *          the UserPrincipal, not null
   * @param msg
   *          the message with fields added
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final UserPrincipal object, final MutableFudgeMsg msg) {
    if (object.getUserName() != null) {
      msg.add(USER_NAME_FIELD_NAME, null, object.getUserName());
    }
    if (object.getIpAddress() != null) {
      msg.add(IP_ADDRESS_FIELD_NAME, null, object.getIpAddress());
    }
  }

  @Override
  public UserPrincipal buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return UserPrincipalFudgeBuilder.fromFudgeMsg(deserializer, msg);
  }

  /**
   * Converts a Fudge message to a UserPrincipal.
   * 
   * @param deserializer
   *          the deserializer, not null
   * @param msg
   *          the message, not null
   * @return a UserPrincipal
   */
  public static UserPrincipal fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final String userName = msg.getString(USER_NAME_FIELD_NAME);
    final String ipAddress = msg.getString(IP_ADDRESS_FIELD_NAME);
    return new UserPrincipal(userName, ipAddress);
  }

}
