/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Class representing the "double-ramp" function.
 */
public class DoubleRampFunction extends Function1D<Double, Double> {
  private final double _x1;
  private final double _x2;
  private final double _y1;
  private final double _y2;

  /**
   * @param x1 The lower edge
   * @param x2 The upper edge, must be greater than x1
   * @param y1 The height below x1
   * @param y2 The height above x2
   */
  public DoubleRampFunction(final double x1, final double x2, final double y1, final double y2) {
    Validate.isTrue(x1 < x2, "x1 must be less than x2");
    _x1 = x1;
    _x2 = x2;
    _y1 = y1;
    _y2 = y2;
  }

  /**
   * @param x
   *          The argument of the function, not null. Must have $x_1 &lt; x &lt; x_2$
   * @return The value of the function
   */
  @Override
  public Double apply(final Double x) {
    Validate.notNull(x, "x");
    if (x < _x1) {
      return _y1;
    }
    if (x > _x2) {
      return _y2;
    }
    return _y1 + (x - _x1) / (_x2 - _x1) * (_y2 - _y1);
  }

}
