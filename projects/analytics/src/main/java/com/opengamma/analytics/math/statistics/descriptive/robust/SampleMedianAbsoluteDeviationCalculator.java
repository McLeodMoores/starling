/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.statistics.descriptive.robust;

import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatistic;
import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsCalculator;
import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsFactory;
import com.opengamma.analytics.math.statistics.descriptive.MedianCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * The median absolute deviation of a sample is a robust estimator of the variability of the data. It
 * is the median of the absolute deviations of the data points from the median:
 * $$
 * \mathrm{MAD} = \mathrm{median}|x_i - median(x)|
 * $$
 */
@DescriptiveStatistic(name = SampleMedianAbsoluteDeviationCalculator.NAME, aliases = {"Sample Median Absolute Deviation", "MAD" })
public class SampleMedianAbsoluteDeviationCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "SampleMedianAbsoluteDeviation";

  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    final int n = x.length;
    ArgumentChecker.isTrue(n > 1, "Need at least two data points to calculate MAD");
    final DescriptiveStatisticsCalculator medianCalculator = DescriptiveStatisticsFactory.of(MedianCalculator.NAME);
    final double median = medianCalculator.evaluate(x);
    final double[] diff = new double[n];
    for (int i = 0; i < n; i++) {
      diff[i] = Math.abs(x[i] - median);
    }
    return medianCalculator.evaluate(diff);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
