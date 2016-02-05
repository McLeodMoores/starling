/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PolynomialInterpolator1D;

/**
 * A named interpolator that wraps a {@link PolynomialInterpolator1D} of degree four.
 */
@InterpolationType(name = "Quartic", aliases = {"Quartic Interpolator", "QuarticInterpolator" })
public class QuarticInterpolator1d extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Quartic";

  /**
   * Creates an instance called "Quartic".
   */
  public QuarticInterpolator1d() {
    super(new PolynomialInterpolator1D(4), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public QuarticInterpolator1d(final String name) {
    super(new PolynomialInterpolator1D(4), name);
  }
}
