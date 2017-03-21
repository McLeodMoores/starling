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
import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * The sample skewness gives a measure of the asymmetry of the probability
 * distribution of a variable. For a series of data $x_1, x_2, \dots, x_n$, an
 * unbiased estimator of the sample skewness is
 * $$
 * \begin{align*}
 * \mu_3 = \frac{\sqrt{n(n-1)}}{n-2}\frac{\frac{1}{n}\sum_{i=1}^n
 *      (x_i - \overline{x})^3}{\left(\frac{1}{n}\sum_{i=1}^n (x_i - \overline{x})^2\right)^\frac{3}{2}}
 * \end{align*}
 * $$
 * where $\overline{x}$ is the sample mean.
 */
@DescriptiveStatistic(name = SampleSkewnessCalculator.NAME, aliases = "Sample Skewness")
public class SampleSkewnessCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "SampleSkewness";

  /**
   * @param x The array of data, not null, must contain at least three data points
   * @return The sample skewness
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.isTrue(x.length >= 3, "Need at least three points to calculate sample skewness");
    double sum = 0;
    double variance = 0;
    final double mean = DescriptiveStatisticsFactory.of(MeanCalculator.NAME).evaluate(x);
    for (final Double d : x) {
      final double diff = d - mean;
      variance += diff * diff;
      sum += diff * diff * diff;
    }
    final int n = x.length;
    variance /= n - 1;
    return Math.sqrt(n - 1.) * sum / (Math.pow(variance, 1.5) * Math.sqrt(n) * (n - 2));
  }

  @Override
  public String getName() {
    return NAME;
  }
}
