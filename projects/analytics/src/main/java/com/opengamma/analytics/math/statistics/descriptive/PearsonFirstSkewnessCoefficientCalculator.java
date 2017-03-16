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
 * Given a series of data $x_1, x_2, \dots, x_n$ with mean $\overline{x}$, mode $m$
 * and standard deviation $\sigma$, the Pearson first skewness coefficient is given by
 * $$
 * \begin{align*}
 * \text{skewness} = \frac{3(\overline{x} - m)}{\sigma}
 * \end{align*}
 * $$.
 */
@DescriptiveStatistic(name = PearsonFirstSkewnessCoefficientCalculator.NAME, aliases = "Pearson First Skewness Coefficient")
public class PearsonFirstSkewnessCoefficientCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "PearsonFirstSkewnessCoefficient";

  /**
   * @param x  the array of data, not null. Must contain at least two data points
   * @return  the Pearson first skewness coefficient
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.isTrue(x.length > 1, "Need at least two data points to calculate Pearson first skewness coefficient");
    return 3 * (DescriptiveStatisticsFactory.of(MeanCalculator.NAME).evaluate(x) - DescriptiveStatisticsFactory.of(ModeCalculator.NAME).evaluate(x))
        / DescriptiveStatisticsFactory.of(SampleStandardDeviationCalculator.NAME).evaluate(x);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
