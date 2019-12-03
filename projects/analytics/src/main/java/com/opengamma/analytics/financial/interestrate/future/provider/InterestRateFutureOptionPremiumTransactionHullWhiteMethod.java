/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a Black approach on the future
 * rate (1.0-price). The Black parameters are represented by (expiration-strike-delay) surfaces. The "delay" is the time between option
 * expiration and future last trading date, i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed
 * without convexity adjustments.
 */
public final class InterestRateFutureOptionPremiumTransactionHullWhiteMethod
    extends InterestRateFutureOptionPremiumTransactionGenericMethod<HullWhiteOneFactorProviderInterface> {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionPremiumTransactionHullWhiteMethod INSTANCE = new InterestRateFutureOptionPremiumTransactionHullWhiteMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionPremiumTransactionHullWhiteMethod() {
    super(InterestRateFutureOptionPremiumSecurityHullWhiteMethod.getInstance());
  }

  /**
   * Return the method unique instance.
   *
   * @return The instance.
   */
  public static InterestRateFutureOptionPremiumTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the method to compute the underlying security price and price curve sensitivity.
   *
   * @return The method.
   */
  @Override
  public InterestRateFutureOptionPremiumSecurityHullWhiteMethod getSecurityMethod() {
    return (InterestRateFutureOptionPremiumSecurityHullWhiteMethod) super.getSecurityMethod();
  }

}
