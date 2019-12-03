/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 *
 */
public class DoubleMatrix2DConverter implements ResultConverter<DoubleMatrix2D> {

  @Override
  public Map<String, Double> convert(final String valueName, final DoubleMatrix2D value) {
    final Map<String, Double> returnValue = new HashMap<>();
    final double[][] data = value.getData();
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[i].length; j++) {
        returnValue.put(valueName + "[" + i + "]" + "[" + j + "]", data[i][j]);
      }
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return DoubleMatrix2D.class;
  }

}
