/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculates the value vega (first order cross-derivative with respect to the implied volatility)
 * for Forex derivatives in the Black (Garman-Kohlhagen) world.
 */
public class ValueVegaForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, CurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final ValueVegaForexBlackSmileCalculator INSTANCE = new ValueVegaForexBlackSmileCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ValueVegaForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ValueVegaForexBlackSmileCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();

  @Override
  public CurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    return METHOD_FXOPTIONVANILLA.vega(optionForex, smileMulticurves);
  }
}
