/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ClampedCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link ClampedCubicSplineInterpolator1D}.
 */
@InterpolationType(name = "Clamped Cubic Spline", aliases = {"ClampedCubicSpline", "Clamped Cubic Spline Interpolator", "ClampedCubicSplineInterpolator" })
public class ClampedCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Clamped Cubic Spline";

  /**
   * Creates an instance called "Clamped Cubic Spline".
   */
  public ClampedCubicSplineInterpolator1dAdapter() {
    super(new ClampedCubicSplineInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public ClampedCubicSplineInterpolator1dAdapter(final String name) {
    super(new ClampedCubicSplineInterpolator1D(), name);
  }
}
