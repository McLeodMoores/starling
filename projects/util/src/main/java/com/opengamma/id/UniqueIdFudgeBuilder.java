/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code UniqueId}.
 */
@FudgeBuilderFor(UniqueId.class)
public final class UniqueIdFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<UniqueId> {

  /** Field name. */
  public static final String SCHEME_FIELD_NAME = "Scheme";
  /** Field name. */
  public static final String VALUE_FIELD_NAME = "Value";
  /** Field name. */
  public static final String VERSION_FIELD_NAME = "Version";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final UniqueId object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Converts an {@link UniqueId} to a mutable Fudge message. Returns null if the id is null.
   *
   * @param serializer  the Fudge serializer
   * @param object  the id
   * @return  the message
   */
  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final UniqueId object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Adds an {@link UniqueId} to a message.
   *
   * @param serializer  the Fudge serializer
   * @param object  the id
   * @param msg  the message, not null
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final UniqueId object, final MutableFudgeMsg msg) {
    addToMessage(msg, SCHEME_FIELD_NAME, object.getScheme());
    addToMessage(msg, VALUE_FIELD_NAME, object.getValue());
    addToMessage(msg, VERSION_FIELD_NAME, object.getVersion());
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(msg);
  }

  /**
   * Converts a Fudge message to an {@link UniqueId}. Returns null if the message is null.
   *
   * @param deserializer  the Fudge deserializer
   * @param msg  the message
   * @return  the id
   */
  public static UniqueId fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    return fromFudgeMsg(msg);
  }

  /**
   * Converts a Fudge message to an {@link UniqueId}.
   *
   * @param msg  the message, not null
   * @return  the id
   */
  public static UniqueId fromFudgeMsg(final FudgeMsg msg) {
    final String scheme = msg.getString(SCHEME_FIELD_NAME);
    final String value = msg.getString(VALUE_FIELD_NAME);
    final String version = msg.getString(VERSION_FIELD_NAME);
    return UniqueId.of(scheme, value, version);
  }

}
