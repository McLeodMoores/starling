/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.provider.ForexNonDeliverableOptionBlackSmileMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionDigitalBlackSmileMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;

/**
 * Calculates the bucketed vega matrix (first order derivative with respect to the implied volatility) for Forex derivatives in the
 * Black (Garman-Kohlhagen) world. The matrix axes are delta and time to expiry.
 */
public class BucketedVegaForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, PresentValueForexBlackVolatilityNodeSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final BucketedVegaForexBlackSmileCalculator INSTANCE = new BucketedVegaForexBlackSmileCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static BucketedVegaForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  BucketedVegaForexBlackSmileCalculator() {
  }

  @Override
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionVanillaBlackSmileMethod.getInstance().presentValueBlackVolatilityNodeSensitivity(option, marketData);
  }

  @Override
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle visitForexOptionDigital(final ForexOptionDigital option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionDigitalBlackSmileMethod.getInstance().presentValueBlackVolatilityNodeSensitivity(option, marketData);
  }

  @Override
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle visitForexNonDeliverableOption(final ForexNonDeliverableOption option, final BlackForexSmileProviderInterface marketData) {
    return ForexNonDeliverableOptionBlackSmileMethod.getInstance().presentValueVolatilityNodeSensitivity(option, marketData);
  }

  @Override
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle visitForexOptionSingleBarrier(final ForexOptionSingleBarrier option, final BlackForexSmileProviderInterface marketData) {
    return ForexOptionSingleBarrierBlackMethod.getInstance().presentValueBlackVolatilityNodeSensitivity(option, marketData);
  }
}
