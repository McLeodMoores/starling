/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ConstrainedCubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.MonotonicityPreservingCubicSplineInterpolator1D;

/**
 * A named interpolator called "Monotonic Constrained Cubic Spline" that wraps {@link MonotonicityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link ConstrainedCubicSplineInterpolator}.
 */
public class MonotonicConstrainedCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public MonotonicConstrainedCubicSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new ConstrainedCubicSplineInterpolator()), "Monotonic Constrained Cubic Spline");
  }
}
