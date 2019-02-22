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
 * Converts a list of double arrays for display.
 */
@SuppressWarnings("rawtypes")
public class ListDoubleArrayConverter implements ResultConverter<List> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final List value, final ConversionMode mode) {
    if (value.get(0).getClass().equals(double[].class)) {
      final Map<String, Object> result = new HashMap<>();
      final int rowCount = value.size();
      final int columnCount = ((double[]) value.get(0)).length;
      final Map<String, Object> summary = new HashMap<>();
      summary.put("rowCount", rowCount);
      summary.put("colCount", columnCount);
      result.put("summary", summary);
      final double[][] array = new double[columnCount][rowCount];
      for (int i = 0; i < rowCount; i++) {
        for (int j = 0; j < columnCount; j++) {
          array[j][i] = ((double[]) value.get(i))[j];
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
    } else if (value.get(0).getClass().equals(Double[].class)) {
      final Map<String, Object> result = new HashMap<>();
      final int rowCount = value.size();
      final int columnCount = ((Double[]) value.get(0)).length;
      final Map<String, Object> summary = new HashMap<>();
      summary.put("rowCount", rowCount);
      summary.put("colCount", columnCount);
      result.put("summary", summary);
      final double[][] array = new double[columnCount][rowCount];
      for (int i = 0; i < rowCount; i++) {
        for (int j = 0; j < columnCount; j++) {
          array[j][i] = ((Double[]) value.get(i))[j];
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
    throw new UnsupportedOperationException();
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final List value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final List value) {
    return "List";
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_2D";
  }
}
