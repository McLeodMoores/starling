/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionSABRMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionPremiumTransactionSABRMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueSABRSTIRFuturesCalculator
    extends InstrumentDerivativeVisitorAdapter<SABRSTIRFuturesProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRSTIRFuturesCalculator INSTANCE = new PresentValueSABRSTIRFuturesCalculator();

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static PresentValueSABRSTIRFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueSABRSTIRFuturesCalculator() {
  }

  // ----- Futures ------

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futureOption,
      final SABRSTIRFuturesProviderInterface sabr) {
    return InterestRateFutureOptionMarginTransactionSABRMethod.getInstance().presentValue(futureOption, sabr);
  }

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureOptionPremiumTransaction(
      final InterestRateFutureOptionPremiumTransaction futureOption,
      final SABRSTIRFuturesProviderInterface sabr) {
    return InterestRateFutureOptionPremiumTransactionSABRMethod.getInstance().presentValue(futureOption, sabr);
  }

}
