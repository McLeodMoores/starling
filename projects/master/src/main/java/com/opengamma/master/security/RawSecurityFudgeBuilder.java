/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code RawSecurity}.
 */
@FudgeBuilderFor(RawSecurity.class)
public class RawSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<RawSecurity> {

  /** Field name. */
  public static final String RAW_DATA_FIELD_NAME = "rawData";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final RawSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    RawSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final RawSecurity object, final MutableFudgeMsg msg) {
    ManageableSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, RAW_DATA_FIELD_NAME, object.getRawData());
  }

  @Override
  public RawSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final RawSecurity object = new RawSecurity(msg.getString(ManageableSecurityFudgeBuilder.SECURITY_TYPE_FIELD_NAME));
    RawSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final RawSecurity object) {
    ManageableSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setRawData(msg.getValue(byte[].class, RAW_DATA_FIELD_NAME));
  }

}
