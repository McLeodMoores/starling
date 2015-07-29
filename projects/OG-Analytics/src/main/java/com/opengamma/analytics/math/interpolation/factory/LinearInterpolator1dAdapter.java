/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;

/**
 * A named interpolator called "Linear" that wraps {@link LinearInterpolator1D}.
 */
public class LinearInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Linear";

  /**
   * Creates an instance.
   */
  public LinearInterpolator1dAdapter() {
    super(new LinearInterpolator1D(), NAME);
  }
}
