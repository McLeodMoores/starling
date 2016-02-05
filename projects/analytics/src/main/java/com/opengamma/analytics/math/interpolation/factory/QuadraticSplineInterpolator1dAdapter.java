/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.QuadraticSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link QuadraticSplineInterpolator1D}.
 */
@InterpolationType(name = "Quadratic Spline", aliases = {"QuadraticSpline", "Quadratic Spline Interpolator", "QuadraticSplineInterpolator" })
public class QuadraticSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Quadratic Spline";

  /**
   * Creates an instance called "Quadratic Spline".
   */
  public QuadraticSplineInterpolator1dAdapter() {
    super(new QuadraticSplineInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public QuadraticSplineInterpolator1dAdapter(final String name) {
    super(new QuadraticSplineInterpolator1D(), name);
  }
}
