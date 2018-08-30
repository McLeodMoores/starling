/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code ExternalIdBundle}.
 */
@FudgeBuilderFor(ExternalIdBundle.class)
public final class ExternalIdBundleFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ExternalIdBundle> {

  /** Field name. */
  public static final String ID_FIELD_NAME = "ID";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ExternalIdBundle object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Converts an {@link ExternalIdBundle} to a mutable Fudge message. Returns null if the id bundle is null.
   *
   * @param serializer  the Fudge serializer
   * @param object  the id bundle
   * @return  the message
   */
  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final ExternalIdBundle object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Adds an {@link ExternalIdBundle} to a message.
   *
   * @param serializer  the Fudge serializer, not null
   * @param object  the id bundle
   * @param msg  the message, not null
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final ExternalIdBundle object, final MutableFudgeMsg msg) {
    for (final ExternalId externalId : object) {
      addToMessage(msg, ID_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, externalId));
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ExternalIdBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  /**
   * Converts a Fudge message to an {@link ExternalIdBundle}. Returns null if the message is null.
   *
   * @param deserializer  the Fudge deserializer
   * @param msg  the message
   * @return  the id bundle
   */
  public static ExternalIdBundle fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final Set<ExternalId> ids = new HashSet<>();
    for (final FudgeField field : msg.getAllByName(ID_FIELD_NAME)) {
      ids.add(ExternalIdFudgeBuilder.fromFudgeMsg((FudgeMsg) field.getValue()));
    }
    return ExternalIdBundle.of(ids);
  }

}
