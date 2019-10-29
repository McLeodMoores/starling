/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.rolldown;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.horizon.RolldownFunction;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderDiscount;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public final class FxOptionBlackDataForwardSlideRolldown implements RolldownFunction<BlackForexSmileProviderInterface> {
  /**
   * A static instance.
   */
  public static final FxOptionBlackDataForwardSlideRolldown INSTANCE = new FxOptionBlackDataForwardSlideRolldown();

  /**
   * Private constructor
   */
  private FxOptionBlackDataForwardSlideRolldown() {
  }

  @Override
  public BlackForexSmileProviderInterface rollDown(final BlackForexSmileProviderInterface data, final double shiftTime) {
    final ParameterProviderInterface shiftedCurves = CurveProviderForwardSlideRolldown.INSTANCE.rollDown(data, shiftTime);
    final Pair<Currency, Currency> currencyPair = data.getCurrencyPair();
    if (shiftedCurves instanceof MulticurveProviderDiscount) {
      return new BlackForexSmileProviderDiscount((MulticurveProviderDiscount) shiftedCurves, data.getVolatility(), currencyPair);
    }
    throw new NotImplementedException();
  }

}
