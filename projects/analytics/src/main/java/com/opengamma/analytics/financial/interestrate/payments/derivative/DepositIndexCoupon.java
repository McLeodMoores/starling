/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Interface for coupons with deposit-like indices.
 *
 * @param <I>
 *          The index type.
 */
public interface DepositIndexCoupon<I extends IndexDeposit> {

  /**
   * Gets the currency.
   *
   * @return the currency
   */
  Currency getCurrency();

  /**
   * Gets the notional.
   *
   * @return the notional
   */
  double getNotional();

  /**
   * Gets the payment time in years.
   *
   * @return the payment time
   */
  double getPaymentTime();

  /**
   * Gets the payment year fraction (as defined by a day-count convention).
   *
   * @return the payment year fraction
   */
  double getPaymentYearFraction();

  /**
   * Gets the reference index.
   *
   * @return the index
   */
  I getIndex();

  /**
   * Accepts a visitor.
   *
   * @param <S>
   *          the type of the data
   * @param <T>
   *          the type of the result
   * @param visitor
   *          the visitor, not null
   * @param data
   *          any data required
   * @return the result
   */
  <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data);

  /**
   * Accepts a visitor.
   *
   * @param <T>
   *          the type of the result
   * @param visitor
   *          the visitor, not null
   * @return the result
   */
  <T> T accept(InstrumentDerivativeVisitor<?, T> visitor);
}
