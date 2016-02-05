/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.QuadraticSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Unit tests for {@link Interpolator1dAdapter}.
 */
public class Interpolator1dAdapterTest {
  /** The interpolator */
  private static final Interpolator1D INTERPOLATOR = new QuadraticInterpolator1d();
  /** The extrapolator */
  private static final Interpolator1D EXTRAPOLATOR = new FlatExtrapolator1D();
  /** The name */
  private static final String NAME = "Test";

  /**
   * Tests the behaviour when the interpolator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new Interpolator1dAdapter(null, NAME);
  }

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new Interpolator1dAdapter(INTERPOLATOR, null);
  }

  /**
   * Tests hashCode(), equals() and getters.
   */
  @Test
  public void testObject() {
    final Interpolator1dAdapter adapter = new Interpolator1dAdapter(INTERPOLATOR, NAME);
    Interpolator1dAdapter other = new Interpolator1dAdapter(INTERPOLATOR, NAME);
    assertEquals(adapter, adapter);
    assertNotEquals(null, adapter);
    assertNotEquals(2, adapter);
    assertEquals(other, adapter);
    assertEquals(other.hashCode(), adapter.hashCode());
    assertEquals(adapter.getUnderlyingInterpolator(), INTERPOLATOR);
    assertEquals(adapter.getName(), NAME);
    other = new Interpolator1dAdapter(new QuadraticSplineInterpolator1D(), NAME);
    assertNotEquals(other, adapter);
    other = new Interpolator1dAdapter(INTERPOLATOR, NAME + "1");
    assertNotEquals(other, adapter);
  }

  /**
   * Tests method delegation for interpolators.
   */
  @Test
  public void testDelegationForInterpolator() {
    final Interpolator1dAdapter adapter = new Interpolator1dAdapter(INTERPOLATOR, NAME);
    final double[] x = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
    final double[] xUnsorted = new double[] {1, 3, 2, 4, 5, 6, 7, 8, 9};
    final double[] y = new double[] {0.1, 0.2, 0.4, 0.6, 0.9, 1.0, 1.4, 1.5, 2.0};
    final Map<Double, Double> xy = new HashMap<>();
    for (int i = 0; i < x.length; i++) {
      xy.put(x[i], y[i]);
    }
    final Interpolator1DDataBundle data = adapter.getDataBundleFromSortedArrays(x, y);
    assertEquals(data, INTERPOLATOR.getDataBundleFromSortedArrays(x, y));
    assertEquals(adapter.getDataBundle(xy), INTERPOLATOR.getDataBundle(xy));
    assertEquals(adapter.getDataBundle(xUnsorted, y), INTERPOLATOR.getDataBundle(xUnsorted, y));
    for (double xi = 1.01; xi < 9; xi += 0.01) {
      assertEquals(adapter.interpolate(data, xi), INTERPOLATOR.interpolate(data, xi));
      assertEquals(adapter.firstDerivative(data, xi), INTERPOLATOR.firstDerivative(data, xi));
      assertEquals(adapter.getNodeSensitivitiesForValue(data, xi), INTERPOLATOR.getNodeSensitivitiesForValue(data, xi));
      assertEquals(adapter.getNodeSensitivitiesForValue(data, xi, true), INTERPOLATOR.getNodeSensitivitiesForValue(data, xi, true));
      assertEquals(adapter.getNodeSensitivitiesForValue(data, xi, false), INTERPOLATOR.getNodeSensitivitiesForValue(data, xi, false));
      assertEquals(adapter.firstDerivative(data, xi), INTERPOLATOR.firstDerivative(data, xi));
    }
  }

  /**
   * Tests method delegation for extrapolators.
   */
  @Test
  public void testDelegationForExtrapolators() {
    final Interpolator1dAdapter adapter = new Interpolator1dAdapter(EXTRAPOLATOR, NAME);
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
