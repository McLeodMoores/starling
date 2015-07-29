/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.SemiLocalCubicSplineInterpolator1D;

/**
 * A named interpolator called "Akima Cubic Spline" that wraps {@link SemiLocalCubicSplineInterpolator1D}.
 */
public class AkimaCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Akima Cubic Spline";

  /**
   * Creates an instance.
   */
  public AkimaCubicSplineInterpolator1dAdapter() {
    super(new SemiLocalCubicSplineInterpolator1D(), NAME);
  }
}
