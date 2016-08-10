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
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the sensitivity of the present value to the nodes of the curve(s) used in pricing. The underlying pricing
 * model is a Black model with smile.
 */
public final class PresentValueCurveSensitivityForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityForexBlackSmileCalculator INSTANCE = new PresentValueCurveSensitivityForexBlackSmileCalculator();

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityForexBlackSmileCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionVanillaBlackSmileMethod.getInstance().presentValueCurveSensitivity(option, marketData);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexNonDeliverableOption(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    return ForexNonDeliverableOptionBlackSmileMethod.getInstance().presentValueCurveSensitivity(option, marketData);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionDigital(final ForexOptionDigital option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionDigitalBlackSmileMethod.getInstance().presentValueCurveSensitivity(option, marketData);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionSingleBarrier(final ForexOptionSingleBarrier option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionSingleBarrierBlackMethod.getInstance().presentValueCurveSensitivity(option, marketData);
  }

}
