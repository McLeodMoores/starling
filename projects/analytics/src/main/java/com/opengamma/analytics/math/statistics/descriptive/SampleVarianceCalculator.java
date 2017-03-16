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
 * Calculates the sample variance of a series of data.
 * <p>
 * The unbiased sample variance $\mathrm{var}$ of a series $x_1, x_2, \dots, x_n$ is given by:
 * $$
 * \begin{align*}
 * \text{var} = \frac{1}{n-1}\sum_{i=1}^{n}(x_i - \overline{x})^2
 * \end{align*}
 * $$
 * where $\overline{x}$ is the sample mean. For the population variance, see {@link PopulationVarianceCalculator}.
 */
@DescriptiveStatistic(name = SampleVarianceCalculator.NAME, aliases = "Sample Variance")
public class SampleVarianceCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "SampleVariance";

  /**
   * @param x  the array of data, not null, must contain at least two elements
   * @return  the sample variance
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.isTrue(x.length >= 2, "Need at least two points to calculate the sample variance");
    final Double mean = DescriptiveStatisticsFactory.of(MeanCalculator.NAME).evaluate(x);
    double sum = 0;
    for (final Double value : x) {
      final double diff = value - mean;
      sum += diff * diff;
    }
    final int n = x.length;
    return sum / (n - 1);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
