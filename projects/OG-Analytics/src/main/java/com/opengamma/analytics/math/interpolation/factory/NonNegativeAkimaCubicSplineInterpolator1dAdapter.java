/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NonnegativityPreservingCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.SemiLocalCubicSplineInterpolator;

/**
 * A named interpolator called "Non-Negative Akima Cubic Spline" that wraps {@link NonnegativityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link SemiLocalCubicSplineInterpolator}.
 */
public class NonNegativeAkimaCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public NonNegativeAkimaCubicSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new SemiLocalCubicSplineInterpolator()), "Non-Negative Akima Cubic Spline");
  }
}
