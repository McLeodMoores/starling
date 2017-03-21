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

import java.util.Arrays;

import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatistic;
import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsCalculator;
import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsFactory;
import com.opengamma.analytics.math.statistics.descriptive.MedianCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * The interquartile range is a measure of statistical dispersion of a set of data. It is the difference between the upper
 * and lower quartiles.
 */
@DescriptiveStatistic(name = InterquartileRangeCalculator.NAME, aliases = {"Interquartile Range", "IQR" })
public class InterquartileRangeCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "InterquartileRange";

  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    final int n = x.length;
    ArgumentChecker.isTrue(n > 3, "Need at least four points to calculate IQR");
    final double[] copy = Arrays.copyOf(x, n);
    Arrays.sort(copy);
    double[] lower, upper;
    if (n % 2 == 0) {
      lower = Arrays.copyOfRange(copy, 0, n / 2);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    } else {
      lower = Arrays.copyOfRange(copy, 0, n / 2 + 1);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    }
    final DescriptiveStatisticsCalculator medianCalculator = DescriptiveStatisticsFactory.of(MedianCalculator.NAME);
    return medianCalculator.evaluate(upper) - medianCalculator.evaluate(lower);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
