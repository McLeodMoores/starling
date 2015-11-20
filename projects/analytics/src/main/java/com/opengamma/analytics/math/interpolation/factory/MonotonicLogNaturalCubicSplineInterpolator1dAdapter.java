/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.LogNaturalCubicMonotonicityPreservingInterpolator1D;

/**
 * A named interpolator called "Monotonic Log Natural Cubic Spline" that wraps {@link LogNaturalCubicMonotonicityPreservingInterpolator1D}.
 */
public class MonotonicLogNaturalCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Log Natural Cubic Spline";

  /**
   * Creates an instance.
   */
  public MonotonicLogNaturalCubicSplineInterpolator1dAdapter() {
    super(new LogNaturalCubicMonotonicityPreservingInterpolator1D(), NAME);
  }
}
