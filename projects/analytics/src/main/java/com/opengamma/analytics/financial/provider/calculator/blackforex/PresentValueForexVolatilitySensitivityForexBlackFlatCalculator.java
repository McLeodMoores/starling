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

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackFlatMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexFlatProviderInterface;

/**
 * Calculates the sensitivity of the present value to the implied volatility used in pricing. The underlying pricing
 * model is a Black model without smile.
 */
public final class PresentValueForexVolatilitySensitivityForexBlackFlatCalculator extends
    InstrumentDerivativeVisitorAdapter<BlackForexFlatProviderInterface, PresentValueForexBlackVolatilitySensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueForexVolatilitySensitivityForexBlackFlatCalculator INSTANCE =
      new PresentValueForexVolatilitySensitivityForexBlackFlatCalculator();

  /**
   * Constructor.
   */
  private PresentValueForexVolatilitySensitivityForexBlackFlatCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueForexVolatilitySensitivityForexBlackFlatCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionVanilla(final ForexOptionVanilla option,
      final BlackForexFlatProviderInterface blackSmile) {
    return ForexOptionVanillaBlackFlatMethod.getInstance().presentValueBlackVolatilitySensitivity(option, blackSmile);
  }

}
