/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PolynomialInterpolator1D;

/**
 * A named interpolator that wraps a {@link PolynomialInterpolator1D} of degree five.
 */
@InterpolationType(name = "Quintic", aliases = {"Quintic Interpolator", "QuinticInterpolator" })
public class QuinticInterpolator1d extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Quintic";

  /**
   * Creates an instance called "Quintic".
   */
  public QuinticInterpolator1d() {
    super(new PolynomialInterpolator1D(5), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public QuinticInterpolator1d(final String name) {
    super(new PolynomialInterpolator1D(5), name);
  }
}
