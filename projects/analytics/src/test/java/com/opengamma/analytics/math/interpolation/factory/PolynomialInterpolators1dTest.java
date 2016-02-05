/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.PolynomialInterpolator1D;

/**
 * Unit tests for {@link QuadraticInterpolator1d}, {@link CubicInterpolator1d}, {@link QuarticInterpolator1d} and {@link QuinticInterpolator1d}.
 */
public class PolynomialInterpolators1dTest {

  /**
   * Tests the degree of the underlying interpolator.
   */
  @Test
  public void testDegree() {
    assertEquals(new QuadraticInterpolator1d().getUnderlyingInterpolator(), new PolynomialInterpolator1D(2));
    assertEquals(new CubicInterpolator1d().getUnderlyingInterpolator(), new PolynomialInterpolator1D(3));
    assertEquals(new QuarticInterpolator1d().getUnderlyingInterpolator(), new PolynomialInterpolator1D(4));
    assertEquals(new QuinticInterpolator1d().getUnderlyingInterpolator(), new PolynomialInterpolator1D(5));
  }
}
