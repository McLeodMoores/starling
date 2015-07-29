/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingCubicSplineInterpolator1D;

/**
 * A named interpolator called "Non-Negative Natural Cubic Spline" that wraps {@link NonnegativityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link NaturalSplineInterpolator}.
 */
public class NonNegativeNaturalCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Non-Negative Natural Cubic Spline";

  /**
   * Creates an instance.
   */
  public NonNegativeNaturalCubicSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new NaturalSplineInterpolator()), NAME);
  }
}
