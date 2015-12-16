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
import com.opengamma.id.ExternalId;

/**
 * Fudge builder for {@link QuandlFedFundsFutureCurveInstrumentProvider}.
 */
@FudgeBuilderFor(QuandlFedFundsFutureCurveInstrumentProvider.class)
public class QuandlFedFundsFutureCurveInstrumentProviderBuilder implements FudgeBuilder<QuandlFedFundsFutureCurveInstrumentProvider> {
  /** The prefix */
  private static final String PREFIX = "prefix";
  /** The data field */
  private static final String DATA_FIELD = "dataField";
  /** The field type */
  private static final String FIELD_TYPE = "fieldType";
  /** The underlying id */
  private static final String UNDERLYING_ID_FIELD = "underlyingId";
  /** The underlying data field */
  private static final String UNDERLYING_DATA_FIELD = "underlyingDataField";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final QuandlFedFundsFutureCurveInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    message.add(PREFIX, object.getFuturePrefix());
    message.add(DATA_FIELD, object.getMarketDataField());
    message.add(FIELD_TYPE, object.getDataFieldType().name());
    serializer.addToMessageWithClassHeaders(message, UNDERLYING_ID_FIELD, null, object.getUnderlyingId());
    message.add(UNDERLYING_DATA_FIELD, object.getUnderlyingDataField());
    return message;
  }

  @Override
  public QuandlFedFundsFutureCurveInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String futurePrefix = message.getString(PREFIX);
    final String dataField = message.getString(DATA_FIELD);
    final String fieldType = message.getString(FIELD_TYPE);
    final ExternalId underlyingId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(UNDERLYING_ID_FIELD));
    final String underlyingDataField = message.getString(UNDERLYING_DATA_FIELD);
    return new QuandlFedFundsFutureCurveInstrumentProvider(futurePrefix, dataField, DataFieldType.valueOf(fieldType),
        underlyingId, underlyingDataField);
  }

}
