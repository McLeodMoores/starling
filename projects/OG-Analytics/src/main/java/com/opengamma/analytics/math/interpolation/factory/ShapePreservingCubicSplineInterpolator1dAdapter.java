/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ShapePreservingCubicSplineInterpolator1D;

/**
 * A named interpolator called "C2 Shape-Preserving Cubic Spline" that wraps {@link ShapePreservingCubicSplineInterpolator1D}.
 */
public class ShapePreservingCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public ShapePreservingCubicSplineInterpolator1dAdapter() {
    super(new ShapePreservingCubicSplineInterpolator1D(), "C2 Shape-Preserving Cubic Spline");
  }
}
