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
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackFlatMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexFlatProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the sensitivity of the present value to the nodes of the curve(s) used in pricing. The underlying pricing
 * model is a Black model without smile.
 */
public final class PresentValueCurveSensitivityForexBlackFlatCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexFlatProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityForexBlackFlatCalculator INSTANCE = new PresentValueCurveSensitivityForexBlackFlatCalculator();

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityForexBlackFlatCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityForexBlackFlatCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexFlatProviderInterface marketData) {
    return ForexOptionVanillaBlackFlatMethod.getInstance().presentValueCurveSensitivity(option, marketData);
  }

}
