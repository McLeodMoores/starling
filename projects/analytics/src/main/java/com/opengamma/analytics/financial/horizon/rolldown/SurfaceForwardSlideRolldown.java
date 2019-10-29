/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.rolldown;

import com.opengamma.analytics.financial.horizon.RolldownFunction;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Produces a {@link Surface} that has a forward slide.
 */
public final class SurfaceForwardSlideRolldown implements RolldownFunction<Surface<Double, Double, Double>> {
  /**
   * A static instance.
   */
  public static final SurfaceForwardSlideRolldown INSTANCE = new SurfaceForwardSlideRolldown();

  /**
   * Private constructor
   */
  private SurfaceForwardSlideRolldown() {
  }

  @Override
  public Surface<Double, Double, Double> rollDown(final Surface<Double, Double, Double> surface, final double time) {
    return surface;
  }

}
