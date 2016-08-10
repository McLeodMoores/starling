/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexNonDeliverableOptionBlackSmileMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionDigitalBlackSmileMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the currency exposure of FX options using the Garman-Kohlhagan model with a volatility smile.
 */
public final class CurrencyExposureForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final CurrencyExposureForexBlackSmileCalculator INSTANCE = new CurrencyExposureForexBlackSmileCalculator();

  /**
   * Constructor.
   */
  private CurrencyExposureForexBlackSmileCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CurrencyExposureForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionVanillaBlackSmileMethod.getInstance().currencyExposure(option, marketData);
  }

  @Override
  public MultipleCurrencyAmount visitForexNonDeliverableOption(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    return ForexNonDeliverableOptionBlackSmileMethod.getInstance().currencyExposure(option, marketData);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionDigital(final ForexOptionDigital option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionDigitalBlackSmileMethod.getInstance().currencyExposure(option, marketData);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionSingleBarrierBlackMethod.getInstance().currencyExposure(option, marketData);
  }

}
