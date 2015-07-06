/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.ExponentialExtrapolator1D;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Unit tests for {@link Extrapolator1dAdapter}.
 */
public class Extrapolator1dAdapterTest {
  /** The extrapolator */
  private static final Interpolator1D EXTRAPOLATOR = new FlatExtrapolator1D();
  /** The name */
  private static final String NAME = "Test";

  /**
   * Tests the behaviour when the extrapolator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExtrapolator() {
    new Extrapolator1dAdapter(null, NAME);
  }

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new Extrapolator1dAdapter(EXTRAPOLATOR, null);
  }

  /**
   * Tests hashCode(), equals() and getters.
   */
  @Test
  public void testObject() {
    final Extrapolator1dAdapter adapter = new Extrapolator1dAdapter(EXTRAPOLATOR, NAME);
    Extrapolator1dAdapter other = new Extrapolator1dAdapter(EXTRAPOLATOR, NAME);
    assertEquals(adapter, adapter);
    assertNotEquals(null, adapter);
    assertNotEquals(2, adapter);
    assertEquals(other, adapter);
    assertEquals(other.hashCode(), adapter.hashCode());
    assertEquals(adapter.getUnderlyingExtrapolator(), EXTRAPOLATOR);
    assertEquals(adapter.getName(), NAME);
    other = new Extrapolator1dAdapter(new ExponentialExtrapolator1D(), NAME);
    assertNotEquals(other, adapter);
    other = new Extrapolator1dAdapter(EXTRAPOLATOR, NAME + "1");
    assertNotEquals(other, adapter);
  }

  /**
   * Tests method delegation.
   */
  @Test
  public void testDelegation() {
    final Extrapolator1dAdapter adapter = new Extrapolator1dAdapter(EXTRAPOLATOR, NAME);
    final double[] x = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
    final double[] y = new double[] {0.1, 0.2, 0.4, 0.6, 0.9, 1.0, 1.4, 1.5, 2.0};
    final Interpolator1DDataBundle data = new ArrayInterpolator1DDataBundle(x, y);
    for (double xi = -1; xi < 1; xi += 0.01) {
      assertEquals(adapter.interpolate(data, xi), EXTRAPOLATOR.interpolate(data, xi));
      assertEquals(adapter.firstDerivative(data, xi), EXTRAPOLATOR.firstDerivative(data, xi));
      // finite difference not supported for extrapolators
      assertEquals(adapter.getNodeSensitivitiesForValue(data, xi, false), EXTRAPOLATOR.getNodeSensitivitiesForValue(data, xi, false));
    }
    for (double xi = 9.01; xi < 11; xi += 0.01) {
      assertEquals(adapter.interpolate(data, xi), EXTRAPOLATOR.interpolate(data, xi));
      assertEquals(adapter.firstDerivative(data, xi), EXTRAPOLATOR.firstDerivative(data, xi));
      // finite difference not supported for extrapolators
      assertEquals(adapter.getNodeSensitivitiesForValue(data, xi, false), EXTRAPOLATOR.getNodeSensitivitiesForValue(data, xi, false));
    }
  }
}
