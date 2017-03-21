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
 * Calculates the population standard deviation of a series of data. The population standard deviation of a series of data is defined as the square root of
 * the population variance (see {@link PopulationVarianceCalculator}).
 */
@DescriptiveStatistic(name = PopulationStandardDeviationCalculator.NAME, aliases = "Population Standard Deviation")
public class PopulationStandardDeviationCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "PopulationStandardDeviation";

  /**
   * @param x  the array of data, not null, must contain at least two data points
   * @return  the population standard deviation
   */
  @Override
  public Double evaluate(final double[] x) {
    return Math.sqrt(DescriptiveStatisticsFactory.of(PopulationVarianceCalculator.NAME).evaluate(x));
  }

  @Override
  public String getName() {
    return NAME;
  }

}
