/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.MonotoneConvexSplineInterpolator1D;

/**
 * A named interpolator called "Monotonic Convex Spline" that wraps {@link MonotoneConvexSplineInterpolator1D}.
 */
public class MonotonicConvexSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Convex Spline";

  /**
   * Creates an instance.
   */
  public MonotonicConvexSplineInterpolator1dAdapter() {
    super(new MonotoneConvexSplineInterpolator1D(), NAME);
  }
}
