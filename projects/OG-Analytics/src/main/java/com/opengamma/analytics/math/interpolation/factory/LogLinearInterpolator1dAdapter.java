/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.LogLinearInterpolator1D;

/**
 * A named interpolator called "Log Linear" that wraps {@link LogLinearInterpolator1D}.
 */
public class LogLinearInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Log Linear";

  /**
   * Creates an instance.
   */
  public LogLinearInterpolator1dAdapter() {
    super(new LogLinearInterpolator1D(), NAME);
  }
}
