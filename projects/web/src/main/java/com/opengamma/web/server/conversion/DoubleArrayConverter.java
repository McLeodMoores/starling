/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class DoubleArrayConverter implements ResultConverter<double[][]> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final double[][] value, final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();
    final int rowCount = value.length;
    final int columnCount = value[0].length;
    final Map<String, Object> summary = new HashMap<>();
    summary.put("rowCount", rowCount);
    summary.put("colCount", columnCount);
    result.put("summary", summary);

    if (mode == ConversionMode.FULL) {
      final String[] xLabels = new String[columnCount];
      final String[] yLabels = new String[rowCount];
      for (int i = 0; i < xLabels.length; i++) {
        xLabels[i] = "";
      }
      result.put("x", xLabels);
      for (int i = 0; i < yLabels.length; i++) {
        yLabels[i] = "".toString();
      }
      result.put("y", yLabels);
      result.put("matrix", value);
    }
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final double[][] value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final double[][] value) {
    return "Labelled Matrix 2D (" + value.length + " x " + value[0].length + ")";
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_2D";
  }
}
