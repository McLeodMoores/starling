/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator1D;

/**
 * A named interpolator called "Natural Spline" that wraps {@link NaturalSplineInterpolator1D}.
 */
public class NaturalSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public NaturalSplineInterpolator1dAdapter() {
    super(new NaturalSplineInterpolator1D(), "Natural Spline");
  }
}
