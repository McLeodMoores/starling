/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Encodes / decodes a {@link CurveKey}.
 * <pre>
 *   message {
 *     String name
 *   }
 * </pre>
 */
@FudgeBuilderFor(CurveKey.class)
public class CurveKeyFudgeBuilder implements FudgeBuilder<CurveKey> {
  /**
   * An instance.
   */
  public static final FudgeBuilder<CurveKey> INSTANCE = new CurveKeyFudgeBuilder();
  private static final String NAME_MSG = "name";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveKey object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(NAME_MSG, object.getName());
    return msg;
  }

  @Override
  public CurveKey buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return CurveKey.of(message.getString(NAME_MSG));
  }

}
