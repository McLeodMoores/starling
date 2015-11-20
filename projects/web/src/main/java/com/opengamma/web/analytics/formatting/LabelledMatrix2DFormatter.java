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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * Formatter.
 */
@SuppressWarnings("rawtypes")
/* package */ class LabelledMatrix2DFormatter extends AbstractFormatter<LabelledMatrix2D> {

  /* package */ static final String X_LABELS = "xLabels";
  /* package */ static final String Y_LABELS = "yLabels";
  /* package */ static final String MATRIX = "matrix";

  private final DoubleFormatter _doubleFormatter;

  /* package */ LabelledMatrix2DFormatter(final DoubleFormatter doubleFormatter) {
    super(LabelledMatrix2D.class);
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<LabelledMatrix2D>(Format.EXPANDED) {
      @Override
      protected Map<String, Object> formatValue(final LabelledMatrix2D value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(final LabelledMatrix2D value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "Matrix (" + value.getYKeys().length + " x " + value.getXKeys().length + ")";
  }

  private Map<String, Object> formatExpanded(final LabelledMatrix2D value, final ValueSpecification valueSpec) {
    final Map<String, Object> results = Maps.newHashMap();
    final int rowCount = value.getYKeys().length;
    final int columnCount = value.getXKeys().length;
    final String[] xLabels = new String[columnCount];
    final String[] yLabels = new String[rowCount];
    for (int i = 0; i < xLabels.length; i++) {
      xLabels[i] = value.getXLabels()[i].toString();
    }
    results.put(X_LABELS, xLabels);
    for (int i = 0; i < yLabels.length; i++) {
      yLabels[i] = value.getYLabels()[i].toString();
    }
    results.put(Y_LABELS, yLabels);
    final List<List<String>> values = Lists.newArrayListWithCapacity(value.getValues().length);
    for (int y = 0; y < value.getValues().length; y++) {
      final double[] xValues = value.getValues()[y];
      final List<String> rowValues = Lists.newArrayListWithCapacity(xValues.length);
      for (final double xValue : xValues) {
        final String formattedValue = _doubleFormatter.formatCell(xValue, valueSpec, null);
        rowValues.add(formattedValue);
      }
      values.add(rowValues);
    }
    results.put(MATRIX, values);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }
}
