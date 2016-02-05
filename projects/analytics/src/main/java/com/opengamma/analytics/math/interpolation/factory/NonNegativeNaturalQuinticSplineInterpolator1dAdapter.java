/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingQuinticSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link NonnegativityPreservingQuinticSplineInterpolator1D}
 * with underlying interpolator {@link NaturalSplineInterpolator}.
 */
@InterpolationType(name = "Non-Negative Natural Quintic Spline", aliases = {"NaturalQuinticSplineWithNonnegativity",
    "Natural Quintic Spline With Non-Negativity", "NonNegativeNaturalQuinticSpline", "Non-Negative Natural Quintic Spline Interpolator",
    "NonNegativeNaturalQuinticSplineInterpolator" })
public class NonNegativeNaturalQuinticSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Non-Negative Natural Quintic Spline";

  /**
   * Creates an instance called "Non-Negative Natural Quintic Spline".
   */
  public NonNegativeNaturalQuinticSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingQuinticSplineInterpolator1D(new NaturalSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public NonNegativeNaturalQuinticSplineInterpolator1dAdapter(final String name) {
    super(new NonnegativityPreservingQuinticSplineInterpolator1D(new NaturalSplineInterpolator()), name);
  }
}
