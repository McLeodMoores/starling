/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the value gamma (second order derivative with respect to the spot rate multiplied by the foreign notional)
 * for Forex derivatives in the Black (Garman-Kohlhagen) world. The delta is calculated with respect to the direct quote
 * i.e. 1 foreign currency = x domestic currency.
 */
public class ValueGammaForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ValueGammaForexBlackSmileCalculator INSTANCE = new ValueGammaForexBlackSmileCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ValueGammaForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ValueGammaForexBlackSmileCalculator() {
  }

  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    ArgumentChecker.notNull(option, "option");
    final double sign = option.isLong() ? 1.0 : -1.0;
    return ForexOptionVanillaBlackSmileMethod.getInstance().gammaRelative(option, marketData, true) * sign
        * Math.abs(option.getUnderlyingForex().getPaymentCurrency1().getAmount());
  }
}
