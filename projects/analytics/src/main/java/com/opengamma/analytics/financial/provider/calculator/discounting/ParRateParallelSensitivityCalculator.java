/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Calculates the change in par rate of an instrument (the exact meaning of par rate depends on the instrument - for swaps it is the par
 * swap rate) due to a parallel move of each yield curve that the instrument is sensitive to - dPar/dR where dR is a movement of the whole
 * curve.
 */
public final class ParRateParallelSensitivityCalculator
    extends InstrumentDerivativeVisitorSameMethodAdapter<MulticurveProviderInterface, Map<String, Double>> {
  private static final ParRateParallelSensitivityCalculator INSTANCE = new ParRateParallelSensitivityCalculator();
  private final ParRateCurveSensitivityDiscountingCalculator _prcsc = ParRateCurveSensitivityDiscountingCalculator.getInstance();

  public static ParRateParallelSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  private ParRateParallelSensitivityCalculator() {
  }

  /**
   * Calculates the change in par rate of an instrument due to a parallel move of each yield curve the instrument is sensitive to.
   *
   * @param ird
   *          instrument
   * @param curves
   *          bundle of relevant yield curves
   * @return the sensitivities
   */
  @Override
  public Map<String, Double> visit(final InstrumentDerivative ird, final MulticurveProviderInterface curves) {
    final MulticurveSensitivity sensitivities = ird.accept(_prcsc, curves);
    final Map<String, Double> result = new HashMap<>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivities.getYieldDiscountingSensitivities().entrySet()) {
      final String name = entry.getKey();
      final double temp = sumListPair(entry.getValue());
      result.put(name, temp);
    }
    return result;
  }

  private static double sumListPair(final List<DoublesPair> list) {
    double sum = 0.0;
    for (final DoublesPair pair : list) {
      sum += pair.getSecond();
    }
    return sum;
  }

  @Override
  public Map<String, Double> visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need curves data");
  }

}
