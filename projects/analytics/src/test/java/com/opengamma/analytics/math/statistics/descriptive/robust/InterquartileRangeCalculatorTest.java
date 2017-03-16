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

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.DescriptiveStatisticsFactory;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * Unit tests for {@link InterquartileRangeCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class InterquartileRangeCalculatorTest {
  private static final Function1D<double[], Double> IQR = DescriptiveStatisticsFactory.of(InterquartileRangeCalculator.NAME);
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double[] UNIFORM_DATA;
  private static final double[] NORMAL_DATA;
  private static final double EPS = 1e-2;
  static {
    final int n = 500000;
    UNIFORM_DATA = new double[n];
    NORMAL_DATA = new double[n];
    for (int i = 0; i < n; i++) {
      UNIFORM_DATA[i] = RANDOM.nextDouble();
      NORMAL_DATA[i] = NORMAL.nextRandom();
    }
  }

  /**
   * Tests that the data cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    IQR.evaluate((double[]) null);
  }

  /**
   * Tests that there must be at least three data points.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    IQR.evaluate(new double[] {1., 2.});
  }

  /**
   * Tests the calculator.
   */
  @Test
  public void test() {
    final double[] x1 = new double[] {1., 2., 3., 4., 5., 6., 7., 8., 9., 10.};
    final double[] x2 = new double[] {1., 2., 3., 4., 5., 6., 7., 8., 9., 10., 11., 12., 13.};
    assertEquals(IQR.evaluate(x1), 5, 1e-15);
    assertEquals(IQR.evaluate(x2), 6, 1e-15);
    assertEquals(IQR.evaluate(UNIFORM_DATA), 0.5, EPS);
    assertEquals(IQR.evaluate(NORMAL_DATA), 2 * NORMAL.getInverseCDF(0.75), EPS);
  }

  /**
   * Tests that the calculator can be obtained from {@link DescriptiveStatisticsFactory}.
   */
  @Test
  public void testCalculatorFromFactory() {
    assertTrue(DescriptiveStatisticsFactory.of(InterquartileRangeCalculator.NAME) instanceof InterquartileRangeCalculator);
    assertTrue(DescriptiveStatisticsFactory.of("InterquartileRange") instanceof InterquartileRangeCalculator);
    assertTrue(DescriptiveStatisticsFactory.of("Interquartile Range") instanceof InterquartileRangeCalculator);
    assertTrue(DescriptiveStatisticsFactory.of("IQR") instanceof InterquartileRangeCalculator);
  }

}
