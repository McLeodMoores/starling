/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.QuadraticSplineInterpolator1D;

/**
 * A named interpolator called "Quadratic Spline" that wraps {@link QuadraticSplineInterpolator1D}.
 */
public class QuadraticSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public QuadraticSplineInterpolator1dAdapter() {
    super(new QuadraticSplineInterpolator1D(), "Quadratic Spline");
  }
}
