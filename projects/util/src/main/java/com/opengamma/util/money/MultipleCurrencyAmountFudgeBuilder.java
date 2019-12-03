/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code MultipleCurrencyAmount}. Uses the currency code as the Fudge field name and the amount
 * as the value.
 */
@FudgeBuilderFor(MultipleCurrencyAmount.class)
public final class MultipleCurrencyAmountFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<MultipleCurrencyAmount> {
  /** Field name. */
  public static final String CURRENCIES_FIELD_NAME = "currencies";
  /** Field name. */
  public static final String AMOUNTS_FIELD_NAME = "amounts";
  /** Ordinal value. */
  private static final int ORDINAL = 1;

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final MultipleCurrencyAmount object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Converts a multiple currency amount to a message.
   *
   * @param serializer  the serializer, not null
   * @param object  the multiple currency amount, not null
   * @return  the message
   */
  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final MultipleCurrencyAmount object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Converts the multiple currency amount to a message.
   *
   * @param serializer  the serializer, not used
   * @param object  the multiple currency amount, not null
   * @param msg  the message
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final MultipleCurrencyAmount object, final MutableFudgeMsg msg) {
    final CurrencyAmount[] currencyAmounts = object.getCurrencyAmounts();
    for (final CurrencyAmount ca : currencyAmounts) {
      serializer.addToMessage(msg, ca.getCurrency().getCode(), ORDINAL, ca.getAmount());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public MultipleCurrencyAmount buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  /**
   * Converts a message to a multiple currency amount.
   *
   * @param deserializer  the deserializer, not used
   * @param msg  the message
   * @return  the multiple currency amount, or null if the message is null
   */
  public static MultipleCurrencyAmount fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final List<FudgeField> fields = msg.getAllByOrdinal(ORDINAL);
    final CurrencyAmount[] amounts = new CurrencyAmount[fields.size()];
    int i = 0;
    for (final FudgeField field : fields) {
      final Double value = deserializer.fieldValueToObject(Double.class, field);
      amounts[i++] = CurrencyAmount.of(field.getName(), value);
    }
    return MultipleCurrencyAmount.of(amounts);
  }

}
