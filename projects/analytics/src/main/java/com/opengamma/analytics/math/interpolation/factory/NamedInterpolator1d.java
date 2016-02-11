/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.NamedInstance;

/**
 * An interface for named instances of {@link Interpolator1D}. Classes that implement this interface can
 * be obtained from a {@link NamedInterpolator1dFactory}.
 */
public abstract class NamedInterpolator1d extends Interpolator1D implements NamedInstance {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Returns true if extrapolation is performed.
   * @return  true if extrapolation is performed
   */
  public abstract boolean isExtrapolator();

}
