/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests {@link VolatilitySurfaceData}.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceDataTest {
  private static final Double[] XS = new Double[] {1., 1., 2.};
  private static final Double[] YS = new Double[] {4., 5., 6.};
  private static final double[] VOLS = new double[] {10, 11, 12};
  private static final Map<Pair<Double, Double>, Double> VALUES = new HashMap<>();
  private static final VolatilitySurfaceData<Double, Double> VOLS_1;
  private static final VolatilitySurfaceData<Double, Double> VOLS_2;

  static {
    for (int i = 0; i < XS.length; i++) {
      VALUES.put(Pairs.of(XS[i], YS[i]), VOLS[i]);
    }
    VOLS_1 = new VolatilitySurfaceData<>("def", "spec", Currency.EUR, XS, YS, VALUES);
    VOLS_2 = new VolatilitySurfaceData<>("def", "spec", Currency.EUR, XS, "x1", YS, "y1", VALUES);
  }

  /**
   * Tests a rectangular surface.
   */
  @Test
  public void testRectangular() {
    final Double[] xValues = new Double[] {1., 2., 3., 4., 5., 6., 7.};
    final Double[] yValues = new Double[] {100., 110., 120., 130., 140., 150., 160., 170., 180., 190.};
    final int xLength = xValues.length;
    final int yLength = yValues.length;
    final double[][] vols = new double[yLength][xLength];
    final Double[] xs = new Double[xLength * yLength];
    final Double[] ys = new Double[xLength * yLength];
    final Map<Pair<Double, Double>, Double> values = new HashMap<>();
    for (int i = 0, k = 0; i < xLength; i++) {
      for (int j = 0; j < yLength; j++, k++) {
        vols[j][i] = k;
        xs[k] = xValues[i];
        ys[k] = yValues[j];
        values.put(Pairs.of(xValues[i], yValues[j]), vols[j][i]);
      }
    }
    final String name = "test";
    final UniqueIdentifiable target = Currency.USD;
    final VolatilitySurfaceData<Double, Double> data = new VolatilitySurfaceData<>(name, name, target, xs, ys, values);
    final String xLabel = "time";
    final String yLabel = "strike";
    final VolatilitySurfaceData<Double, Double> dataWithLabels = new VolatilitySurfaceData<>(name, name, target, xs, xLabel, ys, yLabel, values);
    assertArrayEquals(xs, data.getXs());
    assertArrayEquals(ys, data.getYs());
    assertArrayEquals(xValues, data.getUniqueXValues().toArray(new Double[xLength]));
    assertArrayEquals(xs, dataWithLabels.getXs());
    assertArrayEquals(ys, dataWithLabels.getYs());
    assertArrayEquals(xValues, dataWithLabels.getUniqueXValues().toArray(new Double[xLength]));
    assertEquals("x", data.getXLabel());
    assertEquals("y", data.getYLabel());
    assertEquals(xLabel, dataWithLabels.getXLabel());
    assertEquals(yLabel, dataWithLabels.getYLabel());
    int i = 0;
    for (final Double x : data.getUniqueXValues()) {
      final List<ObjectsPair<Double, Double>> strips = data.getYValuesForX(x);
      final List<ObjectsPair<Double, Double>> stripsWithLabels = dataWithLabels.getYValuesForX(x);
      int j = 0;
      for (final ObjectsPair<Double, Double> strip : strips) {
        assertEquals(yValues[j], strip.getFirst(), 0);
        assertEquals(vols[j++][i], strip.getSecond(), 0);
      }
      i++;
      assertEquals(strips, stripsWithLabels);
    }
  }

  /**
   * Tests a sparse surface.
   */
  @Test
  public void testRagged() {
    final Double[] xs = new Double[] {1., 1., 1., 2., 2., 3., 3., 3., 4.};
    final Double[] ys = new Double[] {4., 5., 6., 4., 5., 5., 6., 7., 8.};
    final double[] vols = new double[] {10, 11, 12, 13, 14, 15, 16, 17, 18};
    final Map<Pair<Double, Double>, Double> values = new HashMap<>();
    for (int i = 0; i < xs.length; i++) {
      values.put(Pairs.of(xs[i], ys[i]), vols[i]);
    }
    final String name = "test";
    final UniqueIdentifiable target = Currency.USD;
    final VolatilitySurfaceData<Double, Double> data = new VolatilitySurfaceData<>(name, name, target, xs, ys, values);
    assertArrayEquals(xs, data.getXs());
    assertArrayEquals(ys, data.getYs());
    assertArrayEquals(new Double[]{1., 2., 3., 4.}, data.getUniqueXValues().toArray(new Double[3]));
    assertEquals(Arrays.asList(Pairs.of(4., 10.), Pairs.of(5., 11.), Pairs.of(6., 12.)), data.getYValuesForX(1.));
    assertEquals(Arrays.asList(Pairs.of(4., 13.), Pairs.of(5., 14.)), data.getYValuesForX(2.));
    assertEquals(Arrays.asList(Pairs.of(5., 15.), Pairs.of(6., 16.), Pairs.of(7., 17.)), data.getYValuesForX(3.));
    assertEquals(Arrays.asList(Pairs.of(8., 18.)), data.getYValuesForX(4.));
  }

  /**
   * Tests the getters.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testGetters() {
    assertEquals(3, VOLS_1.size());
    assertArrayEquals(XS, VOLS_1.getXs());
    assertEquals("x", VOLS_1.getXLabel());
    assertEquals("x1", VOLS_2.getXLabel());
    assertArrayEquals(YS, VOLS_1.getYs());
    assertEquals("y", VOLS_1.getYLabel());
    assertEquals("y1", VOLS_2.getYLabel());
    assertEquals(new TreeSet<>(Arrays.asList(1., 2.)), VOLS_1.getUniqueXValues());
    assertEquals(VALUES, VOLS_1.asMap());
    assertEquals("def", VOLS_1.getDefinitionName());
    assertEquals("spec", VOLS_1.getSpecificationName());
    assertEquals(Currency.EUR, VOLS_1.getTarget());
    assertEquals(Currency.EUR, VOLS_1.getCurrency());
  }

  /**
   * Tests the hashCode method.
   */
  @Test
  public void testHashCode() {
    final VolatilitySurfaceData<Double, Double> other = new VolatilitySurfaceData<>("def", "spec", Currency.EUR, XS, YS, VALUES);
    assertEquals(VOLS_1.hashCode(), other.hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    VolatilitySurfaceData<Double, Double> other = new VolatilitySurfaceData<>("def", "spec", Currency.EUR, XS, YS, VALUES);
    assertEquals(VOLS_1, VOLS_1);
    assertNotEquals(null, VOLS_1);
    assertNotEquals(VALUES, VOLS_1);
    assertEquals(VOLS_1, other);
    other = new VolatilitySurfaceData<>("def1", "spec", Currency.EUR, XS, YS, VALUES);
    assertNotEquals(VOLS_1, other);
    other = new VolatilitySurfaceData<>("def", "spec1", Currency.EUR, XS, YS, VALUES);
    assertNotEquals(VOLS_1, other);
    other = new VolatilitySurfaceData<>("def", "spec", Currency.USD, XS, YS, VALUES);
    assertNotEquals(VOLS_1, other);
    other = new VolatilitySurfaceData<>("def", "spec", Currency.EUR, XS, XS, VALUES);
    assertNotEquals(VOLS_1, other);
    other = new VolatilitySurfaceData<>("def", "spec", Currency.EUR, YS, YS, VALUES);
    assertNotEquals(VOLS_1, other);
    other = new VolatilitySurfaceData<>("def", "spec", Currency.EUR, XS, "x1", YS, "y", VALUES);
    assertNotEquals(VOLS_1, other);
    other = new VolatilitySurfaceData<>("def", "spec", Currency.EUR, XS, "x", YS, "y1", VALUES);
    assertNotEquals(VOLS_1, other);
    final Map<Pair<Double, Double>, Double> values = new HashMap<>();
    for (int i = 0; i < XS.length; i++) {
      values.put(Pairs.of(YS[i], XS[i]), VOLS[i]);
    }
    other = new VolatilitySurfaceData<>("def", "spec", Currency.EUR, XS, YS, values);
    assertNotEquals(VOLS_1, other);
    for (int i = 0; i < XS.length; i++) {
      assertEquals(VOLS_1.getVolatility(XS[i], YS[i]), VOLS[i]);
      assertNull(VOLS_1.getVolatility(XS[i] * -1, YS[i]));
    }
  }

  /**
   * Tests the exception when there are no values.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoValues() {
    VOLS_1.getYValuesForX(-1.);
  }

}
