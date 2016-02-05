/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ExponentialInterpolator1D;

/**
 * A named interpolator that wraps {@link ExponentialInterpolator1D}.
 */
@InterpolationType(name = "Exponential", aliases = {"Exponential Interpolator", "ExponentialInterpolator" })
public class ExponentialInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Exponential";

  /**
   * Creates an instance called "Exponential".
   */
  public ExponentialInterpolator1dAdapter() {
    super(new ExponentialInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public ExponentialInterpolator1dAdapter(final String name) {
    super(new ExponentialInterpolator1D(), name);
  }
}
