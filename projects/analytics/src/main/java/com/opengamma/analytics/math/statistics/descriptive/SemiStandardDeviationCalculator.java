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
 * The semi-standard deviation of a series of data is the partial moment (see {@link PartialMomentCalculator}) calculated with the mean as the threshold.
 * This gives a measure of the spread of the values of a series above or below a threshold, and be used to calculate downside or upside risk.
 */
@DescriptiveStatistic(name = SemiStandardDeviationCalculator.CALCULATOR_NAME, aliases = "Semi Standard Deviation")
public class SemiStandardDeviationCalculator extends DescriptiveStatisticsCalculator {
  /**
   * The name of this calculator.
   */
  public static final String CALCULATOR_NAME = "SemiStandardDeviationCalculator";

  /** True if the downside standard deviation is calculated */
  private final boolean _useDownSide;

  /**
   * Creates a downside semi-standard sample deviation calculator.
   */
  public SemiStandardDeviationCalculator() {
    _useDownSide = true;
  }

  /**
   * Creates a calculator.
   * @param useDownSide  true if data below the mean is used in the calculation, false if data above the mean is used
   * in the calculation
   */
  public SemiStandardDeviationCalculator(final boolean useDownSide) {
    _useDownSide = useDownSide;
  }

  /**
   * @param x  the array of data, not null
   * @return  the semi-standard deviation
   */
  @Override
  public Double evaluate(final double[] x) {
    final double mean = DescriptiveStatisticsFactory.of(MeanCalculator.NAME).evaluate(x);
    final int n = x.length;
    return new PartialMomentCalculator(mean, _useDownSide).evaluate(x) * n / (n - 1);
  }

  @Override
  public String getName() {
    return CALCULATOR_NAME;
  }
}
