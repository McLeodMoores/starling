/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the Fisher kurtosis (a.k.a. excess kurtosis) of a lognormal distribution using the standard
 * deviation and time.
 * $$
 * \begin{align*}
 * y &= \sigma^2 t\\
 * \kappa &= y^8 + 6y^3 + 15y^4 + 16y^2
 * \end{align*}
 * $$
 */
@LognormalStatistic(aliases = "Lognormal Fisher Kurtosis From Volatility")
public class LognormalFisherKurtosisFromVolatilityCalculator extends LognormalStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "LognormalFisherKurtosisFromVolatility";

  @Override
  public Double evaluate(final Double sigma, final Double t) {
    ArgumentChecker.notNull(sigma, "sigma");
    ArgumentChecker.notNull(t, "t");
    final double y = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    final double y2 = y * y;
    return y2 * (16 + y2 * (15 + y2 * (6 + y2)));
  }

  @Override
  public String getName() {
    return NAME;
  }
}
