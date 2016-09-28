/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.BjerksundStenslandVanillaFxOptionCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the sensitivity of the present value to the nodes of the curve(s) using in pricing. The underlying
 * model used is the 2002 Bjerksund-Stensland model and the volatility surface assumes Black volatilities with
 * a smile.
 */
public final class BjerksundStenslandFxCurveSensitivityCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final BjerksundStenslandFxCurveSensitivityCalculator INSTANCE = new BjerksundStenslandFxCurveSensitivityCalculator();

  /**
   * Constructor.
   */
  private BjerksundStenslandFxCurveSensitivityCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static BjerksundStenslandFxCurveSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface blackSmile) {
    return BjerksundStenslandVanillaFxOptionCalculator.bucketedCurveSensitivities(option, blackSmile);
  }

}
