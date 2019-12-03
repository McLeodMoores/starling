/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a Black approach on the future
 * rate (1.0-price). The Black parameters are represented by (expiration-strike-delay) surfaces. The "delay" is the time between option
 * expiration and future last trading date, i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed
 * without convexity adjustments.
 */
public final class InterestRateFutureOptionPremiumTransactionBlackSmileMethod
    extends InterestRateFutureOptionPremiumTransactionGenericMethod<BlackSTIRFuturesProviderInterface> {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionPremiumTransactionBlackSmileMethod INSTANCE = new InterestRateFutureOptionPremiumTransactionBlackSmileMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionPremiumTransactionBlackSmileMethod() {
    super(InterestRateFutureOptionPremiumSecurityBlackSmileMethod.getInstance());
  }

  /**
   * Return the method unique instance.
   *
   * @return The instance.
   */
  public static InterestRateFutureOptionPremiumTransactionBlackSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the method to compute the underlying security price and price curve sensitivity.
   *
   * @return The method.
   */
  @Override
  public InterestRateFutureOptionPremiumSecurityBlackSmileMethod getSecurityMethod() {
    return (InterestRateFutureOptionPremiumSecurityBlackSmileMethod) super.getSecurityMethod();
  }

  /**
   * Computes the present value of a transaction from the future price and curve/volatility data.
   *
   * @param transaction
   *          The future option transaction.
   * @param blackData
   *          The Black volatility and multi-curves provider.
   * @param priceFuture
   *          The price of the underlying future.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromFuturePrice(final InterestRateFutureOptionPremiumTransaction transaction,
      final BlackSTIRFuturesProviderInterface blackData,
      final double priceFuture) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(blackData, "Black / multi-curves provider");
    final double priceSecurity = getSecurityMethod().priceFromFuturePrice(transaction.getUnderlyingOption(),
        blackData, priceFuture);
    final MultipleCurrencyAmount priceTransaction = presentValueFromPrice(transaction, blackData, priceSecurity);
    return priceTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   *
   * @param transaction
   *          The future option transaction.
   * @param blackData
   *          The Black volatility and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public SurfaceValue presentValueBlackSensitivity(final InterestRateFutureOptionPremiumTransaction transaction,
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(blackData, "Black / multi-curves provider");
    SurfaceValue securitySensitivity = getSecurityMethod().priceBlackSensitivity(transaction.getUnderlyingOption(), blackData);
    securitySensitivity = SurfaceValue.multiplyBy(securitySensitivity,
        transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
            * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

  /**
   * Computes the present value gamma of a transaction. This is with respect to futures rate.
   *
   * @param transaction
   *          The future option transaction.
   * @param blackData
   *          The Black volatility and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public double presentValueGamma(final InterestRateFutureOptionPremiumTransaction transaction,
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(blackData, "Black / multi-curves provider");
    final double securityGamma = getSecurityMethod().priceGamma(transaction.getUnderlyingOption(), blackData);
    final double txnGamma = securityGamma * transaction.getQuantity()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnGamma;
  }

  /**
   * Computes the present value delta of a transaction. This is with respect to futures price.
   *
   * @param transaction
   *          The future option transaction.
   * @param blackData
   *          The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public double presentValueDelta(final InterestRateFutureOptionPremiumTransaction transaction,
      final BlackSTIRFuturesProviderInterface blackData) {
    final double securityDelta = getSecurityMethod().priceDelta(transaction.getUnderlyingOption(), blackData);
    final double txnDelta = securityDelta
        * transaction.getQuantity()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnDelta;
  }

  /**
   * Computes the present value volatility sensitivity of a transaction.
   *
   * @param transaction
   *          The future option transaction.
   * @param blackData
   *          The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public double presentValueVega(final InterestRateFutureOptionPremiumTransaction transaction,
      final BlackSTIRFuturesProviderInterface blackData) {
    final double securitySensitivity = getSecurityMethod().priceVega(transaction.getUnderlyingOption(), blackData);
    final double txnSensitivity = securitySensitivity
        * transaction.getQuantity()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnSensitivity;
  }

  /**
   * Computes the present value theta of a transaction.
   *
   * @param transaction
   *          the future option transaction.
   * @param blackData
   *          the curve and Black volatility data.
   * @return the present value theta.
   */
  public double presentValueTheta(final InterestRateFutureOptionPremiumTransaction transaction,
      final BlackSTIRFuturesProviderInterface blackData) {
    final double securitySensitivity = getSecurityMethod().priceTheta(transaction.getUnderlyingOption(), blackData);
    final double txnSensitivity = securitySensitivity
        * transaction.getQuantity()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return txnSensitivity;
  }

  /**
   * Interpolates on the Black Volatility Surface at expiry and strike of option transaction.
   *
   * @param optionTransaction
   *          InterestRateFutureOptionMarginTransaction
   * @param blackData
   *          YieldCurveWithBlackSwaptionBundle
   * @return Lognormal Implied Volatility
   */
  public Double impliedVolatility(final InterestRateFutureOptionPremiumTransaction optionTransaction,
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(optionTransaction, "optionTransaction");
    return getSecurityMethod().impliedVolatility(optionTransaction.getUnderlyingOption(), blackData);
  }

}
