/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;

/**
 * Sets of market data used in tests.
 */
public class NormalDataSets {

  private static final Interpolator1D LINEAR_FLAT = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);

  private static final double[] VOLATILITIES = new double[] {0.0100, 0.0110, 0.0120, 0.0090, 0.0100, 0.0100};
  private static final double[] EXPIRATIONS = new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0};
  private static final double[] STRIKES_PRICES = new double[] {0.99, 0.99, 0.99, 0.995, 0.995, 0.995};
  private static final InterpolatedDoublesSurface NORMAL_SURFACE_FUT_PRICE = InterpolatedDoublesSurface.from(EXPIRATIONS, STRIKES_PRICES, VOLATILITIES, INTERPOLATOR_2D);

  public static InterpolatedDoublesSurface createNormalSurfaceFuturesPrices() {
    return NORMAL_SURFACE_FUT_PRICE;
  }

  public static InterpolatedDoublesSurface createNormalSurfaceFuturesPricesShift(final double shift) {
    final double[] shiftedVol = VOLATILITIES.clone();
    for (int loopvol = 0; loopvol < shiftedVol.length; loopvol++) {
      shiftedVol[loopvol] += shift;
    }
    return InterpolatedDoublesSurface.from(EXPIRATIONS, STRIKES_PRICES, shiftedVol, INTERPOLATOR_2D);
  }

}
