/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NotAKnotCubicSplineInterpolator1D;

/**
 * A named interpolator called "Not-a-Knot Cubic Spline" that wraps {@link NotAKnotCubicSplineInterpolator1D}.
 */
public class NotAKnotCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Not-a-Knot Cubic Spline";

  /**
   * Creates an instance.
   */
  public NotAKnotCubicSplineInterpolator1dAdapter() {
    super(new NotAKnotCubicSplineInterpolator1D(), NAME);
  }
}
