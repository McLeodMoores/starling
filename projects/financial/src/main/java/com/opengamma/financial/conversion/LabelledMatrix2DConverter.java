/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.analytics.LabelledMatrix2D;

/**
 *
 */
public class LabelledMatrix2DConverter implements ResultConverter<LabelledMatrix2D<?, ?>> {

  @Override
  public Map<String, Double> convert(final String valueName, final LabelledMatrix2D<?, ?> value) {
    final Map<String, Double> returnValue = new HashMap<>();
    final double[][] data = value.getValues();
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[i].length; j++) {
        // valueName[xKey=xLabel][yKey=yLabel] -> data
        returnValue.put(
            valueName + "[" + value.getXKeys()[j] + "=" + value.getXLabels()[j] + "]" + "[" + value.getYKeys()[i] + "=" + value.getYLabels()[i] + "]",
            data[i][j]);
      }
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return LabelledMatrix2D.class;
  }

}
