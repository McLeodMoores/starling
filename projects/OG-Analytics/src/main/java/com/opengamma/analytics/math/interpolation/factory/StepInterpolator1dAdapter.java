/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.StepInterpolator1D;

/**
 * A named interpolator called "Step" that wraps {@link StepInterpolator1D}.
 */
public class StepInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public StepInterpolator1dAdapter() {
    super(new StepInterpolator1D(), "Step");
  }
}
