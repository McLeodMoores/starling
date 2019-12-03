/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginSecurityBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionPremiumSecurityBlackSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;

/**
 * Calculates the delta (first derivative of the price with respect to the underlying future price) for interest rate future options.
 */
public final class UnderlyingMarketPriceSTIRFutureOptionCalculator
    extends InstrumentDerivativeVisitorAdapter<BlackSTIRFuturesProviderInterface, Double> {
  /**
   * The unique instance of the calculator.
   */
  private static final UnderlyingMarketPriceSTIRFutureOptionCalculator INSTANCE = new UnderlyingMarketPriceSTIRFutureOptionCalculator();

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static UnderlyingMarketPriceSTIRFutureOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private UnderlyingMarketPriceSTIRFutureOptionCalculator() {
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futureOption,
      final BlackSTIRFuturesProviderInterface black) {
    return InterestRateFutureOptionMarginSecurityBlackSmileMethod
        .getInstance().underlyingFuturesPrice(futureOption.getUnderlyingSecurity(), black);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity futureOption,
      final BlackSTIRFuturesProviderInterface black) {
    return InterestRateFutureOptionMarginSecurityBlackSmileMethod
        .getInstance().underlyingFuturesPrice(futureOption, black);
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction futureOption,
      final BlackSTIRFuturesProviderInterface black) {
    return InterestRateFutureOptionPremiumSecurityBlackSmileMethod.getInstance()
        .underlyingFuturesPrice(futureOption.getUnderlyingOption(), black);
  }

  @Override
  public Double visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity futureOption,
      final BlackSTIRFuturesProviderInterface black) {
    return InterestRateFutureOptionPremiumSecurityBlackSmileMethod.getInstance()
        .underlyingFuturesPrice(futureOption, black);
  }
}
