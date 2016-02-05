/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ConstrainedCubicSplineInterpolator1D;

/**
 * A named interpolator called "Constrained Cubic Spline" that wraps {@link ConstrainedCubicSplineInterpolator1D}.
 */
@InterpolationType(name = "Constrained Cubic Spline", aliases = {"ConstrainedCubicSpline", "Constrained Cubic Spline Interpolator",
    "ConstrainedCubicSplineInterpolator" })
public class ConstrainedCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Constrained Cubic Spline";

  /**
   * Creates an instance called "Constrained Cubic Spline".
   */
  public ConstrainedCubicSplineInterpolator1dAdapter() {
    super(new ConstrainedCubicSplineInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public ConstrainedCubicSplineInterpolator1dAdapter(final String name) {
    super(new ConstrainedCubicSplineInterpolator1D(), name);
  }
}
