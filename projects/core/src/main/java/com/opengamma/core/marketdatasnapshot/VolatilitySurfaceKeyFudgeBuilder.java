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

import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Encodes / decodes a {@link YieldCurveKey}.
 * <pre>
 *   message {
 *     String target
 *     String name
 *     String instrumentType
 *     String quoteType
 *     String quoteUnits
 *   }
 * </pre>
 */
@FudgeBuilderFor(VolatilitySurfaceKey.class)
public final class VolatilitySurfaceKeyFudgeBuilder implements FudgeBuilder<VolatilitySurfaceKey> {
  /**
   * An instance.
   */
  public static final FudgeBuilder<VolatilitySurfaceKey> INSTANCE = new VolatilitySurfaceKeyFudgeBuilder();
  private static final String QUOTE_UNITS_MSG = "quoteUnits";
  private static final String QUOTE_TYPE_MSG = "quoteType";
  private static final String INSTRUMENT_TYPE_MSG = "instrumentType";
  private static final String NAME_MSG = "name";
  private static final String TARGET_MSG = "target";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilitySurfaceKey object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(TARGET_MSG, object.getTarget().toString());
    msg.add(NAME_MSG, object.getName());
    msg.add(INSTRUMENT_TYPE_MSG, object.getInstrumentType());
    msg.add(QUOTE_TYPE_MSG, object.getQuoteType());
    msg.add(QUOTE_UNITS_MSG, object.getQuoteUnits());
    return msg;
  }

  @Override
  public VolatilitySurfaceKey buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UniqueId targetUid;
    final String target = message.getString(TARGET_MSG);
    if (target == null) {
      //Handle old form of snapshot
      final Currency curr = Currency.of(message.getString("currency"));
      targetUid = curr.getUniqueId();
    } else {
      targetUid = UniqueId.parse(target);
    }
    return VolatilitySurfaceKey.of(targetUid, message.getString(NAME_MSG),
        message.getString(INSTRUMENT_TYPE_MSG), message.getString(QUOTE_TYPE_MSG), message.getString(QUOTE_UNITS_MSG));
  }

}
