/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a SABR approach on the future rate
 * (1.0-price). The SABR parameters are represented by (expiration-delay) surfaces. The "delay" is the time between option expiration and
 * future last trading date, i.e. 0 for normal options and x for x-year mid-curve options.
 */
public final class InterestRateFutureOptionPremiumTransactionSABRMethod
    extends InterestRateFutureOptionPremiumTransactionGenericMethod<SABRSTIRFuturesProviderInterface> {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionPremiumTransactionSABRMethod INSTANCE = new InterestRateFutureOptionPremiumTransactionSABRMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionPremiumTransactionSABRMethod() {
    super(InterestRateFutureOptionPremiumSecuritySABRMethod.getInstance());
  }

  /**
   * Return the method unique instance.
   *
   * @return The instance.
   */
  public static InterestRateFutureOptionPremiumTransactionSABRMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the method to compute the underlying security price and price curve sensitivity.
   *
   * @return The method.
   */
  @Override
  public InterestRateFutureOptionPremiumSecuritySABRMethod getSecurityMethod() {
    return (InterestRateFutureOptionPremiumSecuritySABRMethod) super.getSecurityMethod();
  }

  /**
   * Computes the present value of a transaction from the future price and curve/volatility data.
   *
   * @param transaction
   *          The future option transaction.
   * @param sabrData
   *          The SABR and multi-curves provider.
   * @param priceFuture
   *          The price of the underlying future.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromFuturePrice(final InterestRateFutureOptionPremiumTransaction transaction,
      final SABRSTIRFuturesProviderInterface sabrData, final double priceFuture) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(sabrData, "SABR / multi-curves provider");
    final double priceSecurity = getSecurityMethod().priceFromFuturePrice(transaction.getUnderlyingOption(), sabrData, priceFuture);
    final MultipleCurrencyAmount priceTransaction = presentValueFromPrice(transaction, sabrData, priceSecurity);
    return priceTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   *
   * @param transaction
   *          The future option transaction.
   * @param sabrData
   *          The SABR and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final InterestRateFutureOptionPremiumTransaction transaction,
      final SABRSTIRFuturesProviderInterface sabrData) {
    PresentValueSABRSensitivityDataBundle securitySensitivity = getSecurityMethod()
        .priceSABRSensitivity(transaction.getUnderlyingOption(), sabrData);
    securitySensitivity = securitySensitivity
        .multiplyBy(transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
            * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

}
