/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * A named extrapolator that wraps {@link LinearExtrapolator1D}.
 */
@InterpolationType(name = "Linear Extrapolator", aliases = "LinearExtrapolator")
public class LinearExtrapolator1dAdapter extends Interpolator1dAdapter {
  /** Serialization version */
  private static final long serialVersionUID = 1L;
  /**
   * The extrapolator name.
   */
  public static final String NAME = "Linear Extrapolator";

  /**
   * Creates an instance called "Linear Extrapolator[INTERPOLATOR_NAME]".
   * @param interpolator  the interpolator, not null
   */
  public LinearExtrapolator1dAdapter(final Interpolator1dAdapter interpolator) {
    super(new LinearExtrapolator1D(interpolator), transformName(NAME, interpolator.getName()), true);
  }

  /**
   * Creates an instance.
   * @param interpolator  the interpolator, not null
   * @param name  the extrapolator name, not null
   */
  public LinearExtrapolator1dAdapter(final Interpolator1dAdapter interpolator, final String name) {
    super(new LinearExtrapolator1D(interpolator), name, true);
  }

  /**
   * Transforms the name of the interpolator into the extrapolator: (EXTRAPOLATOR_NAME, NAME) -> EXTRAPOLATOR_NAME[NAME].
   * @param extrapolatorName  the version of the name of the linear extrapolator, not null
   * @param name  the interpolator name, not null
   * @return  the transformed name
   */
  protected static String transformName(final String extrapolatorName, final String name) {
    return ArgumentChecker.notNull(extrapolatorName, "extrapolatorName") + "[" + ArgumentChecker.notNull(name, "name") + "]";
  }
}
