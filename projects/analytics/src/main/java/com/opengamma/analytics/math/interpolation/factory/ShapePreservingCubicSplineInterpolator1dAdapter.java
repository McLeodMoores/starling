/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ShapePreservingCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link ShapePreservingCubicSplineInterpolator1D}.
 */
@InterpolationType(name = "C2 Shape-Preserving Cubic Spline", aliases = {"C2ShapePreservingCubicSpline", "C2 Shape Preserving Cubic Spline",
    "C2 Shape-Preserving Cubic Spline Interpolator", "C2ShapePreservingCubicSplineInterpolator" })
public class ShapePreservingCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "C2 Shape-Preserving Cubic Spline";

  /**
   * Creates an instance called "C2 Shape-Preserving Cubic Spline".
   */
  public ShapePreservingCubicSplineInterpolator1dAdapter() {
    super(new ShapePreservingCubicSplineInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public ShapePreservingCubicSplineInterpolator1dAdapter(final String name) {
    super(new ShapePreservingCubicSplineInterpolator1D(), name);
  }
}
