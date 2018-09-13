/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 * Tests the volatility cube data object.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCubeDataTest extends AbstractFudgeBuilderTestCase {
  private static final Double[] XS = new Double[] {1., 2.};
  private static final Double[] YS = new Double[] {10., 20.};
  private static final Double[] ZS = new Double[] {100., 200.};
  private static final Map<Triple<Double, Double, Double>, Double> VALS = new HashMap<>();
  static {
    for (final Double x : XS) {
      for (final Double y : YS) {
        for (final Double z : ZS) {
          VALS.put(Triple.of(x, y, z), x * y * z);
        }
      }
    }
  }
  private static final String DEFINITION_NAME = "def";
  private static final String SPECIFICATION_NAME = "spec";
  private static final VolatilityCubeData<Double, Double, Double> CUBE = new VolatilityCubeData<>(DEFINITION_NAME, SPECIFICATION_NAME, VALS);

  /**
   * Tests a rectangular cube.
   */
  @Test
  public void testRectangular() {
    final Double[] uniqueXValues = new Double[] {1., 2., 3., 4., 5., 6., 7. };
    final Double[] uniqueYValues = new Double[] {100., 110., 120., 130., 140., 150., 160., 170., 180., 190. };
    final Double[] uniqueZValues = new Double[] {1100., 1110., 1120., 1130., 1140., 1150., 1160., 1170., 1180., 1190. };

    final int xLength = uniqueXValues.length;
    final int yLength = uniqueYValues.length;
    final int zLength = uniqueZValues.length;
    final double[][][] vols = new double[xLength][yLength][zLength];
    final Double[] xs = new Double[xLength * yLength * zLength];
    final Double[] ys = new Double[xLength * yLength * zLength];
    final Double[] zs = new Double[xLength * yLength * zLength];
    final Map<Triple<Object, Object, Object>, Double> values = new HashMap<>();
    int count = 0;
    for (int i = 0, v = 0; i < xLength; i++) {
      for (int j = 0; j < yLength; j++) {
        for (int k = 0; k < zLength; k++, v++) {
          vols[i][j][k] = v;
          xs[count] = uniqueXValues[i];
          ys[count] = uniqueYValues[j];
          zs[count++] = uniqueZValues[k];
          values.put(Triple.<Object, Object, Object>of(uniqueXValues[i], uniqueYValues[j], uniqueZValues[k]), vols[i][j][k]);
        }
      }
    }
    final String name = "test";
    final VolatilityCubeData<Object, Object, Object> data = new VolatilityCubeData<>(name, name, values);
    final String xLabel = "time";
    final String yLabel = "strike";
    final String zLabel = "maturity";
    final VolatilityCubeData<Object, Object, Object> dataWithLabels = new VolatilityCubeData<>(name, name, xLabel, yLabel, zLabel, values);
    assertEquals(xs.length, data.getXs().length);
    assertTrue(Arrays.asList(xs).containsAll(Arrays.asList(data.getXs())));
    assertEquals(ys.length, data.getYs().length);
    assertTrue(Arrays.asList(ys).containsAll(Arrays.asList(data.getYs())));
    assertEquals(zs.length, data.getZs().length);
    assertTrue(Arrays.asList(zs).containsAll(Arrays.asList(data.getZs())));
    assertArrayEquals(uniqueXValues, data.getUniqueXValues().toArray(new Double[xLength]));
    assertArrayEquals(uniqueYValues, data.getUniqueYValues().toArray(new Double[yLength]));
    assertEquals(xs.length, dataWithLabels.getXs().length);
    assertTrue(Arrays.asList(xs).containsAll(Arrays.asList(dataWithLabels.getXs())));
    assertEquals(ys.length, dataWithLabels.getYs().length);
    assertTrue(Arrays.asList(ys).containsAll(Arrays.asList(dataWithLabels.getYs())));
    assertEquals(zs.length, dataWithLabels.getZs().length);
    assertTrue(Arrays.asList(zs).containsAll(Arrays.asList(dataWithLabels.getZs())));
    assertArrayEquals(uniqueXValues, dataWithLabels.getUniqueXValues().toArray(new Double[xLength]));
    assertArrayEquals(uniqueYValues, dataWithLabels.getUniqueYValues().toArray(new Double[yLength]));
    assertEquals("x", data.getXLabel());
    assertEquals("y", data.getYLabel());
    assertEquals("z", data.getZLabel());
    assertEquals(xLabel, dataWithLabels.getXLabel());
    assertEquals(yLabel, dataWithLabels.getYLabel());
    assertEquals(zLabel, dataWithLabels.getZLabel());
    int i = 0;
    for (final Object x : data.getUniqueXValues()) {
      int j = 0;
      for (final Object y : data.getUniqueYValues()) {
        final List<ObjectsPair<Object, Double>> strips = data.getZValuesForXandY(x, y);
        final List<ObjectsPair<Object, Double>> stripsWithLabels = dataWithLabels.getZValuesForXandY(x, y);
        int k = 0;
        for (final ObjectsPair<Object, Double> strip : strips) {
          assertEquals(uniqueZValues[k], (Double) strip.getFirst(), 0);
          assertEquals(vols[i][j][k++], strip.getSecond(), 0);
        }
        j++;
        assertEquals(strips, stripsWithLabels);
      }
      i++;
    }
  }

  /**
   * Tests a sparsely-populated cube.
   */
  @Test
  public void testSparse() {
    final Double[] xs = new Double[] {1., 1., 1., 1., 1., 1., 2., 2., 2., 2., 3., 3., 3., 3., 3., 3., 4., 4. };
    final Double[] ys = new Double[] {4., 5., 6., 4., 5., 6., 4., 5., 4., 5., 5., 6., 7., 5., 6., 7., 8., 9. };
    final Double[] zs = new Double[] {14., 15., 16., 24., 25., 26., 14., 15., 24., 25., 15., 16., 17., 25., 26., 27., 18., 28. };
    final double[] vols = new double[] {10., 11., 12., 13., 14., 15., 16., 17., 18., 210., 211., 212., 213., 214., 215., 216., 217., 218. };
    final Map<Triple<Object, Object, Object>, Double> values = new HashMap<>();
    for (int i = 0; i < xs.length; i++) {
      values.put(Triple.<Object, Object, Object>of(xs[i], ys[i], zs[i]), vols[i]);
    }
    final String name = "test";
    final VolatilityCubeData<Object, Object, Object> data = new VolatilityCubeData<>(name, name, values);
    assertEquals(xs.length, data.getXs().length);
    assertTrue(Arrays.asList(xs).containsAll(Arrays.asList(data.getXs())));
    assertEquals(ys.length, data.getYs().length);
    assertTrue(Arrays.asList(ys).containsAll(Arrays.asList(data.getYs())));
    assertEquals(zs.length, data.getZs().length);
    assertTrue(Arrays.asList(zs).containsAll(Arrays.asList(data.getZs())));
    assertArrayEquals(new Double[] {1., 2., 3., 4. }, data.getUniqueXValues().toArray(new Double[3]));
    assertArrayEquals(new Double[] {4., 5., 6., 7., 8., 9. }, data.getUniqueYValues().toArray(new Double[6]));
    assertEquals(Arrays.asList(Pairs.of(14., 10.), Pairs.of(24., 13.)), data.getZValuesForXandY(1., 4.));
    assertEquals(Arrays.asList(Pairs.of(14., 16.), Pairs.of(24., 18.)), data.getZValuesForXandY(2., 4.));
    assertEquals(Arrays.asList(Pairs.of(15., 211.), Pairs.of(25., 214.)), data.getZValuesForXandY(3., 5.));
    assertEquals(Arrays.asList(Pairs.of(18., 217.)), data.getZValuesForXandY(4., 8.));
  }

  /**
   * Tests the error when there are no values for x and y.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoZValue() {
    CUBE.getZValuesForXandY(100., 200.);
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    assertEquals(CUBE.size(), 8);
    for (final Double x : XS) {
      for (final Double y : YS) {
        for (final Double z : ZS) {
          assertEquals(CUBE.getVolatility(x, y, z), x * y * z);
        }
      }
    }
    assertEquals(CUBE.asMap(), VALS);
    final Map<Triple<Double, Double, Double>, Double> newVals = Collections.singletonMap(Triple.of(1., 2., 3.), 10.);
    final VolatilityCubeData<Double, Double, Double> newCube = new VolatilityCubeData<>(DEFINITION_NAME, SPECIFICATION_NAME, VALS);
    newCube.setValues(newVals);
    assertEquals(newCube, new VolatilityCubeData<>(DEFINITION_NAME, SPECIFICATION_NAME, newVals));
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(CUBE, CUBE);
    assertNotEquals(null, CUBE);
    assertNotEquals(VALS, CUBE);
    VolatilityCubeData<Double, Double, Double> other = new VolatilityCubeData<>(DEFINITION_NAME, SPECIFICATION_NAME, VALS);
    assertEquals(CUBE, other);
    assertEquals(CUBE.hashCode(), other.hashCode());
    other = new VolatilityCubeData<>(SPECIFICATION_NAME, SPECIFICATION_NAME, VALS);
    assertNotEquals(CUBE, other);
    other = new VolatilityCubeData<>(DEFINITION_NAME, DEFINITION_NAME, VALS);
    assertNotEquals(CUBE, other);
    other = new VolatilityCubeData<>(DEFINITION_NAME, SPECIFICATION_NAME, VALS);
    other.setValues(Collections.singletonMap(Triple.of(1., 2., 3.), 10.));
    assertNotEquals(CUBE, other);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(VolatilityCubeData.class, CUBE);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(CUBE.metaBean());
    assertNotNull(CUBE.metaBean().definitionName());
    assertNotNull(CUBE.metaBean().specificationName());
    assertNotNull(CUBE.metaBean().values());
    assertNotNull(CUBE.metaBean().xs());
    assertNotNull(CUBE.metaBean().xLabel());
    assertNotNull(CUBE.metaBean().ys());
    assertNotNull(CUBE.metaBean().yLabel());
    assertNotNull(CUBE.metaBean().zs());
    assertNotNull(CUBE.metaBean().zLabel());
    assertEquals(CUBE.metaBean().definitionName().get(CUBE), DEFINITION_NAME);
    assertEquals(CUBE.metaBean().specificationName().get(CUBE), SPECIFICATION_NAME);
    assertEquals(CUBE.metaBean().values().get(CUBE), VALS);
    assertEquals(CUBE.metaBean().xs().get(CUBE), CUBE.getXs());
    assertEquals(CUBE.metaBean().xLabel().get(CUBE), CUBE.getXLabel());
    assertEquals(CUBE.metaBean().ys().get(CUBE), CUBE.getYs());
    assertEquals(CUBE.metaBean().yLabel().get(CUBE), CUBE.getYLabel());
    assertEquals(CUBE.metaBean().zs().get(CUBE), CUBE.getZs());
    assertEquals(CUBE.metaBean().zLabel().get(CUBE), CUBE.getZLabel());
    assertEquals(CUBE.property("definitionName").get(), DEFINITION_NAME);
    assertEquals(CUBE.property("specificationName").get(), SPECIFICATION_NAME);
    assertEquals(CUBE.property("values").get(), VALS);
    assertEquals(CUBE.property("xs").get(), CUBE.getXs());
    assertEquals(CUBE.property("xLabel").get(), CUBE.getXLabel());
    assertEquals(CUBE.property("ys").get(), CUBE.getYs());
    assertEquals(CUBE.property("yLabel").get(), CUBE.getYLabel());
    assertEquals(CUBE.property("zs").get(), CUBE.getZs());
    assertEquals(CUBE.property("zLabel").get(), CUBE.getZLabel());
  }

}
