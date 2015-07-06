/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.MonotonicityPreservingCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator;

/**
 * A named interpolator called "Monotonic Natural Cubic Spline" that wraps {@link MonotonicityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link NaturalSplineInterpolator}.
 */
public class MonotonicNaturalCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public MonotonicNaturalCubicSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new NaturalSplineInterpolator()), "Monotonic Natural Cubic Spline");
  }
}
