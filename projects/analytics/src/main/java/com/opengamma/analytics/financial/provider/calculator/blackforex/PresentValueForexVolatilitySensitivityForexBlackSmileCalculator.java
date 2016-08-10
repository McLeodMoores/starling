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
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.forex.provider.ForexNonDeliverableOptionBlackSmileMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionDigitalBlackSmileMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;

/**
 * Calculates the sensitivity of the present value to the implied volatility used in pricing. The underlying pricing
 * model is a Black model without smile.
 */
public final class PresentValueForexVolatilitySensitivityForexBlackSmileCalculator extends
    InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, PresentValueForexBlackVolatilitySensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueForexVolatilitySensitivityForexBlackSmileCalculator INSTANCE = new PresentValueForexVolatilitySensitivityForexBlackSmileCalculator();

  /**
   * Constructor.
   */
  private PresentValueForexVolatilitySensitivityForexBlackSmileCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueForexVolatilitySensitivityForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface blackSmile) {
    return ForexOptionVanillaBlackSmileMethod.getInstance().presentValueBlackVolatilitySensitivity(option, blackSmile);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexNonDeliverableOption(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface blackSmile) {
    return ForexNonDeliverableOptionBlackSmileMethod.getInstance().presentValueBlackVolatilitySensitivity(option, blackSmile);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionDigital(final ForexOptionDigital option, final BlackForexSmileProviderInterface blackSmile) {
    return ForexOptionDigitalBlackSmileMethod.getInstance().presentValueBlackVolatilitySensitivity(option, blackSmile);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionSingleBarrier(final ForexOptionSingleBarrier option, final BlackForexSmileProviderInterface blackSmile) {
    return ForexOptionSingleBarrierBlackMethod.getInstance().presentValueBlackVolatilitySensitivity(option, blackSmile);
  }

}
