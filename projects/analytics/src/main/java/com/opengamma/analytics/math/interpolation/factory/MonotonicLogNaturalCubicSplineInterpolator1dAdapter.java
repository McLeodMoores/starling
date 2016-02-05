/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.LogNaturalCubicMonotonicityPreservingInterpolator1D;

/**
 * A named interpolator that wraps {@link LogNaturalCubicMonotonicityPreservingInterpolator1D}.
 */
@InterpolationType(name = "Monotonic Log Natural Cubic Spline", aliases = {"LogNaturalCubicSplineWithMonotonicity",
    "Log Natural Cubic Spline With Monotonicity", "MonotonicLogNaturalCubicSpline", "Monotonic Log Natural Cubic Spline Interpolator",
    "MonotonicLogNaturalCubicSplineInterpolator", "LogNaturalCubicWithMonotonicity" })
public class MonotonicLogNaturalCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Log Natural Cubic Spline";

  /**
   * Creates an instance called "Monotonic Log Natural Cubic Spline".
   */
  public MonotonicLogNaturalCubicSplineInterpolator1dAdapter() {
    super(new LogNaturalCubicMonotonicityPreservingInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public MonotonicLogNaturalCubicSplineInterpolator1dAdapter(final String name) {
    super(new LogNaturalCubicMonotonicityPreservingInterpolator1D(), name);
  }
}
