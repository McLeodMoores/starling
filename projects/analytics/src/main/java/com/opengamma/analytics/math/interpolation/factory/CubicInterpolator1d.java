/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PolynomialInterpolator1D;

/**
 * A named interpolator that wraps a {@link PolynomialInterpolator1D} of degree three.
 */
@InterpolationType(name = "Cubic", aliases = { "Cubic Interpolator", "CubicInterpolator" })
public class CubicInterpolator1d extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Cubic";

  /**
   * Creates an instance called "Cubic".
   */
  public CubicInterpolator1d() {
    super(new PolynomialInterpolator1D(3), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public CubicInterpolator1d(final String name) {
    super(new PolynomialInterpolator1D(3), name);
  }
}
