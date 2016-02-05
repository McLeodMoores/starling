/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.CubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link NonnegativityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link CubicSplineInterpolator}.
 */
@InterpolationType(name = "Non-Negative Not-a-Knot Cubic Spline", aliases = {"Non-Negative Not-a-Knot Cubic Spline Interpolator",
    "NonNegativeNotaKnotCubicSpline", "NonNegativeNotaKnotCubicSplineInterpolator", "NotAKnotCubicSplineWithNonnegativity",
    "ClampedCubicSplineWithNonnegativity" })
public class NonNegativeNotAKnotCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Non-Negative Not-a-Knot Cubic Spline";

  /**
   * Creates an instance called "Non-Negative Not-a-Knot Cubic Spline".
   */
  public NonNegativeNotAKnotCubicSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new CubicSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public NonNegativeNotAKnotCubicSplineInterpolator1dAdapter(final String name) {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new CubicSplineInterpolator()), name);
  }
}
