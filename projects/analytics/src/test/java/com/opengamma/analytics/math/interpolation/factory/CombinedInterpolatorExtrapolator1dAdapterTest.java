/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.interpolation.factory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Unit tests for {@link CombinedInterpolatorExtrapolator1dAdapter}.
 */
public class CombinedInterpolatorExtrapolator1dAdapterTest {

  /**
   * Tests the behaviour when the interpolator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new CombinedInterpolatorExtrapolator1dAdapter(null, new FlatExtrapolator1dAdapter(), new FlatExtrapolator1dAdapter());
  }

  /**
   * Tests the behaviour when the extrapolator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExtrapolator() {
    new CombinedInterpolatorExtrapolator1dAdapter(new LinearInterpolator1dAdapter(), null);
  }

  /**
   * Tests the behaviour when the left extrapolator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLeftExtrapolator() {
    new CombinedInterpolatorExtrapolator1dAdapter(new LinearInterpolator1dAdapter(), null, new FlatExtrapolator1dAdapter());
  }

  /**
   * Tests the behaviour when the right extrapolator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRightExtrapolator() {
    new CombinedInterpolatorExtrapolator1dAdapter(new LinearInterpolator1dAdapter(), new FlatExtrapolator1dAdapter(), null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final FlatExtrapolator1dAdapter extrapolator = new FlatExtrapolator1dAdapter();
    final LinearInterpolator1dAdapter interpolator = new LinearInterpolator1dAdapter();
    CombinedInterpolatorExtrapolator1dAdapter adapter = new CombinedInterpolatorExtrapolator1dAdapter(interpolator, extrapolator, extrapolator);
    assertEquals(adapter.getName(), "Combined[interpolator = Linear, left extrapolator = Flat Extrapolator, right extrapolator = Flat Extrapolator]");
    assertEquals(adapter.toString(), "Combined[interpolator = Linear, left extrapolator = Flat Extrapolator, right extrapolator = Flat Extrapolator]");
    assertEquals(adapter.getInterpolator(), interpolator);
    assertEquals(adapter.getLeftExtrapolator(), extrapolator);
    assertEquals(adapter.getRightExtrapolator(), extrapolator);
    assertEquals(adapter, adapter);
    assertNotEquals(null, adapter);
    assertNotEquals(new Object(), adapter);
    assertNotEquals(interpolator, adapter);
    final CombinedInterpolatorExtrapolator1dAdapter other = new CombinedInterpolatorExtrapolator1dAdapter(interpolator, extrapolator, extrapolator);
    assertEquals(adapter, other);
    assertEquals(adapter.hashCode(), other.hashCode());
    adapter = new CombinedInterpolatorExtrapolator1dAdapter(interpolator, extrapolator);
    assertEquals(adapter.getName(), "Combined[interpolator = Linear, left extrapolator = Flat Extrapolator, right extrapolator = Flat Extrapolator]");
    assertEquals(adapter.toString(), "Combined[interpolator = Linear, left extrapolator = Flat Extrapolator, right extrapolator = Flat Extrapolator]");
  }

  /**
   * Tests delegation to the underlying interpolator.
   */
  @Test
  public void testDelegation() {
    final FlatExtrapolator1dAdapter extrapolator = new FlatExtrapolator1dAdapter();
    final LinearInterpolator1dAdapter interpolator = new LinearInterpolator1dAdapter();
    final CombinedInterpolatorExtrapolator delegate = new CombinedInterpolatorExtrapolator(interpolator, extrapolator, extrapolator);
    final CombinedInterpolatorExtrapolator1dAdapter adapter = new CombinedInterpolatorExtrapolator1dAdapter(interpolator, extrapolator, extrapolator);
    final double[] x = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
    final double[] xUnsorted = new double[] {1, 3, 2, 4, 5, 6, 7, 8, 9};
    final double[] y = new double[] {0.1, 0.2, 0.4, 0.6, 0.9, 1.0, 1.4, 1.5, 2.0};
    final Map<Double, Double> xy = new HashMap<>();
    for (int i = 0; i < x.length; i++) {
      xy.put(x[i], y[i]);
    }
    final Interpolator1DDataBundle data = adapter.getDataBundleFromSortedArrays(x, y);
    assertEquals(data, delegate.getDataBundleFromSortedArrays(x, y));
    assertEquals(adapter.getDataBundle(xy), delegate.getDataBundle(xy));
    assertEquals(adapter.getDataBundle(xUnsorted, y), delegate.getDataBundle(xUnsorted, y));
    for (double xi = 1.01; xi < 9; xi += 0.01) {
      assertEquals(adapter.interpolate(data, xi), delegate.interpolate(data, xi));
      assertEquals(adapter.firstDerivative(data, xi), delegate.firstDerivative(data, xi));
      assertEquals(adapter.getNodeSensitivitiesForValue(data, xi), delegate.getNodeSensitivitiesForValue(data, xi));
      assertEquals(adapter.getNodeSensitivitiesForValue(data, xi, true), delegate.getNodeSensitivitiesForValue(data, xi, true));
      assertEquals(adapter.getNodeSensitivitiesForValue(data, xi, false), delegate.getNodeSensitivitiesForValue(data, xi, false));
      assertEquals(adapter.firstDerivative(data, xi), delegate.firstDerivative(data, xi));
    }
  }
}
