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

/**
 * Calculates the population variance of a series of data.
 * <p>
 * The unbiased population variance $\mathrm{var}$ of a series $x_1, x_2, \dots, x_n$ is given by:
 * $$
 * \begin{align*}
 * \text{var} = \frac{1}{n}\sum_{i=1}^{n}(x_i - \overline{x})^2
 * \end{align*}
 * $$
 * where $\overline{x}$ is the sample mean. For the sample variance, see {@link SampleVarianceCalculator}.
 */
@DescriptiveStatistic(name = PopulationVarianceCalculator.NAME, aliases = "Population Variance")
public class PopulationVarianceCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "PopulationVariance";

  /**
   * @param x  the array of data, not null, must contain at least two elements
   * @return  the population variance
   */
  @Override
  public Double evaluate(final double[] x) {
    final double variance = DescriptiveStatisticsFactory.of(SampleVarianceCalculator.NAME).evaluate(x);
    final int n = x.length;
    return variance * (n - 1) / n;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
