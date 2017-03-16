/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.statistics.descriptive;

/**
 * Calculates the standard deviation of a series of data below the mean of the data.
 */
@DescriptiveStatistic(name = LowerSemiStandardDeviationCalculator.NAME,
  aliases = { "Lower Semi-Standard Sample Deviation", "Downside Standard Sample Deviation", "DownsideStandardSampleDeviation" })
public class LowerSemiStandardDeviationCalculator extends SemiStandardDeviationCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "LowerSemiStandardSampleDeviation";

  /**
   * Creates an instance of this calculator.
   */
  LowerSemiStandardDeviationCalculator() {
    super(true);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
