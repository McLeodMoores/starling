/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.MonotonicityPreservingCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator;

/**
 * A named interpolator that wraps {@link MonotonicityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link NaturalSplineInterpolator}.
 */
@InterpolationType(name = "Monotonic Natural Cubic Spline", aliases = {"NaturalCubicSplineWithMonotonicity",
    "Natural Cubic Spline With Monotonicity", "MonotonicNaturalCubicSpline", "Monotonic Natural Cubic Spline Interpolator",
    "MonotonicNaturalCubicSplineInterpolator" })
public class MonotonicNaturalCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Natural Cubic Spline";

  /**
   * Creates an instance called "Monotonic Natural Cubic Spline".
   */
  public MonotonicNaturalCubicSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new NaturalSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public MonotonicNaturalCubicSplineInterpolator1dAdapter(final String name) {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new NaturalSplineInterpolator()), name);
  }
}
