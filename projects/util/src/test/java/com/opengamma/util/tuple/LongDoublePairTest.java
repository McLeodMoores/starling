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
 * Test {@link LongDoublePair}.
 */
@Test(groups = TestGroup.UNIT)
public class LongDoublePairTest {
  private static final double EPS = 1e-15;

  /**
   * Tests construction with an long and double.
   */
  @Test
  public void testLongDoublePairOf() {
    final LongDoublePair test = LongDoublePair.of(2L, 2.5d);
    assertEquals(Long.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
    assertEquals(2L, test.getFirstLong());
    assertEquals(2.5d, test.getSecondDouble(), EPS);
    assertEquals(Long.valueOf(2), test.getKey());
    assertEquals(Double.valueOf(2.5d), test.getValue());
    assertEquals(2L, test.getLongKey());
    assertEquals(2.5d, test.getDoubleValue(), EPS);
    assertEquals("[2, 2.5]", test.toString());
  }

  /**
   * Tests construction with a null Pair<Long, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPair() {
    LongDoublePair.of((Pair<Long, Double>) null);
  }

  /**
   * Tests construction with a null first value in Pair<Long, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPairNullFirst() {
    LongDoublePair.of(ObjectsPair.of((Long) null, Double.valueOf(2.3)));
  }

  /**
   * Tests construction with a null second value in Pair<Long, Double>.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPairNullSecond() {
    LongDoublePair.of(ObjectsPair.of(Long.valueOf(1), (Double) null));
  }

  /**
   * Tests construction with a LongDoublePair.
   */
  @Test
  public void testLongDoublePairOfDLongoublePair() {
    final LongDoublePair test = LongDoublePair.of(LongDoublePair.of(1L, 2.5d));
    assertEquals(test.getFirst(), 1, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
  }

  /**
   * Tests construction with a Pair<Long, Double>.
   */
  @Test
  public void testDoublesPairOfDoublesPair() {
    final LongDoublePair test = LongDoublePair.of(ObjectsPair.of(Long.valueOf(1), Double.valueOf(2.5)));
    assertEquals(test.getFirst(), 1, EPS);
    assertEquals(test.getSecond(), 2.5, EPS);
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testShortString() {
    LongDoublePair.parse("4, 5");
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOpeningBracket() {
    LongDoublePair.parse("2, 3]");
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testClosingBracket() {
    LongDoublePair.parse("[2, 3");
  }

  /**
   * Tests the parse() method.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTooManyValues() {
    LongDoublePair.parse("[2, 3, 4]");
  }

  /**
   * Tests the parse() method.
   */
  @Test
  public void testLongDoublePairParse1() {
    final LongDoublePair test = LongDoublePair.parse("[2, 2.5]");
    assertEquals(Long.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  /**
   * Tests the parse() method.
   */
  @Test
  public void testLongDoublePairParse2() {
    final LongDoublePair test = LongDoublePair.parse("[2,2.5]");
    assertEquals(Long.valueOf(2), test.getFirst());
    assertEquals(Double.valueOf(2.5d), test.getSecond());
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the set method cannot be used.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue() {
    final LongDoublePair pair = LongDoublePair.of(2L, -0.3d);
    pair.setValue(Double.valueOf(1.2d));
  }

  /**
   * Checks that the set method cannot be used.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValueNull() {
    final LongDoublePair pair = LongDoublePair.of(2L, -0.3d);
    pair.setValue(null);
  }

  /**
   * Checks that the set method cannot be used.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValuePrimitives() {
    final LongDoublePair pair = LongDoublePair.of(2L, -0.3d);
    pair.setValue(1.2d);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the compareTo() method.
   */
  @Test
  public void testCompareToLongDoublePair() {
    final LongDoublePair p12 = LongDoublePair.of(1L, 2d);
    final LongDoublePair p13 = LongDoublePair.of(1L, 3d);
    final LongDoublePair p21 = LongDoublePair.of(2L, 1d);

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
  public void testCompareToLongDoublePairAsPair() {
    final Pair<Long, Double> p12 = LongDoublePair.of(1L, 2d);
    final Pair<Long, Double> p13 = LongDoublePair.of(1L, 3d);
    final Pair<Long, Double> p21 = LongDoublePair.of(2L, 1d);

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
    final LongDoublePair ab = LongDoublePair.of(1L, 1.7d);
    final LongDoublePair ac = LongDoublePair.of(1L, 1.9d);
    final LongDoublePair ba = LongDoublePair.of(2L, 1.5d);

    final FirstThenSecondPairComparator<Long, Double> comparator = new FirstThenSecondPairComparator<>();
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
    final LongDoublePair a = LongDoublePair.of(1L, 2.0);
    final LongDoublePair b = LongDoublePair.of(1L, 3.0);
    final LongDoublePair c = LongDoublePair.of(2L, 2.0);
    final LongDoublePair d = LongDoublePair.of(2L, 3.0);
    final LongDoublePair e = LongDoublePair.of(1L, 2.0);

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
    final LongDoublePair a = LongDoublePair.of(1L, 1.7d);
    final Pair<Long, Double> b = ObjectsPair.of(Long.valueOf(1), Double.valueOf(1.7d));
    assertEquals(true, a.equals(b));
    assertEquals(true, b.equals(a));
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsToObjectVersionNull() {
    final LongDoublePair b = LongDoublePair.of(1L, 1.7d);
    final Pair<Long, Double> a = ObjectsPair.of(null, Double.valueOf(1.9d));
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
    final LongDoublePair a = LongDoublePair.of(1L, 1.7d);
    final Pair<Long, Double> b = ObjectsPair.of(Long.valueOf(1), Double.valueOf(1.7d));
    assertEquals(a.hashCode(), b.hashCode());
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCodeValue() {
    final LongDoublePair a = LongDoublePair.of(1L, 2.0);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(Long.valueOf(1).hashCode() ^ Double.valueOf(2.0).hashCode(), a.hashCode());
    // can't test for different hash codes as they might not be different
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final LongDoublePair pair = LongDoublePair.of(2L, 3.);
    assertNotNull(pair.metaBean());
    assertNotNull(pair.metaBean().first());
    assertNotNull(pair.metaBean().second());
    assertEquals(pair.metaBean().first().get(pair), 2, EPS);
    assertEquals(pair.metaBean().second().get(pair), 3., EPS);
    assertEquals(pair.property("first").get(), 2L);
    assertEquals(pair.property("second").get(), 3.);
  }

}
