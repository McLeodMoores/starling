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


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.analytics.math.statistics.descriptive.DescriptiveStatisticsFactory;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SampleMedianAbsoluteDeviationCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class SampleMedianAbsoluteDeviationCalculatorTest {
  private static final double MEAN = 3.5;
  private static final double STD = 0.2;
  private static final Function1D<double[], Double> MAD_CALC = new SampleMedianAbsoluteDeviationCalculator();
  private static final Function1D<double[], Double> STD_CALC = new SampleStandardDeviationCalculator();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(MEAN, STD);
  private static final double[] NORMAL_DATA = new double[50000];
  private static final double EPS = 1e-2;
  static {
    for (int i = 0; i < 50000; i++) {
      NORMAL_DATA[i] = NORMAL.nextRandom();
    }
  }

  /**
   * Tests that the data cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    MAD_CALC.evaluate((double[]) null);
  }

  /**
   * Tests that there must be at least 2 data points.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    MAD_CALC.evaluate(new double[] {3.});
  }

  /**
   * Tests the result.
   */
  @Test
  public void test() {
    assertEquals(STD_CALC.evaluate(NORMAL_DATA), MAD_CALC.evaluate(NORMAL_DATA) / .6745, EPS);
  }

  /**
   * Tests that the calculator can be obtained from {@link DescriptiveStatisticsFactory}.
   */
  @Test
  public void testCalculatorFromFactory() {
    assertTrue(DescriptiveStatisticsFactory.of(SampleMedianAbsoluteDeviationCalculator.NAME) instanceof SampleMedianAbsoluteDeviationCalculator);
    assertTrue(DescriptiveStatisticsFactory.of("SampleMedianAbsoluteDeviation") instanceof SampleMedianAbsoluteDeviationCalculator);
    assertTrue(DescriptiveStatisticsFactory.of("Sample Median Absolute Deviation") instanceof SampleMedianAbsoluteDeviationCalculator);
  }
}
