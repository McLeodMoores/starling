/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PCHIPInterpolator1D;

/**
 * A named interpolator called "PCHIP" that wraps {@link PCHIPInterpolator1D}.
 */
public class PchipInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "PCHIP";

  /**
   * Creates an instance.
   */
  public PchipInterpolator1dAdapter() {
    super(new PCHIPInterpolator1D(), NAME);
  }
}
