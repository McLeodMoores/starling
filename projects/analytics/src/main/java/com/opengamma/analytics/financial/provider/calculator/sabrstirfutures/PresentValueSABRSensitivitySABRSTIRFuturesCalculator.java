/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginSecuritySABRMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionSABRMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionPremiumSecuritySABRMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionPremiumTransactionSABRMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderInterface;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueSABRSensitivitySABRSTIRFuturesCalculator
    extends InstrumentDerivativeVisitorAdapter<SABRSTIRFuturesProviderInterface, PresentValueSABRSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRSensitivitySABRSTIRFuturesCalculator INSTANCE = new PresentValueSABRSensitivitySABRSTIRFuturesCalculator();

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static PresentValueSABRSensitivitySABRSTIRFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueSABRSensitivitySABRSTIRFuturesCalculator() {
  }

  // ----- Futures ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitInterestRateFutureOptionMarginTransaction(
      final InterestRateFutureOptionMarginTransaction futureOption,
      final SABRSTIRFuturesProviderInterface sabr) {
    return InterestRateFutureOptionMarginTransactionSABRMethod
        .getInstance().presentValueSABRSensitivity(futureOption, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitInterestRateFutureOptionPremiumTransaction(
      final InterestRateFutureOptionPremiumTransaction futureOption,
      final SABRSTIRFuturesProviderInterface sabr) {
    return InterestRateFutureOptionPremiumTransactionSABRMethod.getInstance()
        .presentValueSABRSensitivity(futureOption, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitInterestRateFutureOptionMarginSecurity(
      final InterestRateFutureOptionMarginSecurity futureOption,
      final SABRSTIRFuturesProviderInterface sabr) {
    return InterestRateFutureOptionMarginSecuritySABRMethod
        .getInstance().priceSABRSensitivity(futureOption, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitInterestRateFutureOptionPremiumSecurity(
      final InterestRateFutureOptionPremiumSecurity futureOption,
      final SABRSTIRFuturesProviderInterface sabr) {
    return InterestRateFutureOptionPremiumSecuritySABRMethod.getInstance()
        .priceSABRSensitivity(futureOption, sabr);
  }
}
