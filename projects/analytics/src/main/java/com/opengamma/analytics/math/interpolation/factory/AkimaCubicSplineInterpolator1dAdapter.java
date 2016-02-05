/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.SemiLocalCubicSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link SemiLocalCubicSplineInterpolator1D}.
 */
@InterpolationType(name = "Akima Cubic Spline", aliases = {"AkimaCubicSpline", "Akima Cubic Spline Interpolator", "AkimaCubicSplineInterpolator" })
public class AkimaCubicSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Akima Cubic Spline";

  /**
   * Creates an instance called "Akima Cubic Spline".
   */
  public AkimaCubicSplineInterpolator1dAdapter() {
    super(new SemiLocalCubicSplineInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public AkimaCubicSplineInterpolator1dAdapter(final String name) {
    super(new SemiLocalCubicSplineInterpolator1D(), name);
  }
}
