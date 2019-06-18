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

import java.util.Arrays;

import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatistic;
import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the median of a series of data.
 */
@DescriptiveStatistic(name = MedianCalculator.NAME)
public class MedianCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "Median";

  /**
   * @param x  the array of data, not null or empty
   * @return  the median
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notEmpty(x, "x");
    if (x.length == 1) {
      return x[0];
    }
    final double[] x1 = Arrays.copyOf(x, x.length);
    Arrays.sort(x1);
    final int mid = x1.length / 2;
    if (x1.length % 2 == 1) {
      return x1[mid];
    }
    return (x1[mid] + x1[mid - 1]) / 2.;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
