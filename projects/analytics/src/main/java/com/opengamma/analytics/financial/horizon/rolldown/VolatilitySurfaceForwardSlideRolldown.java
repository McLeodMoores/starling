/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.rolldown;

import com.opengamma.analytics.financial.horizon.RolldownFunction;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;

/**
 *
 */
public final class VolatilitySurfaceForwardSlideRolldown implements RolldownFunction<VolatilitySurface> {
  /**
   * A static instance.
   */
  public static final RolldownFunction<VolatilitySurface> INSTANCE = new VolatilitySurfaceForwardSlideRolldown();

  @Override
  public VolatilitySurface rollDown(final VolatilitySurface volatilitySurface, final double time) {
    return volatilitySurface;
  }

  private VolatilitySurfaceForwardSlideRolldown() {
  }
}
