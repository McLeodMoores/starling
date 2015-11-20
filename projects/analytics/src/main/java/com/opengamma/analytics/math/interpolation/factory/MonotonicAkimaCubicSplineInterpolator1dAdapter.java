/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.MonotonicityPreservingCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.SemiLocalCubicSplineInterpolator;

/**
 * A named interpolator called "Monotonic Akima Cubic Spline" that wraps {@link MonotonicityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link SemiLocalCubicSplineInterpolator}.
 */
public class MonotonicAkimaCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Akima Cubic Spline";

  /**
   * Creates an instance.
   */
  public MonotonicAkimaCubicSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new SemiLocalCubicSplineInterpolator()), NAME);
  }
}
