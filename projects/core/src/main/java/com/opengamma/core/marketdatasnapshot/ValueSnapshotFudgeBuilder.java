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
 * Encodes / decodes a {@link ValueSnapshot}.
 * <pre>
 *   message {
 *     optional double marketValue;
 *     optional double overrideValue;
 *   }
 * </pre>
 */
@FudgeBuilderFor(ValueSnapshot.class)
public final class ValueSnapshotFudgeBuilder implements FudgeBuilder<ValueSnapshot> {
  /**
   * An instance.
   */
  public static final FudgeBuilder<ValueSnapshot> INSTANCE = new ValueSnapshotFudgeBuilder();
  private static final String OVERRIDE_VALUE_MSG = "overrideValue";
  private static final String MARKET_VALUE_MSG = "marketValue";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ValueSnapshot object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    if (object.getMarketValue() != null) {
      msg.add(MARKET_VALUE_MSG, object.getMarketValue());
    }
    if (object.getOverrideValue() != null) {
      msg.add(OVERRIDE_VALUE_MSG, object.getOverrideValue());
    }
    return msg;
  }

  @Override
  public ValueSnapshot buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Object marketValue = message.getValue(MARKET_VALUE_MSG);
    final Object overrideValue = message.getValue(OVERRIDE_VALUE_MSG);
    return ValueSnapshot.of(marketValue, overrideValue);
  }

}