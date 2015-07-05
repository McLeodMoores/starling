/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.volatility.surface;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Fudge builder for {@link ExampleCallPutVolatilitySurfaceInstrumentProvider}.
 */
@FudgeBuilderFor(ExampleCallPutVolatilitySurfaceInstrumentProvider.class)
public class ExampleCallPutVolatilitySurfaceInstrumentProviderBuilder implements FudgeBuilder<ExampleCallPutVolatilitySurfaceInstrumentProvider> {
  /** The prefix field name */
  private static final String PREFIX_FIELD_NAME = "optionPrefix";
  /** The data field name */
  private static final String DATA_FIELD_NAME = "dataField";
  /** The use call above strike field name */
  private static final String USE_CALL_ABOVE_STRIKE = "useCallAboveStrike";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ExampleCallPutVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, ExampleCallPutVolatilitySurfaceInstrumentProvider.class);
    message.add(PREFIX_FIELD_NAME, object.getOptionPrefix());
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    message.add(USE_CALL_ABOVE_STRIKE, object.useCallAboveStrike());
    return message;
  }

  @Override
  public ExampleCallPutVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String optionPrefix = message.getString(PREFIX_FIELD_NAME);
    final String dataFieldName = message.getString(DATA_FIELD_NAME);
    final Double useCallAboveStrike = message.getDouble(USE_CALL_ABOVE_STRIKE);
    return new ExampleCallPutVolatilitySurfaceInstrumentProvider(optionPrefix, dataFieldName, useCallAboveStrike);
  }

}
