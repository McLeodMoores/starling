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
 * The sample Pearson kurtosis gives a measure of how heavy the tails of a
 * distribution are with respect to the normal distribution (which has a
 * Pearson kurtosis of three). It is calculated using
 * $$
 * \begin{align*}
 * \text{Pearson kurtosis} = \text{Fisher kurtosis} + 3
 * \end{align*}
 * $$
 * where the Fisher kurtosis is calculated using {@link SampleFisherKurtosisCalculator}.
 */
@DescriptiveStatistic(name = SamplePearsonKurtosisCalculator.NAME, aliases = "Sample Pearson Kurtosis")
public class SamplePearsonKurtosisCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "SamplePearsonKurtosis";

  /**
   * @param x  the array of data, not null. Must contain at least four data points.
   * @return  the sample Pearson kurtosis
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.isTrue(x.length >= 4, "Need at least four points to calculate kurtosis");
    return DescriptiveStatisticsFactory.of(SampleFisherKurtosisCalculator.NAME).evaluate(x) + 3;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
