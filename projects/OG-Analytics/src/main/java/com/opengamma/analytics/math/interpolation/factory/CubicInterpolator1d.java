/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PolynomialInterpolator1D;

/**
 * A named interpolator called "Cubic" that wraps a {@link PolynomialInterpolator1D} of degree three.
 */
public class CubicInterpolator1d extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public CubicInterpolator1d() {
    super(new PolynomialInterpolator1D(3), "Cubic");
  }
}
