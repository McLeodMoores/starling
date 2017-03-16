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
 * Calculates the arithmetic mean of a series of data.
 * <p>
 * The arithmetic mean $\mu$ of a series of elements $x_1, x_2, \dots, x_n$ is given by:
 * $$
 * \begin{align*}
 * \mu = \frac{1}{n}\left({\sum\limits_{i=1}^n x_i}\right)
 * \end{align*}
 * $$
 */
@DescriptiveStatistic(name = MeanCalculator.NAME)
public class MeanCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "Mean";

  /**
   * @param x  the array of data, not null or empty
   * @return  the arithmetic mean
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notEmpty(x, "x");
    if (x.length == 1) {
      return x[0];
    }
    double sum = 0;
    for (final Double d : x) {
      sum += d;
    }
    return sum / x.length;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
