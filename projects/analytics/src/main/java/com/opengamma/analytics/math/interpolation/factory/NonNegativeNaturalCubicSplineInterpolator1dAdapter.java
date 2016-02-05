/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link NonnegativityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link NaturalSplineInterpolator}.
 */
@InterpolationType(name = "Non-Negative Natural Cubic Spline", aliases = { "NaturalCubicSplineWithNonnegativity",
        "Natural Cubic Spline With Non-Negativity", "NonNegativeNaturalCubicSpline", "Non-Negative Natural Cubic Spline Interpolator",
        "NonNegativeNaturalCubicSplineInterpolator" })
public class NonNegativeNaturalCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Non-Negative Natural Cubic Spline";

  /**
   * Creates an instance called "Non-Negative Natural Cubic Spline".
   */
  public NonNegativeNaturalCubicSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new NaturalSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public NonNegativeNaturalCubicSplineInterpolator1dAdapter(final String name) {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new NaturalSplineInterpolator()), name);
  }
}
