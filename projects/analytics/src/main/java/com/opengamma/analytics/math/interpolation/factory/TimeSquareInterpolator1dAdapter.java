/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.TimeSquareInterpolator1D;

/**
 * A named interpolator that wraps {@link TimeSquareInterpolator1D}.
 */
@InterpolationType(name = "Time Square", aliases = {"TimeSquare", "Time Square Interpolator", "TimeSquareInterpolator" })
public class TimeSquareInterpolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The interpolator name.
   */
  public static final String NAME = "Time Square";

  /**
   * Creates an instance called "Time Square".
   */
  public TimeSquareInterpolator1dAdapter() {
    super(new TimeSquareInterpolator1D(), NAME);
  }

  /**
   * Creates an instance.
   * @param name  the interpolator name, not null
   */
  public TimeSquareInterpolator1dAdapter(final String name) {
    super(new TimeSquareInterpolator1D(), name);
  }
}
