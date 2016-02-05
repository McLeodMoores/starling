/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.PCHIPInterpolator1D;

/**
 * A named interpolator that wraps {@link PCHIPInterpolator1D}.
 */
@InterpolationType(name = "PCHIP", aliases = {"PCHIP Interpolator", "Piecewise Cubic Hermite Interpolating Polynomial",
    "Piecewise Cubic Hermite Interpolating Polynomial Interpolator", "MonotonicityPreservingCubicSpline" })
public class PchipInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "PCHIP";

  /**
   * Creates an instance called "PCHIP".
   */
  public PchipInterpolator1dAdapter() {
    super(new PCHIPInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public PchipInterpolator1dAdapter(final String name) {
    super(new PCHIPInterpolator1D(), name);
  }
}
