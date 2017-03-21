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
 * Given a series of data $x_1, x_2, \dots, x_n$ with mean $\overline{x}$, median $m$
 * and standard deviation $\sigma$, the Pearson second skewness coefficient is given by
 * $$
 * \begin{align*}
 * \text{skewness} = \frac{3(\overline{x} - m)}{\sigma}
 * \end{align*}
 * $$.
 */
@DescriptiveStatistic(name = PearsonSecondSkewnessCoefficientCalculator.NAME, aliases = "Pearson Second Skewness Coefficient")
public class PearsonSecondSkewnessCoefficientCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "PearsonSecondSkewnessCoefficient";

  /**
   * @param x  the array of data, not null. Must contain at least two data points
   * @return  the Pearson second skewness coefficient
   */
  @Override
  public Double evaluate(final double[] x) {
    return 3 * (DescriptiveStatisticsFactory.of(MeanCalculator.NAME).evaluate(x) - DescriptiveStatisticsFactory.of(MedianCalculator.NAME).evaluate(x))
        / DescriptiveStatisticsFactory.of(SampleStandardDeviationCalculator.NAME).evaluate(x);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
