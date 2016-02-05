/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.CubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.MonotonicityPreservingCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link MonotonicityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link CubicSplineInterpolator}.
 */
@InterpolationType(name = "Monotonic Not-a-Knot Cubic Spline", aliases = {"Montonic Not-a-Knot Cubic Spline Interpolator",
    "MonotonicNotaKnotCubicSpline", "MonotonicNotaKnotCubicSplineInterpolator", "NotAKnotCubicSplineWithMonotonicity",
    "ClampedCubicSplineWithMonotonicity" })
public class MonotonicNotAKnotCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Not-a-Knot Cubic Spline";

  /**
   * Creates an instance called "Monotonic Not-a-Knot Cubic Spline".
   */
  public MonotonicNotAKnotCubicSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new CubicSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public MonotonicNotAKnotCubicSplineInterpolator1dAdapter(final String name) {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new CubicSplineInterpolator()), name);
  }
}
