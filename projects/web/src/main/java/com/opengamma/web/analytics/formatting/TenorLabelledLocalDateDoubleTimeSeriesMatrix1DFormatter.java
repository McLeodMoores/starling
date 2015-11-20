/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.time.Tenor;

/**
 *
 */
/*package*/ class TenorLabelledLocalDateDoubleTimeSeriesMatrix1DFormatter extends AbstractFormatter<TenorLabelledLocalDateDoubleTimeSeriesMatrix1D> {

  private LocalDateDoubleTimeSeriesFormatter _timeSeriesFormatter;

  /*package*/ TenorLabelledLocalDateDoubleTimeSeriesMatrix1DFormatter(final LocalDateDoubleTimeSeriesFormatter timeSeriesFormatter) {
    super(TenorLabelledLocalDateDoubleTimeSeriesMatrix1D.class);
    _timeSeriesFormatter = timeSeriesFormatter;

    addFormatter(new Formatter<TenorLabelledLocalDateDoubleTimeSeriesMatrix1D>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec, inlineKey);
      }
    });

  }

  @Override
  public Object formatCell(final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D value, final ValueSpecification valueSpec, final Object inlineKey) {
    if (inlineKey == null) {
      return "Vector (" + value.size() + ")";
    } else {
      return formatInline(value, valueSpec, Format.CELL, inlineKey);
    }
  }

  private Object formatInline(final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D matrix, final ValueSpecification valueSpec, final Format format, final Object inlineKey) {
    final LocalDateDoubleTimeSeries ts = getTimeSeries(matrix, inlineKey);
    return ts != null ? _timeSeriesFormatter.format(ts, valueSpec, format, inlineKey) : null;
  }

  private Object formatExpanded(final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D matrix, final ValueSpecification valueSpec, final Object inlineKey) {
    final LocalDateDoubleTimeSeries ts = getTimeSeries(matrix, inlineKey);
    return ts != null ? _timeSeriesFormatter.formatExpanded(ts) : null;
  }

  private LocalDateDoubleTimeSeries getTimeSeries(final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D matrix, final Object inlineKey) {
    final Tenor tenorKey = (Tenor) inlineKey;
    for (int i = 0; i < matrix.size(); i++) {
      if (tenorKey.equals(matrix.getKeys()[i])) {
        return matrix.getValues()[i];
      }
    }
    return null;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }

}
