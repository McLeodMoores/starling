/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.ExponentialExtrapolator1D;

/**
 * A named extrapolator that wraps {@link ExponentialExtrapolator1D}.
 */
@InterpolationType(name = "Exponential Extrapolator", aliases = "ExponentialExtrapolator")
public class ExponentialExtrapolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The extrapolator name.
   */
  public static final String NAME = "Exponential Extrapolator";

  /**
   * Creates an instance called "Exponential Extrapolator".
   */
  public ExponentialExtrapolator1dAdapter() {
    super(new ExponentialExtrapolator1D(), NAME, true);
  }

  /**
   * Creates an instance.
   * @param name  the extrapolator name, not null
   */
  public ExponentialExtrapolator1dAdapter(final String name) {
    super(new ExponentialExtrapolator1D(), name, true);
  }
}
