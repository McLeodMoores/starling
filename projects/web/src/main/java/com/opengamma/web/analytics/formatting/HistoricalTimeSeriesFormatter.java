/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class HistoricalTimeSeriesFormatter extends AbstractFormatter<HistoricalTimeSeries> {

  private final LocalDateDoubleTimeSeriesFormatter _delegate = new LocalDateDoubleTimeSeriesFormatter();

  /* package */ HistoricalTimeSeriesFormatter() {
    super(HistoricalTimeSeries.class);
  }

  @Override
  public Object formatCell(final HistoricalTimeSeries value, final ValueSpecification valueSpec, final Object inlineKey) {
    return _delegate.formatCell(value.getTimeSeries(), valueSpec, inlineKey);
  }

  @Override
  public Object format(final HistoricalTimeSeries value, final ValueSpecification valueSpec, final Format format, final Object inlineKey) {
    return _delegate.format(value.getTimeSeries(), valueSpec, format, inlineKey);
  }

  @Override
  public DataType getDataType() {
    return DataType.TIME_SERIES;
  }
}
