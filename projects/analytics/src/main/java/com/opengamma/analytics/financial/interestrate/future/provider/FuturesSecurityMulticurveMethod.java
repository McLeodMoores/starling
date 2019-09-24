/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceCurveSensitivityMulticurveCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceMulticurveCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Interface to generic futures security pricing method for multi-curve provider.
 */
public class FuturesSecurityMulticurveMethod extends FuturesSecurityMethod {

  /** The futures price calculator **/
  private static final FuturesPriceMulticurveCalculator FPMC = FuturesPriceMulticurveCalculator.getInstance();
  /** The futures price calculator **/
  private static final FuturesPriceCurveSensitivityMulticurveCalculator FPCSMC = FuturesPriceCurveSensitivityMulticurveCalculator
      .getInstance();

  /**
   * Computes the quoted price of a futures from a multicurve provider.
   *
   * @param futures
   *          The futures security.
   * @param multicurve
   *          The multicurve provider.
   * @return The price.
   */
  public double price(final FuturesSecurity futures, final ParameterProviderInterface multicurve) {
    return futures.accept(FPMC, multicurve);
  }

  /**
   * Computes the quoted price curve sensitivity of a futures from a multicurve provider.
   *
   * @param futures
   *          The futures security.
   * @param multicurve
   *          The multicurve provider.
   * @return The price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final FuturesSecurity futures, final ParameterProviderInterface multicurve) {
    return futures.accept(FPCSMC, multicurve);
  }

  /**
   * Computes the present value of the future security as the value of one future with a price of 0.
   *
   * @param future
   *          The future security.
   * @param curves
   *          The curves.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final FederalFundsFutureSecurity future, final MulticurveProviderInterface curves) {
    final double price = price(future, curves);
    final double pv = price * future.getPaymentAccrualFactor() * future.getNotional();
    return MultipleCurrencyAmount.of(future.getCurrency(), pv);
  }

  // TODO
  // /**
  // * Computes the future rate (1-price) curve sensitivity from the curves using an estimation of the future rate without convexity
  // adjustment.
  // *
  // * @param future
  // * The future.
  // * @param curves
  // * The yield curves. Should contain the forward curve associated.
  // * @return The rate curve sensitivity.
  // */
  // public InterestRateCurveSensitivity parRateCurveSensitivity(final InterestRateFutureSecurity future, final MulticurveProviderInterface
  // curves) {
  // ArgumentChecker.notNull(future, "future");
  // ArgumentChecker.notNull(curves, "curves");
  // final YieldAndDiscountCurve curve = curves.getCurve(curveName);
  // final double ta = future.getFixingPeriodStartTime();
  // final double tb = future.getFixingPeriodEndTime();
  // final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / future.getFixingPeriodAccrualFactor();
  // final DoublesPair s1 = DoublesPair.of(ta, -ta * ratio);
  // final DoublesPair s2 = DoublesPair.of(tb, tb * ratio);
  // final List<DoublesPair> temp = new ArrayList<>();
  // temp.add(s1);
  // temp.add(s2);
  // final Map<String, List<DoublesPair>> result = new HashMap<>();
  // result.put(curveName, temp);
  // return new InterestRateCurveSensitivity(result);
  // }

}
