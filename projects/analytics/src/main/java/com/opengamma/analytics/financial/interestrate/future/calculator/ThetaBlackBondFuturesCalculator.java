/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesOptionMarginSecurityBlackBondFuturesMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;

/**
 * Calculator for bond future option's theta.
 */
public final class ThetaBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /**
   * The singleton.
   */
  private static final ThetaBlackBondFuturesCalculator INSTANCE = new ThetaBlackBondFuturesCalculator();

  /**
   * Returns a singleton of the calculator.
   * 
   * @return the calculator.
   */
  public static ThetaBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Singleton constructor.
   */
  private ThetaBlackBondFuturesCalculator() {
  }

  /**
   * Pricing method for theta.
   */
  private static final BondFuturesOptionMarginSecurityBlackBondFuturesMethod METHOD = BondFuturesOptionMarginSecurityBlackBondFuturesMethod
      .getInstance();

  @Override
  public Double visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity option,
      final BlackBondFuturesProviderInterface data) {
    return METHOD.theta(option, data);
  }

  @Override
  public Double visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction option,
      final BlackBondFuturesProviderInterface data) {
    return METHOD.theta(option.getUnderlyingSecurity(), data);
  }
}
