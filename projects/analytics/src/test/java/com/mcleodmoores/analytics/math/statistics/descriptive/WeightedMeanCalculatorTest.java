/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;

/**
 * Unit tests for {@link WeightedMeanCalculator}.
 */
public class WeightedMeanCalculatorTest {
  private static final WeightedMeanCalculator CALCULATOR = new WeightedMeanCalculator();

  /**
   * Tests that the weight function cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeightFunction() {
    CALCULATOR.apply(null, new double[] {1, 2, 3});
  }

  /**
   * Tests that the values cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValues() {
    CALCULATOR.apply(EqualWeightFunction.of(100), null);
  }

  /**
   * Tests that the weighted mean is equal to the mean if the weights are equal.
   */
  @Test
  public void testEqualWeightedMean() {
    final int n = 1000;
    final double[] values = new double[n];
    final Random random = new Random(10004);
    for (int i = 0; i < n; i++) {
      values[i] = random.nextDouble();
    }
    final EqualWeightFunction f = EqualWeightFunction.of(n);
    assertEquals(CALCULATOR.apply(f, values), new MeanCalculator().evaluate(values), 1e-15);
  }

  /**
   * Tests that the exponentially-weighted mean of a series is less than the sample mean.
   */
  @Test
  public void testExponentialWeightedMean() {
    final int n = 1000;
    final double[] values = new double[n];
    for (int i = 0; i < n; i++) {
      values[i] = i;
    }
    final ExponentialWeightFunction f = ExponentialWeightFunction.of(0.06);
    assertTrue(CALCULATOR.apply(f, values) < new MeanCalculator().evaluate(values));
  }

  /**
   * Detect regressions.
   */
  @Test
  public void regression() {
    final double[] expected = new double[] {0.7253918469569604, 0.6037494362511888, 0.6335251789806483};
    final Random random = new Random(496);
    for (int i = 0; i < 3; i++) {
      final int n = 1000;
      final double[] values = new double[n];
      for (int j = 0; j < n; j++) {
        values[j] = random.nextDouble();
      }
      final ExponentialWeightFunction f = ExponentialWeightFunction.of(random.nextDouble());
      assertEquals(expected[i], CALCULATOR.apply(f, values), 1e-15);
    }
  }
}
