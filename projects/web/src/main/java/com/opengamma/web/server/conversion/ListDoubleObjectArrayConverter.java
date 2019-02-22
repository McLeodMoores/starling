/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Converts a list of Double arrays for display.
 */
public class ListDoubleObjectArrayConverter implements ResultConverter<List<Double[]>> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final List<Double[]> value, final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();
    final int rowCount = value.size();
    final int columnCount = value.get(0).length;
    final Map<String, Object> summary = new HashMap<>();
    summary.put("rowCount", rowCount);
    summary.put("colCount", columnCount);
    result.put("summary", summary);
    final double[][] array = new double[rowCount][columnCount];
    for (int i = 0; i < rowCount; i++) {
      for (int j = 0; j < columnCount; j++) {
        array[i][j] = value.get(i)[j];
      }
    }
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
      result.put("matrix", array);
    }
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final List<Double[]> value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final List<Double[]> value) {
    return "Labelled Matrix 2D (" + value.size() + " x " + value.get(0).length + ")";
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_2D";
  }
}
