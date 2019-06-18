/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Converter for {@link HistoricalTimeSeries} results.
 */
public class HistoricalTimeSeriesConverter implements ResultConverter<HistoricalTimeSeries> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final HistoricalTimeSeries value,
      final ConversionMode mode) {
    return LocalDateDoubleTimeSeriesConverter.convertForDisplayImpl(context, valueSpec, value.getTimeSeries(), mode);
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final HistoricalTimeSeries value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final HistoricalTimeSeries value) {
    return value.toString();
  }

  @Override
  public String getFormatterName() {
    return "TIME_SERIES";
  }

}
