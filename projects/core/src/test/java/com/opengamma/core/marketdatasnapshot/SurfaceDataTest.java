/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests for {@link SurfaceData}.
 */
@Test(groups = TestGroup.UNIT)
public class SurfaceDataTest {
  private static final String NAME = "NAME";
  private static final double[] XS = new double[] {1, 2, 3, 4, 5, 6, 7, 8};
  private static final double[] YS = new double[] {10, 20, 30, 40, 50, 60};
  private static final Map<Pair<Double, Double>, Double> VALS = new HashMap<>();
  static {
    for (final double x : XS) {
      for (final double y : YS) {
        VALS.put(Pairs.of(x, y), x);
      }
    }
  }
  private static final SurfaceData<Double, Double> DATA = new SurfaceData<>(NAME, VALS);

  /**
   * Tests that the values cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValues() {
    new SurfaceData<>(NAME, null);
  }

  /**
   * Tests the size of the data object.
   */
  @Test
  public void testSize() {
    assertEquals(DATA.size(), XS.length * YS.length);
  }

  /**
   * Tests the getValue method.
   */
  @Test
  public void testGetValue() {
    assertNull(DATA.getValue(-1., -1.));
    for (final double x : XS) {
      for (final double y : YS) {
        assertEquals(DATA.getValue(x, y), x);
      }
    }
  }

  /**
   * Tests the getUniqueXValues method.
   */
  @Test
  public void testGetUniqueXValues() {
    final SortedSet<Double> xs = DATA.getUniqueXValues();
    assertEquals(xs.size(), XS.length);
    int i = 0;
    for (final Double x : xs) {
      assertEquals(x, XS[i++]);
    }
  }

  /**
   * Tests the exception when there are no values for an x.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoYValuesForX() {
    DATA.getYValuesForX(-1.).isEmpty();
  }

  /**
   * Tests the getYValuesForX method.
   */
  @Test
  public void testGetYValuesForX() {
    for (final double x : XS) {
      final List<ObjectsPair<Double, Double>> ys = DATA.getYValuesForX(x);
      assertEquals(ys.size(), YS.length);
      int j = 0;
      for (final ObjectsPair<Double, Double> p : ys) {
        assertEquals(p.first, YS[j++]);
        assertEquals(p.second, x);
      }
    }
  }

  /**
   * Tests the asMap method.
   */
  @Test
  public void testAsMap() {
    assertEquals(DATA.asMap(), VALS);
  }

  /**
   * Tests that the map cannot be set to null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetNullMap() {
    DATA.setValues(null);
  }

  /**
   * Tests the setValues method.
   */
  @Test
  public void testSetValues() {
    final SurfaceData<Double, Double> copy = DATA.clone();
    final Map<Pair<Double, Double>, Double> vals = new HashMap<>();
    for (final double x : XS) {
      for (final double y : YS) {
        vals.put(Pairs.of(x, y), x);
      }
    }
    copy.setValues(vals);
    assertEquals(copy.asMap(), vals);
  }

  /**
   * Tests the behaviour when an x value is null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullX() {
    final Double[] xs = new Double[] { 1., null };
    final Double[] ys = new Double[] { 10., 20., 30. };
    final Map<Pair<Double, Double>, Double> vals = new HashMap<>();
    for (final Double x : xs) {
      for (final Double y : ys) {
        vals.put(Pairs.of(x, y), x);
      }
    }
    new SurfaceData<>(NAME, vals);
  }

  /**
   * Tests the behaviour when a y value is null.
   */
  @Test
  public void testNullY() {
    final Double[] xs = new Double[] { 10., 20., 30. };
    final Double[] ys = new Double[] { 1., null };
    final Map<Pair<Double, Double>, Double> vals = new HashMap<>();
    for (final Double x : xs) {
      for (final Double y : ys) {
        vals.put(Pairs.of(x, y), x);
      }
    }
    final SurfaceData<Double, Double> data = new SurfaceData<>(NAME, vals);
    assertEquals(data.asMap(), vals);
  }

  /**
   * Tests the x and y labels.
   */
  @Test
  public void testLabels() {
    assertEquals(DATA.getXLabel(), "x");
    assertEquals(DATA.getYLabel(), "y");
    final SurfaceData<Double, Double> data = new SurfaceData<>(NAME, "y", "z", VALS);
    assertEquals(data.getXLabel(), "y");
    assertEquals(data.getYLabel(), "z");
  }
}
