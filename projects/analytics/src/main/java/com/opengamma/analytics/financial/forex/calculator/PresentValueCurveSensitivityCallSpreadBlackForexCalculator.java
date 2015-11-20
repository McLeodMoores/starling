/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityMCSCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.method.ForexOptionDigitalCallSpreadBlackMethod;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the present value curve sensitivity for Forex digital options using vanilla call spread with Black formula for underlying vanilla.
 * To compute the curve sensitivity, the Black volatility is kept constant; the volatility is not recomputed for curve and forward changes.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class PresentValueCurveSensitivityCallSpreadBlackForexCalculator extends PresentValueCurveSensitivityMCSCalculator {

  /**
   * The methods used by the different instruments.
   */
  private final ForexOptionDigitalCallSpreadBlackMethod _methodFxOptionDigital;

  /**
   * Private constructor.
   * @param spread The relative spread used in the call-spread pricing. The call spread strikes are (for an original strike K), K*(1-spread) and K*(1+spread).
   */
  public PresentValueCurveSensitivityCallSpreadBlackForexCalculator(final double spread) {
    _methodFxOptionDigital = new ForexOptionDigitalCallSpreadBlackMethod(spread);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    return _methodFxOptionDigital.presentValueCurveSensitivity(derivative, data);
  }

}
