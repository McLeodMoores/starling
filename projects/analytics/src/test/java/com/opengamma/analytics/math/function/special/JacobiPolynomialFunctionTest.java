/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class JacobiPolynomialFunctionTest {
  private static final double ALPHA = 0.12;
  private static final double BETA = 0.34;
  private static final DoubleFunction1D P0 = new DoubleFunction1D() {

    @Override
    public Double apply(final Double x) {
      return 1.;
    }

  };
  private static final DoubleFunction1D P1 = new DoubleFunction1D() {

    @Override
    public Double apply(final Double x) {
      return 0.5 * (2 * (ALPHA + 1) + (ALPHA + BETA + 2) * (x - 1));
    }

  };
  private static final DoubleFunction1D P2 = new DoubleFunction1D() {

    @Override
    public Double apply(final Double x) {
      return 0.125 * (4 * (ALPHA + 1) * (ALPHA + 2) + 4 * (ALPHA + BETA + 3) * (ALPHA + 2) * (x - 1) + (ALPHA + BETA + 3) * (ALPHA + BETA + 4) * (x - 1) * (x - 1));
    }

  };
  private static final DoubleFunction1D[] P = new DoubleFunction1D[] {P0, P1, P2};
  private static final JacobiPolynomialFunction JACOBI = new JacobiPolynomialFunction();
  private static final double EPS = 1e-9;

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoAlphaBeta() {
    JACOBI.getPolynomials(3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeN() {
    JACOBI.getPolynomials(-3, ALPHA, BETA);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetPolynomials() {
    JACOBI.getPolynomialsAndFirstDerivative(3);
  }

  @Test
  public void test() {
    DoubleFunction1D[] p = JACOBI.getPolynomials(0, ALPHA, BETA);
    assertEquals(p.length, 1);
    final double x = 1.23;
    assertEquals(p[0].apply(x), 1, EPS);
    for (int i = 0; i <= 2; i++) {
      p = JACOBI.getPolynomials(i, ALPHA, BETA);
      for (int j = 0; j <= i; j++) {
        assertEquals(P[j].apply(x), p[j].apply(x), EPS);
      }
    }
  }
}
