/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

/**
 * A pre-constructed version of the data which should be used for interpolation
 * on an {@link com.opengamma.analytics.math.interpolation.Interpolator1D}.
 *
 */
public interface Interpolator1DDataBundle {

  Double getLowerBoundKey(Double value);

  int getLowerBoundIndex(Double value);

  Double get(Double key);

  Double firstKey();

  Double lastKey();

  Double firstValue();

  Double lastValue();

  Double higherKey(Double key);

  Double higherValue(Double key);

  InterpolationBoundedValues getBoundedValues(Double key);

  boolean containsKey(Double key);

  int size();

  double[] getKeys();

  double[] getValues();

  void setYValueAtIndex(int index, double y);

}
