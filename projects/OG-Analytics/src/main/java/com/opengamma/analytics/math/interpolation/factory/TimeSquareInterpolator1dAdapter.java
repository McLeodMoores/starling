/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.TimeSquareInterpolator1D;

/**
 * A named interpolator called "Time Square" that wraps {@link TimeSquareInterpolator1D}.
 */
public class TimeSquareInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public TimeSquareInterpolator1dAdapter() {
    super(new TimeSquareInterpolator1D(), "Time Square");
  }
}
