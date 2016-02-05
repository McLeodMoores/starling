/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.MonotonicityPreservingQuinticSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator;

/**
 * A named interpolator that wraps {@link MonotonicityPreservingQuinticSplineInterpolator1D}
 * with underlying interpolator {@link NaturalSplineInterpolator}.
 */
@InterpolationType(name = "Monotonic Natural Quintic Spline", aliases = {"NaturalQuinticSplineWithMonotonicity",
    "Natural Quintic Spline With Monotonicity", "MonotonicNaturalQuinticSpline", "Monotonic Natural Quintic Spline Interpolator",
    "MonotonicNaturalQuinticSplineInterpolator" })
public class MonotonicNaturalQuinticSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Natural Quintic Spline";

  /**
   * Creates an instance called "Monotonic Natural Quintic Spline".
   */
  public MonotonicNaturalQuinticSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingQuinticSplineInterpolator1D(new NaturalSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public MonotonicNaturalQuinticSplineInterpolator1dAdapter(final String name) {
    super(new MonotonicityPreservingQuinticSplineInterpolator1D(new NaturalSplineInterpolator()), name);
  }
}
