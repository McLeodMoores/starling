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

import com.opengamma.util.money.Currency;

/**
 * Encodes / decodes a {@link YieldCurveKey}.
 * <pre>
 *   message {
 *     String currency
 *     String name
 *   }
 * </pre>
 */
@FudgeBuilderFor(YieldCurveKey.class)
public class YieldCurveKeyFudgeBuilder implements FudgeBuilder<YieldCurveKey> {
  /**
   * An instance.
   */
  public static final FudgeBuilder<YieldCurveKey> INSTANCE = new YieldCurveKeyFudgeBuilder();
  private static final String CURRENCY_MSG = "ccy";
  private static final String NAME_MSG = "name";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final YieldCurveKey object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(CURRENCY_MSG, object.getCurrency().getCode());
    msg.add(NAME_MSG, object.getName());
    return msg;
  }

  @Override
  public YieldCurveKey buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return YieldCurveKey.of(Currency.of(message.getString(CURRENCY_MSG)), message.getString(NAME_MSG));
  }

}
