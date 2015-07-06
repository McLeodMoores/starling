/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.StepUpperInterpolator1D;

/**
 * A named interpolator called "Step Upper" that wraps {@link StepUpperInterpolator1D}.
 */
public class StepUpperInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public StepUpperInterpolator1dAdapter() {
    super(new StepUpperInterpolator1D(), "Step Upper");
  }
}
