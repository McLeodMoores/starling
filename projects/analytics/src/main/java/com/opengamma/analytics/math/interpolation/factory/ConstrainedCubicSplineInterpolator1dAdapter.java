/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ConstrainedCubicSplineInterpolator1D;

/**
 * A named interpolator called "Constrained Cubic Spline" that wraps {@link ConstrainedCubicSplineInterpolator1D}.
 */
public class ConstrainedCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Constrained Cubic Spline";

  /**
   * Creates an instance.
   */
  public ConstrainedCubicSplineInterpolator1dAdapter() {
    super(new ConstrainedCubicSplineInterpolator1D(), NAME);
  }
}
