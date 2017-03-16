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

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the quartile skewness coefficient, which is given by:
 * $$
 * \begin{align*}
 * \text{QS} = \frac{Q_1 - 2Q_2 + Q_3}{Q_3 - Q_1}
 * \end{align*}
 * $$
 * where $Q_1$, $Q_2$ and $Q_3$ are the first, second and third quartiles.
 * <p>
 * The quartile skewness coefficient is also known as the Bowley skewness.
 */
@DescriptiveStatistic(name = QuartileSkewnessCalculator.NAME, aliases = "Quartile Skewness")
public class QuartileSkewnessCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "QuartileSkewness";

  /**
   * @param x  the array of data, not null. Must contain at least three points.
   * @return  the quartile skewness.
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    final int n = x.length;
    ArgumentChecker.isTrue(n >= 3, "Need at least three points to calculate interquartile range");
    if (n == 3) {
      return (x[2] - 2 * x[1] + x[0]) / 2.;
    }
    final double[] copy = Arrays.copyOf(x, n);
    Arrays.sort(copy);
    double[] lower, upper;
    if (n % 2 == 0) {
      lower = Arrays.copyOfRange(copy, 0, n / 2);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    } else {
      lower = Arrays.copyOfRange(copy, 0, n / 2 + 1);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    }
    final DescriptiveStatisticsCalculator medianCalculator = DescriptiveStatisticsFactory.of(MedianCalculator.NAME);
    final double q1 = medianCalculator.evaluate(lower);
    final double q2 = medianCalculator.evaluate(x);
    final double q3 = medianCalculator.evaluate(upper);
    return (q1 - 2 * q2 + q3) / (q3 - q1);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
