/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.financial.security.LongShort;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.ExpiryFudgeBuilder;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code FXOptionSecurity}.
 */
@FudgeBuilderFor(FXOptionSecurity.class)
public class FXOptionSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FXOptionSecurity> {

  /** Field name. */
  public static final String PUT_CURRENCY_FIELD_NAME = "putCurrency";
  /** Field name. */
  public static final String CALL_CURRENCY_FIELD_NAME = "callCurrency";
  /** Field name. */
  public static final String PUT_AMOUNT_FIELD_NAME = "putAmount";
  /** Field name. */
  public static final String CALL_AMOUNT_FIELD_NAME = "callAmount";
  /** Field name. */
  public static final String EXPIRY_FIELD_NAME = "expiry";
  /** Field name. */
  public static final String SETTLEMENT_DATE_FIELD_NAME = "settlementDate";
  /** Field name. */
  public static final String IS_LONG_FIELD_NAME = "isLong";
  /** Field name. */
  public static final String EXERCISE_TYPE_FIELD_NAME = "exerciseType";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FXOptionSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FXOptionSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final FXOptionSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, PUT_CURRENCY_FIELD_NAME, object.getPutCurrency());
    addToMessage(msg, CALL_CURRENCY_FIELD_NAME, object.getCallCurrency());
    addToMessage(msg, PUT_AMOUNT_FIELD_NAME, object.getPutAmount());
    addToMessage(msg, CALL_AMOUNT_FIELD_NAME, object.getCallAmount());
    addToMessage(msg, EXPIRY_FIELD_NAME, ExpiryFudgeBuilder.toFudgeMsg(serializer, object.getExpiry()));
    addToMessage(msg, SETTLEMENT_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getSettlementDate()));
    addToMessage(msg, IS_LONG_FIELD_NAME, object.isLong());
    addToMessage(msg, EXERCISE_TYPE_FIELD_NAME, ExerciseTypeFudgeBuilder.toFudgeMsg(serializer, object.getExerciseType()));
  }

  @Override
  public FXOptionSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final FXOptionSecurity object = new FXOptionSecurity();
    FXOptionSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final FXOptionSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setPutCurrency(msg.getValue(Currency.class, PUT_CURRENCY_FIELD_NAME));
    object.setCallCurrency(msg.getValue(Currency.class, CALL_CURRENCY_FIELD_NAME));
    object.setPutAmount(msg.getDouble(PUT_AMOUNT_FIELD_NAME));
    object.setCallAmount(msg.getDouble(CALL_AMOUNT_FIELD_NAME));
    object.setExpiry(ExpiryFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXPIRY_FIELD_NAME)));
    object.setSettlementDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SETTLEMENT_DATE_FIELD_NAME)));
    object.setLongShort(LongShort.ofLong(msg.getBoolean(IS_LONG_FIELD_NAME)));
    object.setExerciseType(ExerciseTypeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(EXERCISE_TYPE_FIELD_NAME)));
  }

}
