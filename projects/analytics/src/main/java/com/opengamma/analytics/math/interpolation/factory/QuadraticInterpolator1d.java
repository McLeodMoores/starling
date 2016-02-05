/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PolynomialInterpolator1D;

/**
 * A named interpolator that wraps a {@link PolynomialInterpolator1D} of degree two.
 */
@InterpolationType(name = "Quadratic", aliases = {"Quadratic Interpolator", "QuadraticInterpolator" })
public class QuadraticInterpolator1d extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Quadratic";

  /**
   * Creates an instance called "Quadratic".
   */
  public QuadraticInterpolator1d() {
    super(new PolynomialInterpolator1D(2), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public QuadraticInterpolator1d(final String name) {
    super(new PolynomialInterpolator1D(2), name);
  }
}
