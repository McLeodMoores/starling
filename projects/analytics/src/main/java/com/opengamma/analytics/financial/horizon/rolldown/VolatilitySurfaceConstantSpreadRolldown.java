/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.rolldown;

import com.opengamma.analytics.financial.horizon.RolldownFunction;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 *
 */
public final class VolatilitySurfaceConstantSpreadRolldown implements RolldownFunction<VolatilitySurface> {
  /**
   * A static instance.
   */
  public static final RolldownFunction<VolatilitySurface> INSTANCE = new VolatilitySurfaceConstantSpreadRolldown();

  @Override
  public VolatilitySurface rollDown(final VolatilitySurface volatilitySurface, final double time) {
    final Surface<Double, Double, Double> surface = volatilitySurface.getSurface();
    final Function<Double, Double> shiftedFunction = x -> surface.getZValue(x[0] + time, x[1]);
    return new VolatilitySurface(FunctionalDoublesSurface.from(shiftedFunction));
  }

  private VolatilitySurfaceConstantSpreadRolldown() {
  }
}
