/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.MonotoneConvexSplineInterpolator1D;

/**
 * A named interpolator called "Monotonic Convex Spline" that wraps {@link MonotoneConvexSplineInterpolator1D}.
 */
@InterpolationType(name = "Monotonic Convex Spline", aliases = {"MonotoneConvexSpline", "Monotone Convex Spline", "MonotonicConvexSpline",
    "Monotonic Convex Spline Interpolator", "MonotonicConvexSplineInterpolator", "MonotoneConvexCubicSpline" })
public class MonotonicConvexSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Convex Spline";

  /**
   * Creates an instance called "Monotonic Convex Spline".
   */
  public MonotonicConvexSplineInterpolator1dAdapter() {
    super(new MonotoneConvexSplineInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public MonotonicConvexSplineInterpolator1dAdapter(final String name) {
    super(new MonotoneConvexSplineInterpolator1D(), name);
  }
}
