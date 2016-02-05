/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PCHIPYieldCurveInterpolator1D;

/**
 * A named interpolator that wraps {@link PCHIPYieldCurveInterpolator1D}.
 */
@InterpolationType(name = "Modified PCHIP", aliases = {"ModifiedPCHIP", "Modified PCHIP Interpolator", "ModifiedPCHIPInterpolator",
    "Modified Piecewise Cubic Hermite Interpolating Polynomial", "Modified Piecewise Cubic Hermite Interpolating Polynomial Interpolator" })
public class ModifiedPchipInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Modified PCHIP";

  /**
   * Creates an instance called "Modified PCHIP".
   */
  public ModifiedPchipInterpolator1dAdapter() {
    super(new PCHIPYieldCurveInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public ModifiedPchipInterpolator1dAdapter(final String name) {
    super(new PCHIPYieldCurveInterpolator1D(), name);
  }
}
