/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.statistics.descriptive;

/**
 * Calculates the standard deviation of a series of data above the mean of the data.
 */
@DescriptiveStatistic(name = UpperSemiStandardDeviationCalculator.NAME,
  aliases = { "Upper Semi-Standard Sample Deviation", "Upside Standard Sample Deviation", "UpsideStandardSampleDeviation" })
public class UpperSemiStandardDeviationCalculator extends SemiStandardDeviationCalculator {
  /**
   * The name of this calculator.
   */
  public static final String NAME = "LowerSemiStandardSampleDeviation";

  /**
   * Creates an instance of this calculator.
   */
  UpperSemiStandardDeviationCalculator() {
    super(false);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
