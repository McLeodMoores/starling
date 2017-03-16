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
 * The trimmed mean is a robust estimator of the mean that removes all outliers in a set of data
 * above or below a certain percentile $$\gamma$$.
 */
@DescriptiveStatistic(name = TrimmedMeanCalculator.NAME, aliases = "Trimmed Mean")
public class TrimmedMeanCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "TrimmedMean";

  /** The percentile */
  private final double _gamma;

  /**
   * Creates the calculator.
   * @param gamma  the percentile, must be between 0 and 1
   */
  public TrimmedMeanCalculator(final double gamma) {
    ArgumentChecker.isTrue(gamma >= 0 && gamma <= 1, "Gamma must be between 0 and 1, have {}", gamma);
    _gamma = gamma > 0.5 ? 1 - gamma : gamma;
  }

  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notEmpty(x, "x was null");
    final int length = x.length;
    final int value = (int) Math.round(length * _gamma);
    final double[] copy = Arrays.copyOf(x, length);
    Arrays.sort(copy);
    final double[] trimmed = new double[length - 2 * value];
    for (int i = 0; i < trimmed.length; i++) {
      trimmed[i] = x[i + value];
    }
    return DescriptiveStatisticsFactory.of(MeanCalculator.NAME).evaluate(trimmed);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
