/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 *
 */
/* package */ class LocalDateDoubleTimeSeriesFormatter extends AbstractFormatter<LocalDateDoubleTimeSeries> {

  /** The number of milliseconds per day. */
  private static final long MILLIS_PER_DAY = 86400L * 1000;

  /* package */ LocalDateDoubleTimeSeriesFormatter() {
    super(LocalDateDoubleTimeSeries.class);
    addFormatter(new Formatter<LocalDateDoubleTimeSeries>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final LocalDateDoubleTimeSeries value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value);
      }
    });

  }

  @Override
  public String formatCell(final LocalDateDoubleTimeSeries timeSeries, final ValueSpecification valueSpec, final Object inlineKey) {
    String text = "Time-series ";
    text += timeSeries.isEmpty() ? "(empty)" : "(" + timeSeries.getEarliestTime() + " to " + timeSeries.getLatestTime() + ")";
    return text;
  }

  public Map<String, Object> formatExpanded(final LocalDateDoubleTimeSeries value) {
    final List<Object[]> data = Lists.newArrayListWithCapacity(value.size());
    for (final LocalDateDoubleEntryIterator it = value.iterator(); it.hasNext(); ) {
      final LocalDate date = it.nextTime();
      final long epochMillis = date.toEpochDay() * MILLIS_PER_DAY;
      data.add(new Object[]{epochMillis, it.currentValue()});
    }
    final Map<String, String> templateData = ImmutableMap.of("data_field", "Historical Time Series",
                                                       "observation_time", "Historical Time Series");
    final Map<String, Object> timeSeries = ImmutableMap.of("fieldLabels", new String[]{"Time", "Value"},
                                                     "data", data);
    return ImmutableMap.<String, Object>of("template_data", templateData, "timeseries", timeSeries);
  }

  @Override
  public DataType getDataType() {
    return DataType.TIME_SERIES;
  }
}
