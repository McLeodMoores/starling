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
 * Fudge builder for {@code ExternalId}.
 */
@FudgeBuilderFor(ExternalId.class)
public final class ExternalIdFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ExternalId> {

  /** Field name. */
  public static final String SCHEME_FIELD_NAME = "Scheme";
  /** Field name. */
  public static final String VALUE_FIELD_NAME = "Value";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ExternalId object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Converts an {@link ExternalId} to a mutable Fudge message. Returns null if the id is null.
   *
   * @param serializer  the Fudge serializer
   * @param object  the id
   * @return  the message
   */
  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final ExternalId object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Adds an {@link ExternalId} to a message.
   *
   * @param serializer  the Fudge serializer
   * @param object  the id
   * @param msg  the message, not null
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final ExternalId object, final MutableFudgeMsg msg) {
    addToMessage(msg, SCHEME_FIELD_NAME, object.getScheme().getName());
    addToMessage(msg, VALUE_FIELD_NAME, object.getValue());
  }

  //-------------------------------------------------------------------------
  @Override
  public ExternalId buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(msg);
  }

  /**
   * Converts a Fudge message to an {@link ExternalId}. Returns null if the message is null.
   *
   * @param deserializer  the Fudge deserializer
   * @param msg  the message
   * @return  the id
   */
  public static ExternalId fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    return fromFudgeMsg(msg);
  }

  /**
   * Converts a Fudge message to an {@link ExternalId}.
   *
   * @param msg  the message, not null
   * @return  the id
   */
  public static ExternalId fromFudgeMsg(final FudgeMsg msg) {
    final String scheme = msg.getString(SCHEME_FIELD_NAME);
    final String value = msg.getString(VALUE_FIELD_NAME);
    return ExternalId.of(scheme, value);
  }

}
