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
 * Calculates the $n^th$ normalized central moment of a series of data. Given
 * the $n^th$ central moment $\mu_n$ of a series of data with standard
 * deviation $\sigma$, the normalized central moment is given by:
 * $$
 * \begin{align*}
 * \mu_n' = \frac{\mu_n}{\sigma^n}
 * \end{align*}
 * $$
 * The normalization gives a scale-invariant, dimensionless quantity. The
 * normalized central moment is also known as the <i>standardized moment</i>.
 */
@DescriptiveStatistic(name = SampleNormalizedCentralMomentCalculator.NAME, aliases = "Sample Normalized Central Moment")
public class SampleNormalizedCentralMomentCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of the calculator.
   */
  public static final String NAME = "SampleNormalizedCentralMoment";

  /** The degree */
  private final int _n;
  /** The moment calculator */
  private final DescriptiveStatisticsCalculator _momentCalculator;

  /**
   * @param n  the degree of the moment of calculate, cannot be negative
   */
  public SampleNormalizedCentralMomentCalculator(final int n) {
    _n = n;
    _momentCalculator = new SampleCentralMomentCalculator(n);
  }

  /**
   * @param x  the array of data, not null. Must contain at least two data points.
   * @return  the normalized sample central moment
   */
  @Override
  public Double evaluate(final double[] x) {
    if (_n == 0) {
      return 1.;
    }
    return _momentCalculator.evaluate(x) / Math.pow(DescriptiveStatisticsFactory.of(SampleStandardDeviationCalculator.NAME).evaluate(x), _n);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
