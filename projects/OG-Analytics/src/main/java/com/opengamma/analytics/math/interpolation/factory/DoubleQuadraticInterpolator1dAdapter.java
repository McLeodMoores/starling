/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;

/**
 * A named interpolator called "Double Quadratic" that wraps {@link DoubleQuadraticInterpolator1D}.
 */
public class DoubleQuadraticInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public DoubleQuadraticInterpolator1dAdapter() {
    super(new DoubleQuadraticInterpolator1D(), "Double Quadratic");
  }
}
