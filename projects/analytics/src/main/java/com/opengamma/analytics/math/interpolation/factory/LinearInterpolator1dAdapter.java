/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;

/**
 * A named interpolator that wraps {@link LinearInterpolator1D}.
 */
@InterpolationType(name = "Linear", aliases = {"Linear Interpolator", "LinearInterpolator" })
public class LinearInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Linear";

  /**
   * Creates an instance called "Linear".
   */
  public LinearInterpolator1dAdapter() {
    super(new LinearInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public LinearInterpolator1dAdapter(final String name) {
    super(new LinearInterpolator1D(), name);
  }
}
