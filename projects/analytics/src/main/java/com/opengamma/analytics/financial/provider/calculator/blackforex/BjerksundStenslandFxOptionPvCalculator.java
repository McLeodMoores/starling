/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.BjerksundStenslandVanillaFxOptionCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of American FX options using the Bjerksund-Stensland model with volatility smile.
 */
public final class BjerksundStenslandFxOptionPvCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final BjerksundStenslandFxOptionPvCalculator INSTANCE = new BjerksundStenslandFxOptionPvCalculator();

  /**
   * Constructor.
   */
  private BjerksundStenslandFxOptionPvCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static BjerksundStenslandFxOptionPvCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface blackSmile) {
    return BjerksundStenslandVanillaFxOptionCalculator.presentValue(option, blackSmile);
  }

}
