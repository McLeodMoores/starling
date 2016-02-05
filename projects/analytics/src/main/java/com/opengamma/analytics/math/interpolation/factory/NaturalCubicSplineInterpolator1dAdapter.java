/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NaturalCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link NaturalCubicSplineInterpolator1D}.
 */
@InterpolationType(name = "Natural Cubic Spline", aliases = {"NaturalCubicSpline", "Natural Cubic Spline Interpolator", "NaturalCubicSplineInterpolator" })
public class NaturalCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Natural Cubic Spline";

  /**
   * Creates an instance called "Natural Cubic Spline".
   */
  public NaturalCubicSplineInterpolator1dAdapter() {
    super(new NaturalCubicSplineInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public NaturalCubicSplineInterpolator1dAdapter(final String name) {
    super(new NaturalCubicSplineInterpolator1D(), name);
  }
}
