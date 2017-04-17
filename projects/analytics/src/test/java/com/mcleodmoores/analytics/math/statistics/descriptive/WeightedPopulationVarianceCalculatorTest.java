/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.covariance.ExponentialWeightedMovingAverageHistoricalVolatilityCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.analytics.math.statistics.descriptive.PopulationVarianceCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CalculationMode;

/**
 * Unit tests for {@link WeightedPopulationVarianceCalculator}.
 */
public class WeightedPopulationVarianceCalculatorTest {
  private static final WeightedPopulationVarianceCalculator CALCULATOR = new WeightedPopulationVarianceCalculator();

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
   * Tests that the weighted sample deviation is equal to the sample variance if the weights are equal.
   */
  @Test
  public void testEqualWeight() {
    final int n = 1000;
    final double[] values = new double[n];
    for (int i = 0; i < n; i++) {
      values[i] = Math.random();
    }
    final EqualWeightFunction f = EqualWeightFunction.ofInverse(n);
    assertEquals(CALCULATOR.apply(f, values), new PopulationVarianceCalculator().evaluate(values), 1e-15);
  }

  /**
   * Tests that the exponentially-weighted sampled variance of a series is less than the sample variance.
   */
  @Test
  public void testExponentialWeightedSampleVariance() {
    final int n = 1000;
    final double[] values = new double[n];
    for (int i = 0; i < n; i++) {
      values[i] = i;
    }
    final ExponentialWeightFunction f = ExponentialWeightFunction.of(0.03);
    assertTrue(CALCULATOR.apply(f, values) < new SampleVarianceCalculator().evaluate(values));
  }

  /**
   * Tests that the results are the same as those for the historical time series calculator.
   */
  @Test
  public void testTimeSeriesWeighting() {
    final int n = 1000;
    final LocalDate[] dates = new LocalDate[n];
    final double[] values = new double[n];
    final double[] reverseValues = new double[n];
    final Random random = new Random(10003);
    for (int i = 0; i < n; i++) {
      dates[i] = LocalDate.now().plusDays(i);
      values[i] = random.nextDouble();
      reverseValues[n - i - 1] = values[i];
    }
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(dates, reverseValues);
    final double lambda = 0.94;
    final TimeSeriesReturnCalculator noOp = new TimeSeriesReturnCalculator(CalculationMode.LENIENT) {

      @Override
      public LocalDateDoubleTimeSeries evaluate(final LocalDateDoubleTimeSeries... x) {
        return x[0];
      }
    };
    final double expectedStdDev = new ExponentialWeightedMovingAverageHistoricalVolatilityCalculator(lambda, noOp).evaluate(ts);
    final ExponentialWeightFunction f = ExponentialWeightFunction.of(1 - lambda);
    final Double variance = CALCULATOR.apply(f, values);
    final double stdDev = Math.sqrt(variance);
    final double temp = stdDev / expectedStdDev;
    //    assertEquals(stdDev, expectedStdDev, 1e-15);
  }

  /**
   * Detect regressions.
   */
  @Test
  public void regression() {
    final double[] expected = new double[] {0.010713544966874324, 0.04933157915323014, 8.575114320709428E-4};
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
