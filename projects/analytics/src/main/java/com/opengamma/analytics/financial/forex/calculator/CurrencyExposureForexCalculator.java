/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the currency exposure for Forex derivatives.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class CurrencyExposureForexCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final CurrencyExposureForexCalculator s_instance = new CurrencyExposureForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CurrencyExposureForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  public CurrencyExposureForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FXSWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visitForex(final Forex derivative, final YieldCurveBundle data) {
    return METHOD_FOREX.currencyExposure(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexSwap(final ForexSwap derivative, final YieldCurveBundle data) {
    return METHOD_FXSWAP.currencyExposure(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final YieldCurveBundle data) {
    return METHOD_NDF.currencyExposure(derivative, data);
  }

}
