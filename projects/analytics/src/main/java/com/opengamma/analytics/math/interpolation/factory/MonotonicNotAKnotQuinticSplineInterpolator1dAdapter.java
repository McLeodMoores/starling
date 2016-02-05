/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.CubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.MonotonicityPreservingQuinticSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link MonotonicityPreservingQuinticSplineInterpolator1D}
 * with underlying interpolator {@link CubicSplineInterpolator}.
 */
@InterpolationType(name = "Monotonic Not-a-Knot Quintic Spline", aliases = {"Montonic Not-a-Knot Quintic Spline Interpolator",
    "MonotonicNotaKnotQuinticSpline", "MonotonicNotaKnotQuinticSplineInterpolator", "NotAKnotQuinticSplineWithMonotonicity",
    "ClampedQuinticSplineWithMonotonicity" })
public class MonotonicNotAKnotQuinticSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Not-a-Knot Quintic Spline";

  /**
   * Creates an instance called "Monotonic Not-a-Knot Quintic Spline".
   */
  public MonotonicNotAKnotQuinticSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingQuinticSplineInterpolator1D(new CubicSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public MonotonicNotAKnotQuinticSplineInterpolator1dAdapter(final String name) {
    super(new MonotonicityPreservingQuinticSplineInterpolator1D(new CubicSplineInterpolator()), name);
  }
}
