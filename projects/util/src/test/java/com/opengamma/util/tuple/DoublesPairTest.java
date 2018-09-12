/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link DoublesPair}.
 */
@Test(groups = TestGroup.UNIT)
public class DoublesPairTest {
  private static final double EPS = 1e-15;

  /**
   * Tests construction with two Doubles.
   */
  @Test
  public void testDoublesPairOfDoubleDouble() {
    final DoublesPair test = DoublesPair.of(1.2d, 2.5d);
    assertEquals(test.getFirst(), 1.2, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
    assertEquals(test.getFirstDouble(), 1.2, EPS);
    assertEquals(test.getSecondDouble(), 2.5, EPS);
    assertEquals(test.getKey(), 1.2, EPS);
    assertEquals(test.getValue(), 2.5, EPS);
    assertEquals(test.getDoubleKey(), 1.2, EPS);
    assertEquals(test.getDoubleValue(), 2.5, EPS);
    assertEquals(test.toString(), "[1.2, 2.5]");
  }

  /**
   * Tests construction with a null DoublesPair.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDoublesPair() {
    DoublesPair.of(null);
  }

  /**
   * Tests construction with a null Pair<Double, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPair() {
    DoublesPair.of((Pair<Double, Double>) null);
  }

  /**
   * Tests construction with a null first value in Pair<Double, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPairNullFirst() {
    DoublesPair.of(ObjectsPair.of((Double) null, Double.valueOf(2.3)));
  }

  /**
   * Tests construction with a null second value in Pair<Double, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPairNullSecond() {
    DoublesPair.of(ObjectsPair.of(Double.valueOf(1.2), (Double) null));
  }

  /**
   * Tests construction with a DoublesPair.
   */
  @Test
  public void testDoublesPairOfDoublesPair() {
    final DoublesPair test = DoublesPair.of(DoublesPair.of(1.2d, 2.5d));
    assertEquals(test.getFirst(), 1.2, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
  }

  /**
   * Tests construction with a Pair<Double, Double>.
   */
  @Test
  public void testDoublesPairOfPair() {
    DoublesPair test = DoublesPair.of(ObjectsPair.of(Double.valueOf(1.2d), Double.valueOf(2.5d)));
    assertEquals(test.getFirst(), 1.2, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
    test = DoublesPair.of((Pair<Double, Double>) DoublesPair.of(1.2, 2.5));
    assertEquals(test.getFirst(), 1.2, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
  }

  /**
   * Tests construction with a null Pair<Double, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPairOfNumbers() {
    DoublesPair.ofNumbers((Pair<Double, Double>) null);
  }

  /**
   * Tests construction with a null first value in Pair<Double, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPairNullFirstNumber() {
    DoublesPair.ofNumbers(ObjectsPair.of((Double) null, Double.valueOf(2.3)));
  }

  /**
   * Tests construction with a null second value.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPairNullSecondNumber() {
    DoublesPair.ofNumbers(ObjectsPair.of(Double.valueOf(1.2), (Double) null));
  }

  /**
   * Tests construction with ofNumbers().
   */
  @Test
  public void testDoublesPairOfNumber() {
    DoublesPair test = DoublesPair.ofNumbers(ObjectsPair.of(Double.valueOf(1.2d), Double.valueOf(2.5d)));
    assertEquals(test.getFirst(), 1.2, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
    test = DoublesPair.ofNumbers(DoublesPair.of(1.2d, 2.5));
    assertEquals(test.getFirst(), 1.2, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testShortString() {
    DoublesPair.parse("4, 5");
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOpeningBracket() {
    DoublesPair.parse("2, 3]");
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testClosingBracket() {
    DoublesPair.parse("[2, 3");
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTooManyValues() {
    DoublesPair.parse("[2, 3, 4]");
  }

  /**
   * Tests the parse() method.
   */
  @Test
  public void testDoublesPairParse1() {
    final DoublesPair test = DoublesPair.parse("[1.2, 2.5]");
    assertEquals(Double.valueOf(1.2d), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  /**
   * Tests the parse() method.
   */
  @Test
  public void testDoublesPairParse2() {
    final DoublesPair test = DoublesPair.parse("[1.2,2.5]");
    assertEquals(Double.valueOf(1.2d), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  //-------------------------------------------------------------------------
  /**
   * Sets that the set method cannot be used.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue() {
    final DoublesPair pair = DoublesPair.of(2.1d, -0.3d);
    pair.setValue(Double.valueOf(1.2d));
  }

  /**
   * Tests that the set method cannot be used.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValueNull() {
    final DoublesPair pair = DoublesPair.of(2.1d, -0.3d);
    pair.setValue(null);
  }

  /**
   * Tests that the set method cannot be used.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValuePrimitives() {
    final DoublesPair pair = DoublesPair.of(2.1d, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToDoublesPair() {
    final DoublesPair p12 = DoublesPair.of(1d, 2d);
    final DoublesPair p13 = DoublesPair.of(1d, 3d);
    final DoublesPair p21 = DoublesPair.of(2d, 1d);

    assertEquals(p12.compareTo(p12), 0);
    assertTrue(p12.compareTo(p13) < 0);
    assertTrue(p12.compareTo(p21) < 0);

    assertTrue(p13.compareTo(p12) > 0);
    assertEquals(p13.compareTo(p13), 0);
    assertTrue(p13.compareTo(p21) < 0);

    assertTrue(p21.compareTo(p12) > 0);
    assertTrue(p21.compareTo(p13) > 0);
    assertEquals(p21.compareTo(p21), 0);
  }

  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToDoublesPairAsPair() {
    final Pair<Double, Double> p12 = DoublesPair.of(1d, 2d);
    final Pair<Double, Double> p13 = DoublesPair.of(1d, 3d);
    final Pair<Double, Double> p21 = DoublesPair.of(2d, 1d);

    assertEquals(p12.compareTo(p12), 0);
    assertTrue(p12.compareTo(p13) < 0);
    assertTrue(p12.compareTo(p21) < 0);

    assertTrue(p13.compareTo(p12) > 0);
    assertEquals(p13.compareTo(p13), 0);
    assertTrue(p13.compareTo(p21) < 0);

    assertTrue(p21.compareTo(p12) > 0);
    assertTrue(p21.compareTo(p13) > 0);
    assertEquals(p21.compareTo(p21), 0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final DoublesPair a = DoublesPair.of(1d, 2.0d);
    final DoublesPair b = DoublesPair.of(1d, 3.0d);
    final DoublesPair c = DoublesPair.of(2d, 2.0d);
    final DoublesPair d = DoublesPair.of(2d, 3.0d);
    final DoublesPair e = DoublesPair.of(1d, 2d);

    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, a.equals(c));
    assertEquals(false, a.equals(d));
    assertEquals(true, a.equals(e));

    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    assertEquals(false, b.equals(c));
    assertEquals(false, b.equals(d));
    assertEquals(false, b.equals(e));

    assertEquals(false, c.equals(a));
    assertEquals(false, c.equals(b));
    assertEquals(true, c.equals(c));
    assertEquals(false, c.equals(d));
    assertEquals(false, c.equals(e));

    assertEquals(false, d.equals(a));
    assertEquals(false, d.equals(b));
    assertEquals(false, d.equals(c));
    assertEquals(true, d.equals(d));
    assertEquals(false, d.equals(e));
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsToObjectVersion() {
    final DoublesPair a = DoublesPair.of(1.1d, 1.7d);
    final Pair<Double, Double> b = ObjectsPair.of(Double.valueOf(1.1d), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsToObjectVersionNull() {
    final DoublesPair b = DoublesPair.of(1.1d, 1.7d);
    final Pair<Double, Double> a = ObjectsPair.of(null, Double.valueOf(1.9d));
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final DoublesPair a = DoublesPair.of(1.1d, 1.7d);
    final Pair<Double, Double> b = ObjectsPair.of(Double.valueOf(1.1d), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCodeValue() {
    final DoublesPair a = DoublesPair.of(1.1d, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(Double.valueOf(1.1d).hashCode() ^ Double.valueOf(2.0).hashCode(), a.hashCode());
    // can't test for different hash codes as they might not be different
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final DoublesPair pair = DoublesPair.of(2., 3.);
    assertNotNull(pair.metaBean());
    assertNotNull(pair.metaBean().first());
    assertNotNull(pair.metaBean().second());
    assertEquals(pair.metaBean().first().get(pair), 2.);
    assertEquals(pair.metaBean().second().get(pair), 3.);
    assertEquals(pair.property("first").get(), 2.);
    assertEquals(pair.property("second").get(), 3.);
  }
}
