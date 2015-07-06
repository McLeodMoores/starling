/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ClampedCubicSplineInterpolator1D;

/**
 * A named interpolator called "Clamped Cubic Spline" that wraps {@link ClampedCubicSplineInterpolator1D}.
 */
public class ClampedCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public ClampedCubicSplineInterpolator1dAdapter() {
    super(new ClampedCubicSplineInterpolator1D(), "Clamped Cubic Spline");
  }
}
