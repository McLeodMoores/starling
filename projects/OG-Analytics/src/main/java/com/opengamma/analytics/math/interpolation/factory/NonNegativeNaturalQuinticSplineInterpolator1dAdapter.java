/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingQuinticSplineInterpolator1D;

/**
 * A named interpolator called "Non-Negative Natural Quintic Spline" that wraps {@link NonnegativityPreservingQuinticSplineInterpolator1D}
 * with underlying interpolator {@link NaturalSplineInterpolator}.
 */
public class NonNegativeNaturalQuinticSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public NonNegativeNaturalQuinticSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingQuinticSplineInterpolator1D(new NaturalSplineInterpolator()), "Non-Negative Natural Quintic Spline");
  }
}
