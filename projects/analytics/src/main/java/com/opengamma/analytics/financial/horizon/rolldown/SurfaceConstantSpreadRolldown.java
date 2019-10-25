/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.rolldown;

import com.opengamma.analytics.financial.horizon.RolldownFunction;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Produces a {@link Surface} that has been shifted forward in time without slide. That is, it moves in such a way that the volatility
 * requested for the same maturity DATE will be equal for the original market data bundle and the shifted one.
 */
public final class SurfaceConstantSpreadRolldown implements RolldownFunction<Surface<Double, Double, Double>> {
  /**
   * A static instance.
   */
  public static final SurfaceConstantSpreadRolldown INSTANCE = new SurfaceConstantSpreadRolldown();

  /**
   * Private constructor
   */
  private SurfaceConstantSpreadRolldown() {
  }

  @Override
  public Surface<Double, Double, Double> rollDown(final Surface<Double, Double, Double> surface, final double time) {
    final Function<Double, Double> shiftedFunction = x -> surface.getZValue(x[0] + time, x[1]);
    return FunctionalDoublesSurface.from(shiftedFunction);
  }

}
