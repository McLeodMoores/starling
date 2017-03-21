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

import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatistic;
import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the geometric mean of a series of data.
 * <p>
 * The geometric mean $\mu$ of a series of elements $x_1, x_2, \dots, x_n$ is given by:
 * $$
 * \begin{align*}
 * \mu = \left({\prod\limits_{i=1}^n x_i}\right)^{\frac{1}{n}}
 * \end{align*}
 * $$
 */
@DescriptiveStatistic(name = GeometricMeanCalculator.NAME, aliases = "Geometric Mean")
public class GeometricMeanCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "GeometricMean";

  /**
   * @param x  the array of data, not null or empty
   * @return  the geometric mean
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notEmpty(x, "x");
    final int n = x.length;
    double mult = x[0];
    for (int i = 1; i < n; i++) {
      mult *= x[i];
    }
    return Math.pow(mult, 1. / n);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
