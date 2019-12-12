/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method for the pricing of interest rate future options with up-front premium.
 *
 * @param <DATA_TYPE>
 *          Data type. Extends ParameterProviderInterface.
 */
public abstract class InterestRateFutureOptionPremiumTransactionGenericMethod<DATA_TYPE extends ParameterProviderInterface> {

  /**
   * The method to compute the underlying security price and price curve sensitivity.
   */
  private final InterestRateFutureOptionPremiumSecurityGenericMethod<DATA_TYPE> _methodSecurity;

  /**
   * Constructor.
   *
   * @param methodSecurity
   *          The method to compute the underlying security price and price curve sensitivity.
   */
  public InterestRateFutureOptionPremiumTransactionGenericMethod(
      final InterestRateFutureOptionPremiumSecurityGenericMethod<DATA_TYPE> methodSecurity) {
    _methodSecurity = methodSecurity;
  }

  /**
   * Returns the method to compute the underlying security price and price curve sensitivity.
   *
   * @return The method.
   */
  public InterestRateFutureOptionPremiumSecurityGenericMethod<DATA_TYPE> getSecurityMethod() {
    return _methodSecurity;
  }

  /**
   * Compute the present value of a future option transaction from a quoted price.
   *
   * @param option
   *          The future option.
   * @param blackData
   *          The yield curves. Should contain the discounting and forward curves associated to the instrument.
   * @param price
   *          The option price to be used for the present value.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final InterestRateFutureOptionPremiumTransaction option,
      final DATA_TYPE blackData, final double price) {
    final MultipleCurrencyAmount premiumPV = option.getPremium().accept(PresentValueDiscountingCalculator.getInstance(),
        blackData.getMulticurveProvider());
    final MultipleCurrencyAmount optionPV = MultipleCurrencyAmount.of(option.getUnderlyingOption().getCurrency(),
        price * option.getQuantity() * option.getUnderlyingOption().getUnderlyingFuture().getNotional()
            * option.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor());
    return optionPV.plus(premiumPV);
  }

  /**
   * Computes the present value of a transaction.
   *
   * @param transaction
   *          The future option transaction.
   * @param data
   *          The data provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final InterestRateFutureOptionPremiumTransaction transaction, final DATA_TYPE data) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(data, "data provider");
    final double priceSecurity = _methodSecurity.price(transaction.getUnderlyingOption(), data);
    final MultipleCurrencyAmount pvTransaction = presentValueFromPrice(transaction, data, priceSecurity);
    return pvTransaction;
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   *
   * @param transaction
   *          The future option transaction.
   * @param data
   *          The data provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final InterestRateFutureOptionPremiumTransaction transaction,
      final DATA_TYPE data) {
    ArgumentChecker.notNull(transaction, "Transaction on option on STIR futures");
    ArgumentChecker.notNull(data, "data provider");
    final InterestRateFutureSecurity underlyingFuture = transaction.getUnderlyingOption().getUnderlyingFuture();
    final MulticurveSensitivity securitySensitivity = _methodSecurity.priceCurveSensitivity(transaction.getUnderlyingOption(), data);
    return MultipleCurrencyMulticurveSensitivity.of(
        transaction.getUnderlyingOption().getCurrency(),
        securitySensitivity
            .multipliedBy(transaction.getQuantity() * underlyingFuture.getNotional() * underlyingFuture.getPaymentAccrualFactor()));
  }

}
