/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;

/**
 * A named extrapolator called "Flat" that wraps {@link FlatExtrapolator1D}.
 */
public class FlatExtrapolator1dAdapter extends Extrapolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance.
   */
  public FlatExtrapolator1dAdapter() {
    super(new FlatExtrapolator1D(), "Flat");
  }
}
