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

import com.opengamma.analytics.math.statistics.descriptive.DescriptiveStatistic;
import com.opengamma.analytics.math.statistics.descriptive.DescriptiveStatisticsCalculator;
import com.opengamma.analytics.math.statistics.descriptive.DescriptiveStatisticsFactory;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * The Winsorized mean is a robust estimator of the mean that replaces the outliers in a set of data
 * by the values at the specified percentile $$\gamma$$.
 */
@DescriptiveStatistic(name = WinsorizedMeanCalculator.NAME, aliases = "Winsorized Mean")
public class WinsorizedMeanCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "WinsorizedMean";

  /** The percentile */
  private final double _gamma;

  /**
   * Creates the calculator.
   * @param gamma  the percentile, must be between 0 and 1
   */
  public WinsorizedMeanCalculator(final double gamma) {
    ArgumentChecker.isTrue(gamma > 0 && gamma < 1, "Gamma must be between 0 and 1, have {}", gamma);
    _gamma = gamma > 0.5 ? 1 - gamma : gamma;
  }

  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notEmpty(x, "x was null");
    final int length = x.length;
    final double[] winsorized = Arrays.copyOf(x, length);
    Arrays.sort(winsorized);
    final int value = (int) Math.round(length * _gamma);
    final double x1 = winsorized[value];
    final double x2 = winsorized[length - value - 1];
    for (int i = 0; i < value; i++) {
      winsorized[i] = x1;
      winsorized[length - 1 - i] = x2;
    }
    return DescriptiveStatisticsFactory.of(MeanCalculator.NAME).evaluate(winsorized);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
