/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.rolldown;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.horizon.RolldownFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Produces a YieldCurveWithBlackCubeBundle that has been shifted forward in time without slide. That is, it moves in such a way that the
 * vol or rate requested for the same maturity DATE will be equal for the original market data bundle and the shifted one.
 */
public final class StirFutureOptionBlackDataConstantSpreadRolldown implements RolldownFunction<BlackSTIRFuturesProviderInterface> {
  /** The singleton instance */
  private static final StirFutureOptionBlackDataConstantSpreadRolldown INSTANCE = new StirFutureOptionBlackDataConstantSpreadRolldown();

  /**
   * Gets the singleton instance.
   *
   * @return The instance
   */
  public static StirFutureOptionBlackDataConstantSpreadRolldown getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private StirFutureOptionBlackDataConstantSpreadRolldown() {
  }

  @Override
  public BlackSTIRFuturesProviderInterface rollDown(final BlackSTIRFuturesProviderInterface data, final double time) {
    if (!(data instanceof BlackSTIRFuturesSmileProvider)) {
      throw new NotImplementedException("Unhandled data type " + data.getClass());
    }
    final ParameterProviderInterface shiftedCurves = CurveProviderConstantSpreadRolldown.INSTANCE.rollDown(data, time);
    final Surface<Double, Double, Double> surface = ((BlackSTIRFuturesSmileProvider) data).getBlackParameters();
    final Surface<Double, Double, Double> shiftedVolatilitySurface = SurfaceConstantSpreadRolldown.INSTANCE.rollDown(surface, time);
    if (shiftedCurves instanceof MulticurveProviderDiscount) {
      return new BlackSTIRFuturesSmileProviderDiscount((MulticurveProviderDiscount) shiftedCurves, shiftedVolatilitySurface,
          data.getFuturesIndex());
    }
    throw new NotImplementedException("Unhandled data type " + shiftedCurves.getClass());
  }
}
