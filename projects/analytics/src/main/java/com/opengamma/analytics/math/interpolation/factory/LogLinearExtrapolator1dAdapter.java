/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.LogLinearExtrapolator1D;

/**
 * A named extrapolator that wraps {@link LogLinearExtrapolator1D}.
 */
@InterpolationType(name = "Log Linear Extrapolator", aliases = "LogLinearExtrapolator")
public class LogLinearExtrapolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The extrapolator name.
   */
  public static final String NAME = "Log Linear Extrapolator";

  /**
   * Creates an instance called "Log Linear Extrapolator[INTERPOLATOR_NAME]".
   * @param interpolator  the interpolator, not null
   */
  public LogLinearExtrapolator1dAdapter(final Interpolator1dAdapter interpolator) {
    super(new LogLinearExtrapolator1D(interpolator), NamedInterpolator1dFactory.transformName(NAME, interpolator.getName()), true);
  }

  /**
   * Creates an instance.
   * @param interpolator  the interpolator, not null
   * @param name  the extrapolator name, not null
   */
  public LogLinearExtrapolator1dAdapter(final Interpolator1dAdapter interpolator, final String name) {
    super(new LogLinearExtrapolator1D(interpolator), name, true);
  }

}
