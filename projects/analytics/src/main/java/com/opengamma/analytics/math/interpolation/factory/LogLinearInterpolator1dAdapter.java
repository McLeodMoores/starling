/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.LogLinearInterpolator1D;

/**
 * A named interpolator that wraps {@link LogLinearInterpolator1D}.
 */
@InterpolationType(name = "Log Linear", aliases = {"LogLinear", "Log Linear Interpolator", "LogLinearInterpolator" })
public class LogLinearInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Log Linear";

  /**
   * Creates an instance called "Log Linear".
   */
  public LogLinearInterpolator1dAdapter() {
    super(new LogLinearInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public LogLinearInterpolator1dAdapter(final String name) {
    super(new LogLinearInterpolator1D(), name);
  }
}
