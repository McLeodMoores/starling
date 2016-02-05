/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NonnegativityPreservingCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.SemiLocalCubicSplineInterpolator;

/**
 * A named interpolator that wraps {@link NonnegativityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link SemiLocalCubicSplineInterpolator}.
 */
@InterpolationType(name = "Non-Negative Akima Cubic Spline", aliases = {"AkimaCubicSplineWithNonnegativity",
    "Akima Cubic Spline With Non-Negativity", "NonNegativeAkimaCubicSpline", "Non-Negative Akima Cubic Spline Interpolator",
    "NonNegativeAkimaCubicSplineInterpolator" })
public class NonNegativeAkimaCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Non-Negative Akima Cubic Spline";

  /**
   * Creates an instance called "Non-Negative Akima Cubic Spline".
   */
  public NonNegativeAkimaCubicSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new SemiLocalCubicSplineInterpolator()), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public NonNegativeAkimaCubicSplineInterpolator1dAdapter(final String name) {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new SemiLocalCubicSplineInterpolator()), name);
  }
}
