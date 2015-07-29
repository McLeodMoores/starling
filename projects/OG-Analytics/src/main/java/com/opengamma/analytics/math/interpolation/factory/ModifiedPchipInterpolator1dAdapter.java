/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PCHIPYieldCurveInterpolator1D;

/**
 * A named interpolator called "Modified PCHIP" that wraps {@link PCHIPYieldCurveInterpolator1D}.
 */
public class ModifiedPchipInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Modified PCHIP";

  /**
   * Creates an instance.
   */
  public ModifiedPchipInterpolator1dAdapter() {
    super(new PCHIPYieldCurveInterpolator1D(), NAME);
  }
}
