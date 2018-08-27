/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class MultipleCurrencyInterestRateCurveSensitivityConverter implements ResultConverter<MultipleCurrencyInterestRateCurveSensitivity> {

  @Override
  public Map<String, Double> convert(final String valueName, final MultipleCurrencyInterestRateCurveSensitivity value) {
    final Map<String, Double> returnValue = new HashMap<>();
    for (final Currency ccy : value.getCurrencies()) {
      final InterestRateCurveSensitivity ccySensitivity = value.getSensitivity(ccy);
      for (final Map.Entry<String, List<DoublesPair>> curveSensitivities : ccySensitivity.getSensitivities().entrySet()) {
        final String curveName = curveSensitivities.getKey();
        for (final DoublesPair sensitivityEntry : curveSensitivities.getValue()) {
          final Double cashFlowTime = sensitivityEntry.getFirst();
          final Double sensitivityValue = sensitivityEntry.getSecond();
          final String key = valueName + "[" + ccy.getCode() + "][" + curveName + "]";
          returnValue.put(key + "[time]", cashFlowTime);
          returnValue.put(key + "[value]", sensitivityValue);
        }
      }
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return MultipleCurrencyInterestRateCurveSensitivity.class;
  }

}
