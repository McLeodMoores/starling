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
 * Calculates the Pearson kurtosis of a lognormal distribution using the standard deviation and time.
 * $$
 * \begin{align*}
 * \y &= \sigma^2 t\\
 * \kappa &= y^8 + 6y^3 + 15y^4 + 16y^2 + 3
 * \end{align*}
 * $$
 */
@LognormalStatistic(aliases = "Lognormal Pearson Kurtosis From Volatility")
public class LognormalPearsonKurtosisFromVolatilityCalculator extends LognormalStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "LognormalPearsonKurtosisFromVolatility";

  /** The Fisher kurtosis calculator */
  private static final LognormalFisherKurtosisFromVolatilityCalculator CALCULATOR = new LognormalFisherKurtosisFromVolatilityCalculator();

  @Override
  public Double evaluate(final Double sigma, final Double t) {
    ArgumentChecker.notNull(sigma, "sigma");
    ArgumentChecker.notNull(t, "t");
    return CALCULATOR.evaluate(sigma, t) + 3;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
