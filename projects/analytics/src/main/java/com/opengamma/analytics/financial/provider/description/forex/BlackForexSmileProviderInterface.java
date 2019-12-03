/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.forex;

import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.util.money.Currency;

/**
 * Interface to Forex volatility smile described from delta and multi-curves provider.
 */
public interface BlackForexSmileProviderInterface extends BlackForexProviderInterface<SmileDeltaTermStructureParametersStrikeInterpolation> {

  /**
   * Returns the (Black implied) volatility.
   * 
   * @param ccy1
   *          The first currency.
   * @param ccy2
   *          The second currency.
   * @param time
   *          The time to expiration.
   * @param strike
   *          The strike.
   * @param forward
   *          The forward.
   * @return The volatility.
   */
  double getVolatility(Currency ccy1, Currency ccy2, double time, double strike, double forward);

  /**
   * Returns the volatility and the sensitivity of this volatility to the points that were used in surface construction.
   * 
   * @param ccy1
   *          The first currency.
   * @param ccy2
   *          The second currency.
   * @param time
   *          The time to expiration.
   * @param strike
   *          The strike.
   * @param forward
   *          The forward.
   * @return Volatility and bucketed sensitivities
   */
  VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(Currency ccy1, Currency ccy2, double time, double strike, double forward);

}
