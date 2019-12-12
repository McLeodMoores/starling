/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.rolldown;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.horizon.RolldownFunction;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Changes the provided market data so that the curves and (Black) surface have been shifted forward in time without slide.
 */
public final class SwaptionBlackDataConstantSpreadRolldown implements RolldownFunction<BlackSwaptionFlatProviderInterface> {
  /**
   * A static instance.
   */
  public static final SwaptionBlackDataConstantSpreadRolldown INSTANCE = new SwaptionBlackDataConstantSpreadRolldown();

  /**
   * Private constructor
   */
  private SwaptionBlackDataConstantSpreadRolldown() {
  }

  @Override
  public BlackSwaptionFlatProviderInterface rollDown(final BlackSwaptionFlatProviderInterface data, final double time) {
    final ParameterProviderInterface shiftedCurves = CurveProviderConstantSpreadRolldown.INSTANCE.rollDown(data, time);
    final Surface<Double, Double, Double> surface = data.getBlackParameters().getVolatilitySurface();
    final Surface<Double, Double, Double> shiftedVolatilitySurface = SurfaceConstantSpreadRolldown.INSTANCE.rollDown(surface, time);
    final BlackFlatSwaptionParameters shiftedParameters = new BlackFlatSwaptionParameters(shiftedVolatilitySurface,
        data.getBlackParameters().getGeneratorSwap());
    if (shiftedCurves instanceof MulticurveProviderDiscount) {
      return new BlackSwaptionFlatProviderDiscount((MulticurveProviderDiscount) shiftedCurves, shiftedParameters);
    }
    throw new NotImplementedException("Cannot handle shifted curves of type " + shiftedCurves.getClass());
  }
}
