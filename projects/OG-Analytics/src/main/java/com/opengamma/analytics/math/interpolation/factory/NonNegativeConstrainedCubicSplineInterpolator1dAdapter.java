/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ConstrainedCubicSplineInterpolator;
import com.opengamma.analytics.math.interpolation.NonnegativityPreservingCubicSplineInterpolator1D;

/**
 * A named interpolator called "Non-Negative Constrained Cubic Spline" that wraps {@link NonnegativityPreservingCubicSplineInterpolator1D}
 * with underlying interpolator {@link ConstrainedCubicSplineInterpolator}.
 */
public class NonNegativeConstrainedCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public NonNegativeConstrainedCubicSplineInterpolator1dAdapter() {
    super(new NonnegativityPreservingCubicSplineInterpolator1D(new ConstrainedCubicSplineInterpolator()), "Non-Negative Constrained Cubic Spline");
  }
}
