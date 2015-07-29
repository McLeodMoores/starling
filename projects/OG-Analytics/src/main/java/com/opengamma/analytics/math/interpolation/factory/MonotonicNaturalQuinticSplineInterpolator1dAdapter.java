/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.MonotonicityPreservingQuinticSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator;

/**
 * A named interpolator called "Monotonic Natural Quintic Spline" that wraps {@link MonotonicityPreservingQuinticSplineInterpolator1D}
 * with underlying interpolator {@link NaturalSplineInterpolator}.
 */
public class MonotonicNaturalQuinticSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Natural Quintic Spline";

  /**
   * Creates an instance.
   */
  public MonotonicNaturalQuinticSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingQuinticSplineInterpolator1D(new NaturalSplineInterpolator()), NAME);
  }
}
