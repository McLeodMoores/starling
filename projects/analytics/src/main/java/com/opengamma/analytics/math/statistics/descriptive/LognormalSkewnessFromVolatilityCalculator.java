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
 * Calculates the skewness of a lognormal distribution using the standard deviation and time.
 * $$
 * \begin{align*}
 * y &= \sqrt{\exp{\sigma^2 t} - 1}\\
 * \tau &= y^3 + 3y
 * \end{align*}
 * $$
 */
@LognormalStatistic(aliases = "Lognormal Skewness From Volatility")
public class LognormalSkewnessFromVolatilityCalculator extends LognormalStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "LognormalSkewnessFromVolatility";

  @Override
  public Double evaluate(final Double sigma, final Double t) {
    ArgumentChecker.notNull(sigma, "sigma");
    ArgumentChecker.notNull(t, "t");
    final double y = Math.sqrt(Math.exp(sigma * sigma * t) - 1);
    return y * (3 + y * y);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
