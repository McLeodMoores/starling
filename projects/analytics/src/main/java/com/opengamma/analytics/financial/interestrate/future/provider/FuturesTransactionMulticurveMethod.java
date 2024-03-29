/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.provider.calculator.singlevalue.FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Generic futures transaction pricing method.
 */
public class FuturesTransactionMulticurveMethod extends FuturesTransactionMethod {

  /** The calculator used to compute the present value curve sensitivity from the price curve sensitivity **/
  private static final FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator PVCSIC = FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator
      .getInstance();

  /**
   * Constructor.
   */
  public FuturesTransactionMulticurveMethod() {
    super(new FuturesSecurityMulticurveMethod());
  }

  /**
   * Gets the securityMethod.
   *
   * @return the securityMethod
   */
  @Override
  public FuturesSecurityMulticurveMethod getSecurityMethod() {
    return (FuturesSecurityMulticurveMethod) super.getSecurityMethod();
  }

  /**
   * Compute the present value of a future transaction from a curve provider.
   *
   * @param futures
   *          The futures.
   * @param multicurve
   *          The multicurve and parameters provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final FuturesTransaction<?> futures, final ParameterProviderInterface multicurve) {
    final double price = getSecurityMethod().price(futures.getUnderlyingSecurity(), multicurve);
    return presentValueFromPrice(futures, price);
  }

  /**
   * Compute the present value curve sensitivity to rates of a future.
   *
   * @param futures
   *          The futures.
   * @param multicurve
   *          The multicurve and parameters provider.
   * @return The present value rate sensitivity.
   */

  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final FuturesTransaction<?> futures,
      final ParameterProviderInterface multicurve) {
    final MulticurveSensitivity priceSensitivity = getSecurityMethod().priceCurveSensitivity(futures.getUnderlyingSecurity(), multicurve);
    return futures.accept(PVCSIC, priceSensitivity);
  }

  // TODO
  // /**
  // * Computes the future rate (1-price) from the curves using an estimation of the future rate without convexity adjustment.
  // *
  // * @param future
  // * The future.
  // * @param curves
  // * The yield curves. Should contain the forward curve associated.
  // * @return The rate.
  // */
  // public double parRate(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
  // Validate.notNull(future, "Future");
  // Validate.notNull(curves, "Curves");
  // final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getUnderlyingSecurity().getForwardCurveName());
  // final double forward = (forwardCurve.getDiscountFactor(future.getUnderlyingSecurity().getFixingPeriodStartTime())
  // / forwardCurve.getDiscountFactor(future.getUnderlyingSecurity().getFixingPeriodEndTime()) - 1)
  // / future.getUnderlyingSecurity().getFixingPeriodAccrualFactor();
  // return forward;
  // }
}
