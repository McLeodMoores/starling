/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.CubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingQuinticSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link NonnegativityPreservingQuinticSplineInterpolator1D}
 * with underlying interpolator {@link CubicSplineInterpolator}.
 */
@InterpolationType(name = "Non-Negative Not-a-Knot Quintic Spline", aliases = {"Non-Negative Not-a-Knot Quintic Spline Interpolator",
    "NonNegativeNotaKnotQuinticSpline", "NonNegativeNotaKnotQuinticSplineInterpolator", "NotAKnotQuinticSplineWithNonnegativity",
    "ClampedQuinticSplineWithNonnegativity" })
public class NonNegativeNotAKnotQuinticSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Non-Negative Not-a-Knot Quintic Spline";

  /**
   * Creates an instance called "Non-Negative Not-a-Knot Quintic Spline".
   */
  public NonNegativeNotAKnotQuinticSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingQuinticSplineInterpolator1D(new CubicSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public NonNegativeNotAKnotQuinticSplineInterpolator1dAdapter(final String name) {
    super(new NonnegativityPreservingQuinticSplineInterpolator1D(new CubicSplineInterpolator()), name);
  }
}
