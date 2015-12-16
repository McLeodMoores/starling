/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;

/**
 * Fudge builder for {@link QuandlFutureCurveInstrumentProvider}.
 */
@FudgeBuilderFor(QuandlFutureCurveInstrumentProvider.class)
public class QuandlFutureCurveInstrumentProviderBuilder implements FudgeBuilder<QuandlFutureCurveInstrumentProvider> {
  /** The prefix */
  private static final String PREFIX = "prefix";
  /** The data field */
  private static final String DATA_FIELD = "dataField";
  /** The field type */
  private static final String FIELD_TYPE = "fieldType";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final QuandlFutureCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    message.add(PREFIX, object.getFuturePrefix());
    message.add(DATA_FIELD, object.getMarketDataField());
    message.add(FIELD_TYPE, object.getDataFieldType().name());
    return message;
  }

  @Override
  public QuandlFutureCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String futurePrefix = message.getString(PREFIX);
    final String dataField = message.getString(DATA_FIELD);
    final String fieldType = message.getString(FIELD_TYPE);
    return new QuandlFutureCurveInstrumentProvider(futurePrefix, dataField, DataFieldType.valueOf(fieldType));
  }

}
