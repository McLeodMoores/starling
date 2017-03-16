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

/**
 * Calculates the sample standard deviation of a series of data. The sample standard deviation of a series of data is defined as the square root of
 * the sample variance (see {@link SampleVarianceCalculator}).
 */
@DescriptiveStatistic(name = SampleStandardDeviationCalculator.NAME, aliases = "Sample Standard Deviation")
public class SampleStandardDeviationCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "SampleStandardDeviation";

  /**
   * @param x  the array of data, not null, must contain at least two data points
   * @return  the sample standard deviation
   */
  @Override
  public Double evaluate(final double[] x) {
    return Math.sqrt(DescriptiveStatisticsFactory.of(SampleVarianceCalculator.NAME).evaluate(x));
  }

  @Override
  public String getName() {
    return NAME;
  }
}
