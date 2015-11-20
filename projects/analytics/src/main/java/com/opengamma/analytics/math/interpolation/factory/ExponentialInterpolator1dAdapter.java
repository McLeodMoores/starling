/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ExponentialInterpolator1D;

/**
 * A named interpolator called "Exponential" that wraps {@link ExponentialInterpolator1D}.
 */
public class ExponentialInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Exponential";

  /**
   * Creates an instance.
   */
  public ExponentialInterpolator1dAdapter() {
    super(new ExponentialInterpolator1D(), NAME);
  }
}
