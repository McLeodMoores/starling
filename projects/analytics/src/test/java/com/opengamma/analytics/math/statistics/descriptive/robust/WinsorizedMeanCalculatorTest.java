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
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link WinsorizedMeanCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class WinsorizedMeanCalculatorTest {
  private static final int N = 100;
  private static final Function1D<double[], Double> CALCULATOR = new WinsorizedMeanCalculator(0.1);
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private static final double EPS = 1e-12;

  /**
   * Tests that the gamma must be greater than zero.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowGamma() {
    new WinsorizedMeanCalculator(0);
  }

  /**
   * Tests that the gamma must be less than zero.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighGamma() {
    new WinsorizedMeanCalculator(1);
  }

  /**
   * Tests that the data cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((double[]) null);
  }

  /**
   * Tests that the array cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    CALCULATOR.evaluate(new double[0]);
  }

  /**
   * Tests the calculator.
   */
  @Test
  public void test() {
    final double[] x = new double[N];
    for (int i = 0; i < 10; i++) {
      x[i] = Double.valueOf(10);
    }
    for (int i = 10; i < N - 10; i++) {
      x[i] = Double.valueOf(i);
    }
    for (int i = N - 10; i < N; i++) {
      x[i] = Double.valueOf(N - 10 - 1);
    }
    assertEquals(CALCULATOR.evaluate(x), MEAN.evaluate(x), EPS);
    for (int i = 0; i < N - 1; i++) {
      x[i] = Double.valueOf(i);
    }
    x[N - 1] = 100000.;
    assertTrue(CALCULATOR.evaluate(x) < MEAN.evaluate(x));
  }

  /**
   * Tests that the calculator can be obtained from {@link DescriptiveStatisticsFactory}.
   */
  @Test
  public void testCalculatorFromFactory() {
    assertTrue(DescriptiveStatisticsFactory.of(WinsorizedMeanCalculator.NAME, 0.5) instanceof WinsorizedMeanCalculator);
  }
}
