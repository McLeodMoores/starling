/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the repeated one-dimensional integration to integrate 2-D functions.
 */
@Test(groups = TestGroup.UNIT)
public class IntegratorRepeated2DTest {

  @Test
  /**
   * Numerical integral vs a known explicit solution.
   */
  public void integrate() {

    // Test function.
    final Function2D<Double, Double> f = new Function2D<Double, Double>() {
      @Override
      public Double evaluate(final Double x1, final Double x2) {
        return x1 + Math.sin(x2);
      }
    };

    final double absTol = 1.0E-6;
    final double relTol = 1.0E-6;
    final int minSteps = 6;
    final RungeKuttaIntegrator1D integrator1D = new RungeKuttaIntegrator1D(absTol, relTol, minSteps);
    final IntegratorRepeated2D integrator2D = new IntegratorRepeated2D(integrator1D);

    Double[] lower;
    Double[] upper;
    double result, resultExpected;
    // First set of limits.
    lower = new Double[] {0.0, 1.0};
    upper = new Double[] {2.0, 10.0};
    result = integrator2D.integrate(f, lower, upper);
    resultExpected = (upper[0] * upper[0] - lower[0] * lower[0]) / 2.0 * (upper[1] - lower[1]) + (upper[0] - lower[0]) * (-Math.cos(upper[1]) + Math.cos(lower[1]));
    assertEquals("Integration 2D - repeated 1D", resultExpected, result, 1E-8);
    // Second set of limits.
    lower = new Double[] {0.25, 5.25};
    upper = new Double[] {25.25, 35.25};
    result = integrator2D.integrate(f, lower, upper);
    resultExpected = (upper[0] * upper[0] - lower[0] * lower[0]) / 2.0 * (upper[1] - lower[1]) + (upper[0] - lower[0]) * (-Math.cos(upper[1]) + Math.cos(lower[1]));
    assertEquals("Integration 2D - repeated 1D", resultExpected, result, 1E-6);
  }
}
