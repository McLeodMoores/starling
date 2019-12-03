/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link IntDoublePair}.
 */
@Test(groups = TestGroup.UNIT)
public class IntDoublePairTest {
  private static final double EPS = 1e-15;

  /**
   * Tests construction with an int and double.
   */
  @Test
  public void testIntDoublePairOf() {
    final IntDoublePair test = IntDoublePair.of(2, 2.5d);
    assertEquals(Integer.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
    assertEquals(2, test.getFirstInt());
    assertEquals(2.5d, test.getSecondDouble(), EPS);
    assertEquals(Integer.valueOf(2), test.getKey());
    assertEquals(Double.valueOf(2.5d), test.getValue());
    assertEquals(2, test.getIntKey());
    assertEquals(2.5d, test.getDoubleValue(), EPS);
    assertEquals("[2, 2.5]", test.toString());
  }

  /**
   * Tests construction with a null Pair<Integer, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPair() {
    IntDoublePair.of((Pair<Integer, Double>) null);
  }

  /**
   * Tests construction with a null first value in Pair<Integer, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPairNullFirst() {
    IntDoublePair.of(ObjectsPair.of((Integer) null, Double.valueOf(2.3)));
  }

  /**
   * Tests construction with a null second value in Pair<Integer, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPairNullSecond() {
    IntDoublePair.of(ObjectsPair.of(Integer.valueOf(1), (Double) null));
  }

  /**
   * Tests construction with a IntDoublePair.
   */
  @Test
  public void testIntDoublePairOfDIntoublePair() {
    final IntDoublePair test = IntDoublePair.of(IntDoublePair.of(1, 2.5d));
    assertEquals(test.getFirst(), 1, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
  }

  /**
   * Tests construction with a Pair<Integer, Double>.
   */
  @Test
  public void testDoublesPairOfDoublesPair() {
    final IntDoublePair test = IntDoublePair.of(ObjectsPair.of(Integer.valueOf(1), Double.valueOf(2.5)));
    assertEquals(test.getFirst(), 1, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testShortString() {
    IntDoublePair.parse("4, 5");
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOpeningBracket() {
    IntDoublePair.parse("2, 3]");
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testClosingBracket() {
    IntDoublePair.parse("[2, 3");
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTooManyValues() {
    IntDoublePair.parse("[2, 3, 4]");
  }

  /**
   * Tests the parse() method.
   */
  @Test
  public void testIntDoublePairParse1() {
    final IntDoublePair test = IntDoublePair.parse("[2, 2.5]");
    assertEquals(Integer.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  /**
   * Tests the parse() method.
   */
  @Test
  public void testIntDoublePairParse2() {
    final IntDoublePair test = IntDoublePair.parse("[2,2.5]");
    assertEquals(Integer.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the set method cannot be used.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue() {
    final IntDoublePair pair = IntDoublePair.of(2, -0.3d);
    pair.setValue(Double.valueOf(1.2d));
  }

  /**
   * Checks that the set method cannot be used.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValueNull() {
    final IntDoublePair pair = IntDoublePair.of(2, -0.3d);
    pair.setValue(null);
  }

  /**
   * Checks that the set method cannot be used.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValuePrimitives() {
    final IntDoublePair pair = IntDoublePair.of(2, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToIntDoublePair() {
    final IntDoublePair p12 = IntDoublePair.of(1, 2d);
    final IntDoublePair p13 = IntDoublePair.of(1, 3d);
    final IntDoublePair p21 = IntDoublePair.of(2, 1d);

    assertTrue(p12.compareTo(p12) == 0);
    assertTrue(p12.compareTo(p13) < 0);
    assertTrue(p12.compareTo(p21) < 0);

    assertTrue(p13.compareTo(p12) > 0);
    assertTrue(p13.compareTo(p13) == 0);
    assertTrue(p13.compareTo(p21) < 0);

    assertTrue(p21.compareTo(p12) > 0);
    assertTrue(p21.compareTo(p13) > 0);
    assertTrue(p21.compareTo(p21) == 0);
  }

  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToIntDoublePairAsPair() {
    final Pair<Integer, Double> p12 = IntDoublePair.of(1, 2d);
    final Pair<Integer, Double> p13 = IntDoublePair.of(1, 3d);
    final Pair<Integer, Double> p21 = IntDoublePair.of(2, 1d);

    assertTrue(p12.compareTo(p12) == 0);
    assertTrue(p12.compareTo(p13) < 0);
    assertTrue(p12.compareTo(p21) < 0);

    assertTrue(p13.compareTo(p12) > 0);
    assertTrue(p13.compareTo(p13) == 0);
    assertTrue(p13.compareTo(p21) < 0);

    assertTrue(p21.compareTo(p12) > 0);
    assertTrue(p21.compareTo(p13) > 0);
    assertTrue(p21.compareTo(p21) == 0);
  }

  /**
   * Tests the comparator.
   */
  @Test
  public void testCompareToComparatorFirstThenSecond() {
    final IntDoublePair ab = IntDoublePair.of(1, 1.7d);
    final IntDoublePair ac = IntDoublePair.of(1, 1.9d);
    final IntDoublePair ba = IntDoublePair.of(2, 1.5d);

    final FirstThenSecondPairComparator<Integer, Double> comparator = new FirstThenSecondPairComparator<>();
    assertTrue(comparator.compare(ab, ab) == 0);
    assertTrue(comparator.compare(ac, ab) > 0);
    assertTrue(comparator.compare(ba, ab) > 0);

    assertTrue(comparator.compare(ab, ac) < 0);
    assertTrue(comparator.compare(ac, ac) == 0);
    assertTrue(comparator.compare(ba, ac) > 0);

    assertTrue(comparator.compare(ab, ba) < 0);
    assertTrue(comparator.compare(ac, ba) < 0);
    assertTrue(comparator.compare(ba, ba) == 0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final IntDoublePair a = IntDoublePair.of(1, 2.0);
    final IntDoublePair b = IntDoublePair.of(1, 3.0);
    final IntDoublePair c = IntDoublePair.of(2, 2.0);
    final IntDoublePair d = IntDoublePair.of(2, 3.0);
    final IntDoublePair e = IntDoublePair.of(1, 2.0);

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
    final IntDoublePair a = IntDoublePair.of(1, 1.7d);
    final Pair<Integer, Double> b = ObjectsPair.of(Integer.valueOf(1), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsToObjectVersionNull() {
    final IntDoublePair b = IntDoublePair.of(1, 1.7d);
    final Pair<Integer, Double> a = ObjectsPair.of(null, Double.valueOf(1.9d));
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
    final IntDoublePair a = IntDoublePair.of(1, 1.7d);
    final Pair<Integer, Double> b = ObjectsPair.of(Integer.valueOf(1), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCodeValue() {
    final IntDoublePair a = IntDoublePair.of(1, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(Integer.valueOf(1).hashCode() ^ Double.valueOf(2.0).hashCode(), a.hashCode());
    // can't test for different hash codes as they might not be different
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final IntDoublePair pair = IntDoublePair.of(2, 3.);
    assertNotNull(pair.metaBean());
    assertNotNull(pair.metaBean().first());
    assertNotNull(pair.metaBean().second());
    assertEquals(pair.metaBean().first().get(pair), 2, EPS);
    assertEquals(pair.metaBean().second().get(pair), 3., EPS);
    assertEquals(pair.property("first").get(), 2);
    assertEquals(pair.property("second").get(), 3.);
  }

}
