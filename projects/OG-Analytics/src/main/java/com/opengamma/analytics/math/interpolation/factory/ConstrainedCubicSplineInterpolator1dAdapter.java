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
   * Creates an instance.
   */
  public ConstrainedCubicSplineInterpolator1dAdapter() {
    super(new ConstrainedCubicSplineInterpolator1D(), "Constrained Cubic Spline");
  }
}
