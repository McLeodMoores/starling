/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.MonotonicityPreservingCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.SemiLocalCubicSplineInterpolator;

/**
 * A named interpolator that wraps {@link MonotonicityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link SemiLocalCubicSplineInterpolator}.
 */
@InterpolationType(name = "Monotonic Akima Cubic Spline", aliases = {"AkimaCubicSplineWithMonotonicity",
    "Akima Cubic Spline With Monotonicity", "MonotonicAkimaCubicSpline", "Monotonic Akima Cubic Spline Interpolator",
    "MonotonicAkimaCubicSplineInterpolator" })
public class MonotonicAkimaCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Monotonic Akima Cubic Spline";

  /**
   * Creates an instance called "Monotonic Akima Cubic Spline".
   */
  public MonotonicAkimaCubicSplineInterpolator1dAdapter() {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new SemiLocalCubicSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public MonotonicAkimaCubicSplineInterpolator1dAdapter(final String name) {
    super(new MonotonicityPreservingCubicSplineInterpolator1D(new SemiLocalCubicSplineInterpolator()), name);
  }
}
