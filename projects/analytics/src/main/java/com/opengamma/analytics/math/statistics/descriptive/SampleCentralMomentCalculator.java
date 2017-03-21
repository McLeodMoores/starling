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
 * Calculates the $n^th$ sample central moment of a series of data.
 * <p>
 * The sample central moment $\mu_n$ of a series of data $x_1, x_2, \dots, x_s$ is given by:
 * $$
 * \begin{align*}
 * \mu_n = \frac{1}{s}\sum_{i=1}^s (x_i - \overline{x})^n
 * \end{align*}
 * $$
 * where $\overline{x}$ is the mean.
 */
@DescriptiveStatistic(name = SampleCentralMomentCalculator.NAME, aliases = "Sample Central Moment")
public class SampleCentralMomentCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of the calculator.
   */
  public static final String NAME = "SampleCentralMoment";

  /** The number of the moment */
  private final int _n;

  /**
   * @param n  the degree of the moment to calculate, cannot be negative
   */
  public SampleCentralMomentCalculator(final int n) {
    ArgumentChecker.isTrue(n >= 0, "n must be >= 0");
    _n = n;
  }

  /**
   * @param x The array of data, not null. Must contain at least two data points.
   * @return The sample central moment.
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.isTrue(x.length >= 2, "Need at least 2 data points to calculate central moment");
    if (_n == 0) {
      return 1.;
    }
    final double mu = DescriptiveStatisticsFactory.of(MeanCalculator.NAME).evaluate(x);
    double sum = 0;
    for (final Double d : x) {
      sum += Math.pow(d - mu, _n);
    }
    return sum / (x.length - 1);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
