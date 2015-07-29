/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ExponentialExtrapolator1D;

/**
 * A named extrapolator called "Exponential" that wraps {@link ExponentialExtrapolator1D}.
 */
public class ExponentialExtrapolator1dAdapter extends Extrapolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Exponential";
  /**
   * Creates an instance.
   */
  public ExponentialExtrapolator1dAdapter() {
    super(new ExponentialExtrapolator1D(), NAME);
  }
}
