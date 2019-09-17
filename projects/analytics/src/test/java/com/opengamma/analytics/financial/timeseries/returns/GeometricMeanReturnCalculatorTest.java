/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GeometricMeanReturnCalculatorTest {
  private static final Function1D<double[], Double> SIMPLE = new SimplyCompoundedGeometricMeanReturnCalculator();
  private static final Function1D<double[], Double> CONTINUOUS = new ContinuouslyCompoundedGeometricMeanReturnCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    SIMPLE.apply((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty1() {
    SIMPLE.apply(new double[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    CONTINUOUS.apply((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmpty2() {
    CONTINUOUS.apply(new double[0]);
  }

  @Test
  public void test() {
    final int n = 100;
    final double[] x = new double[n];
    final double r = Math.random();
    for (int i = 0; i < n; i++) {
      x[i] = r;
    }
    assertEquals(SIMPLE.apply(x), r, 1e-12);
    assertEquals(CONTINUOUS.apply(x), r, 1e-12);
  }
}
