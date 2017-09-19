/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.surface.volatility.fx;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivitiesModel;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class FxOptionDataProvider
  implements BlackForexProviderInterface<VolatilityAndBucketedSensitivitiesModel<Triple<Double, Double, Double>>> {

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return null;
  }

  @Override
  public double[] parameterForwardSensitivity(final String name,
      final List<ForwardSensitivity> pointSensitivity) {
    return null;
  }

  @Override
  public Set<String> getAllCurveNames() {
    return null;
  }

  @Override
  public BlackForexProviderInterface<VolatilityAndBucketedSensitivitiesModel<Triple<Double, Double, Double>>> copy() {
    return null;
  }

  @Override
  public VolatilityAndBucketedSensitivitiesModel<Triple<Double, Double, Double>> getVolatility() {
    return null;
  }

  @Override
  public Pair<Currency, Currency> getCurrencyPair() {
    return null;
  }

  @Override
  public boolean checkCurrencies(final Currency ccy1, final Currency ccy2) {
    return false;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return null;
  }

}
