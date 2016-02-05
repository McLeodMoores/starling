/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.StepInterpolator1D;

/**
 * A named interpolator that wraps {@link StepInterpolator1D}.
 */
@InterpolationType(name = "Step", aliases = {"Step Interpolator", "StepInterpolator" })
public class StepInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Step";

  /**
   * Creates an instance called "Step".
   */
  public StepInterpolator1dAdapter() {
    super(new StepInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public StepInterpolator1dAdapter(final String name) {
    super(new StepInterpolator1D(), name);
  }
}
