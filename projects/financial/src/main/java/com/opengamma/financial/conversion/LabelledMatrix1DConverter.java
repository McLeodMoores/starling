/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.analytics.LabelledMatrix1D;

/**
 *
 */
public class LabelledMatrix1DConverter implements ResultConverter<LabelledMatrix1D<?, ?>> {

  @Override
  public Map<String, Double> convert(final String valueName, final LabelledMatrix1D<?, ?> value) {
    final Map<String, Double> returnValue = new HashMap<>();
    final Object[] keys = value.getKeys();
    final double[] values = value.getValues();
    for (int i = 0; i < values.length; i++) {
      final Object k = keys[i];
      final double v = values[i];
      returnValue.put(valueName + "[" + k.toString() + "]", v);
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return LabelledMatrix1D.class;
  }

}
