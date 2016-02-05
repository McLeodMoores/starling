/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ConstrainedCubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.MonotonicityPreservingCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link MonotonicityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link ConstrainedCubicSplineInterpolator}.
 */
@InterpolationType(name = "Monotonic Constrained Cubic Spline", aliases = {"ConstrainedCubicSplineWithMonotonicity",
    "Constrained Cubic Spline With Monotonicity", "MonotonicConstrainedCubicSpline", "Monotonic Constrained Cubic Spline Interpolator",
    "MonotonicConstrainedCubicSplineInterpolator" })
public class MonotonicConstrainedCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Constrained Cubic Spline";

  /**
   * Creates an instance called "Monotonic Constrained Cubic Spline".
   */
  public MonotonicConstrainedCubicSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new ConstrainedCubicSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public MonotonicConstrainedCubicSplineInterpolator1dAdapter(final String name) {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new ConstrainedCubicSplineInterpolator()), name);
  }
}
