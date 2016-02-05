/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;

/**
 * A named interpolator that wraps {@link DoubleQuadraticInterpolator1D}.
 */
@InterpolationType(name = "Double Quadratic", aliases = {"DoubleQuadratic", "Double Quadratic Interpolator", "DoubleQuadraticInterpolator" })
public class DoubleQuadraticInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Double Quadratic";

  /**
   * Creates an instance called "Double Quadratic".
   */
  public DoubleQuadraticInterpolator1dAdapter() {
    super(new DoubleQuadraticInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public DoubleQuadraticInterpolator1dAdapter(final String name) {
    super(new DoubleQuadraticInterpolator1D(), name);
  }
}
