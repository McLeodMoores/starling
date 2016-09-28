/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculates the percentage (or spot) gamma, the second order derivative with respect to the spot rate multiplied by the spot rate,
 * for Forex derivatives in the Black (Garman-Kohlhagen) world. The gamma is returned as the sensitivity with respect to the direct
 * quote, i.e. 1 foreign = x domestic.
 */
public class PercentageGammaForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, CurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PercentageGammaForexBlackSmileCalculator INSTANCE = new PercentageGammaForexBlackSmileCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PercentageGammaForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PercentageGammaForexBlackSmileCalculator() {
  }

  @Override
  public CurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionVanillaBlackSmileMethod.getInstance().gammaSpot(option, marketData, true);
  }

}
