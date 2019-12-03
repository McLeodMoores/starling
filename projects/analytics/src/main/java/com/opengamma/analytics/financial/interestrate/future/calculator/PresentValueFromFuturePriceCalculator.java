/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureTransactionDiscountingMethod;

// CSOFF
/**
 * Calculate present value for futures from the quoted price.
 *
 * @deprecated {@link com.opengamma.analytics.financial.interestrate.YieldCurveBundle} is deprecated
 */
@Deprecated
public final class PresentValueFromFuturePriceCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final PresentValueFromFuturePriceCalculator INSTANCE = new PresentValueFromFuturePriceCalculator();
  /**
   * The method to compute bond future prices.
   */
  private static final BondFutureDiscountingMethod METHOD_BOND_FUTURE = BondFutureDiscountingMethod.getInstance();

  /**
   * The method to compute interest rate future prices.
   */
  private static final InterestRateFutureTransactionDiscountingMethod METHOD_RATE_FUTURE = InterestRateFutureTransactionDiscountingMethod
      .getInstance();

  /**
   * Return the calculator instance.
   *
   * @return The instance.
   */
  public static PresentValueFromFuturePriceCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PresentValueFromFuturePriceCalculator() {
  }

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final Double futurePrice) {
    Validate.notNull(future);
    return METHOD_RATE_FUTURE.presentValueFromPrice(future, futurePrice).getAmount(future.getCurrency());
  }

  @Override
  public Double visitBondFuture(final BondFuture future, final Double futurePrice) {
    Validate.notNull(future);
    return METHOD_BOND_FUTURE.presentValueFromPrice(future, futurePrice).getAmount();
  }

}
