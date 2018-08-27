/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for PreciseDoubleTimeSeries
 */
@FudgeBuilderFor(PreciseDoubleTimeSeries.class)
public class PreciseDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<PreciseDoubleTimeSeries<?>> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final PreciseDoubleTimeSeries<?> object) {
    return DoubleTimeSeriesFudgeBuilder.INSTANCE.buildMessage(serializer, object);
  }

  @Override
  public PreciseDoubleTimeSeries<?> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return (PreciseDoubleTimeSeries<?>) DoubleTimeSeriesFudgeBuilder.INSTANCE.buildObject(deserializer, message);
  }

}
