/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 *
 * @param <T>
 *          The type of the result returned
 */
public interface PDELocalVolatilityCalculator<T> {

  T getResult(LocalVolatilitySurfaceMoneyness localVolatility, ForwardCurve forwardCurve, EuropeanVanillaOption option, YieldAndDiscountCurve discountingCurve);

  T getResult(LocalVolatilitySurfaceStrike localVolatility, ForwardCurve forwardCurve, EuropeanVanillaOption option, YieldAndDiscountCurve discountingCurve);
}
