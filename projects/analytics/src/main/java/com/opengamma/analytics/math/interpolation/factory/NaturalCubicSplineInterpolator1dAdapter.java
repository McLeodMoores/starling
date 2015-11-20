/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NaturalCubicSplineInterpolator1D;

/**
 * A named interpolator called "Natural Cubic Spline" that wraps {@link NaturalCubicSplineInterpolator1D}.
 */
public class NaturalCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Natural Cubic Spline";

  /**
   * Creates an instance.
   */
  public NaturalCubicSplineInterpolator1dAdapter() {
    super(new NaturalCubicSplineInterpolator1D(), NAME);
  }
}
