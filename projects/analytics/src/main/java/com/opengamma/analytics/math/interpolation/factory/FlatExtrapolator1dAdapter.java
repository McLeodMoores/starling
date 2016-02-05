/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;

/**
 * A named extrapolator that wraps {@link FlatExtrapolator1D}.
 */
@InterpolationType(name = "Flat Extrapolator", aliases = "FlatExtrapolator")
public class FlatExtrapolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The extrapolator name.
   */
  public static final String NAME = "Flat Extrapolator";

  /**
   * Creates an instance called "Flat Extrapolator".
   */
  public FlatExtrapolator1dAdapter() {
    super(new FlatExtrapolator1D(), NAME, true);
  }

  /**
   * Creates an instance.
   * @param name  the extrapolator name, not null
   */
  public FlatExtrapolator1dAdapter(final String name) {
    super(new FlatExtrapolator1D(), name, true);
  }
}
