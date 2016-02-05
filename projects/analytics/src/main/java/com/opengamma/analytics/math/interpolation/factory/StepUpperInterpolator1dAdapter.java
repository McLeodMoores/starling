/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.StepUpperInterpolator1D;

/**
 * A named interpolator that wraps {@link StepUpperInterpolator1D}.
 */
@InterpolationType(name = "Step Upper", aliases = {"Step Upper Interpolator", "StepUpperInterpolator", "StepUpper" })
public class StepUpperInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Step Upper";

  /**
   * Creates an instance called "Step Upper".
   */
  public StepUpperInterpolator1dAdapter() {
    super(new StepUpperInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public StepUpperInterpolator1dAdapter(final String name) {
    super(new StepUpperInterpolator1D(), name);
  }
}
