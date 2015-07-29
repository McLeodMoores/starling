/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PolynomialInterpolator1D;

/**
 * A named interpolator called "Quadratic" that wraps a {@link PolynomialInterpolator1D} of degree two.
 */
public class QuadraticInterpolator1d extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Quadratic";

  /**
   * Creates an instance.
   */
  public QuadraticInterpolator1d() {
    super(new PolynomialInterpolator1D(2), NAME);
  }
}
