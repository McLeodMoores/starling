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
 * The sample Fisher kurtosis gives a measure of how heavy the tails of a distribution are with respect to the normal distribution (which
 * has a Fisher kurtosis of zero). An estimator of the kurtosis is
 * $$
 * \begin{align*}
 * \mu_4 = \frac{(n+1)n}{(n-1)(n-2)(n-3)}\frac{\sum_{i=1}^n (x_i - \overline{x})^4}{\mu_2^2} - 3\frac{(n-1)^2}{(n-2)(n-3)}
 * \end{align*}
 * $$
 * where $\overline{x}$ is the sample mean and $\mu_2$ is the unbiased estimator of the population variance.
 * <p>
 * Fisher kurtosis is also known as the _excess kurtosis_.
 */
@DescriptiveStatistic(name = SampleFisherKurtosisCalculator.NAME, aliases = "Sample Fisher Kurtosis")
public class SampleFisherKurtosisCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "SampleFisherKurtosis";

  /**
   * @param x  the array of data, not null. Must contain at least four data points.
   * @return  the sample Fisher kurtosis
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.isTrue(x.length >= 4, "Need at least four points to calculate kurtosis");
    double sum = 0;
    final double mean = DescriptiveStatisticsFactory.of(MeanCalculator.NAME).evaluate(x);
    double variance = 0;
    for (final Double d : x) {
      final double diff = d - mean;
      final double diffSq = diff * diff;
      variance += diffSq;
      sum += diffSq * diffSq;
    }
    final int n = x.length;
    final double n1 = n - 1;
    final double n2 = n1 - 1;
    variance /= n1;
    return n * (n + 1.) * sum / (n1 * n2 * (n - 3.) * variance * variance) - 3 * n1 * n1 / (n2 * (n - 3.));
  }

  @Override
  public String getName() {
    return NAME;
  }
}
