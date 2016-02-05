/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.NaturalSplineInterpolator1D;

/**
 * A named interpolator that wraps {@link NaturalSplineInterpolator1D}.
 */
@InterpolationType(name = "Natural Spline", aliases = {"NaturalSpline", "Natural Spline Interpolator", "NaturalSplineInterpolator" })
public class NaturalSplineInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Natural Spline";

  /**
   * Creates an instance called "Natural Spline".
   */
  public NaturalSplineInterpolator1dAdapter() {
    super(new NaturalSplineInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public NaturalSplineInterpolator1dAdapter(final String name) {
    super(new NaturalSplineInterpolator1D(), name);
  }
}
