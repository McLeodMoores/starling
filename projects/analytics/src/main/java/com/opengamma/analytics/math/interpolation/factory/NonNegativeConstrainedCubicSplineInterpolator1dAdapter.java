/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ConstrainedCubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link NonnegativityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link ConstrainedCubicSplineInterpolator}.
 */
@InterpolationType(name = "Non-Negative Constrained Cubic Spline", aliases = {"ConstrainedCubicSplineWithNonnegativity",
    "Constrained Cubic Spline With Non-Negativity", "NonNegativeConstrainedCubicSpline", "Non-Negative Constrained Cubic Spline Interpolator",
    "NonNegativeConstrainedCubicSplineInterpolator" })
public class NonNegativeConstrainedCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Non-Negative Constrained Cubic Spline";

  /**
   * Creates an instance called "Non-Negative Constrained Cubic Spline".
   */
  public NonNegativeConstrainedCubicSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new ConstrainedCubicSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public NonNegativeConstrainedCubicSplineInterpolator1dAdapter(final String name) {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new ConstrainedCubicSplineInterpolator()), name);
  }
}
