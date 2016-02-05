/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NotAKnotCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link NotAKnotCubicSplineInterpolator1D}.
 */
@InterpolationType(name = "Not-a-Knot Cubic Spline", aliases = {"NotAKnotCubicSpline", "Not A Knot Cubic Spline", "Not-a-Knot Cubic Spline Interpolator",
    "NotAKnotCubicSplineInterpolator" })
public class NotAKnotCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Not-a-Knot Cubic Spline";

  /**
   * Creates an instance called "Not-a-Knot Cubic Spline".
   */
  public NotAKnotCubicSplineInterpolator1dAdapter() {
    super(new NotAKnotCubicSplineInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public NotAKnotCubicSplineInterpolator1dAdapter(final String name) {
    super(new NotAKnotCubicSplineInterpolator1D(), NAME);
  }
}
