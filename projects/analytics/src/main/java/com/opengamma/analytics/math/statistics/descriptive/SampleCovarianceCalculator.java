/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the sample covariance of two series of data, $x_1, x_2, \dots, x_n$ and $y_1, y_2, \dots, y_n$.
 *
 * <p>
 * The sample covariance is given by:
 * $$
 * \begin{align*}
 * \text{cov} = \frac{1}{n-1}\sum_{i=1}^n (x_i - \overline{x})(y_i - \overline{y})
 * \end{align*}
 * $$
 * where $\overline{x}$ and $\overline{y}$ are the means of the two series.
 */
public class SampleCovarianceCalculator implements Function<double[], Double> {

  /**
   * @param x  the array of data, not null. The first and second elements must be arrays of data, neither of which is null or has less than two elements.
   * @return  the sample covariance
   */
  @Override
  public Double evaluate(final double[]... x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.isTrue(x.length > 1, "Need two data series to calculate the covariance");
    final double[] x1 = x[0];
    final double[] x2 = x[1];
    final int n = x1.length;
    ArgumentChecker.isTrue(x2.length == n, "The two series must be the same length");
    final DescriptiveStatisticsCalculator meanCalculator = DescriptiveStatisticsFactory.of(MeanCalculator.NAME);
    final double mean1 = meanCalculator.evaluate(x1);
    final double mean2 = meanCalculator.evaluate(x2);
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += (x1[i] - mean1) * (x2[i] - mean2);
    }
    return sum / (n - 1);
  }
}
