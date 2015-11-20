/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PolynomialInterpolator1D;

/**
 * A named interpolator called "Quintic" that wraps a {@link PolynomialInterpolator1D} of degree five.
 */
public class QuinticInterpolator1d extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Quintic";

  /**
   * Creates an instance.
   */
  public QuinticInterpolator1d() {
    super(new PolynomialInterpolator1D(5), NAME);
  }
}
