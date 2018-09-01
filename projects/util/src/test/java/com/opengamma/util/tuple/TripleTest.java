/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link Triple}.
 */
@Test(groups = TestGroup.UNIT)
public class TripleTest {

  /**
   * Tests Triple<Object, Object, Object>.
   */
  @Test
  public void testTripleObjectObjectObject() {
    final Triple<String, String, String> test = Triple.of("A", "B", "C");
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getThird(), "C");
  }

  /**
   * Tests Triple<Object, Object, null>.
   */
  @Test
  public void testTripleObjectObjectNull() {
    final Triple<String, String, String> test = Triple.of("A", "B", null);
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getThird(), null);
  }

  /**
   * Tests Triple<Object, null, Object>.
   */
  @Test
  public void testTripleObjectNullObject() {
    final Triple<String, String, String> test = Triple.of("A", null, "C");
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), null);
    assertEquals(test.getThird(), "C");
  }

  /**
   * Tests Triple<Object, null, null>.
   */
  @Test
  public void testTripleObjectNullNull() {
    final Triple<String, String, String> test = Triple.of("A", null, null);
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), null);
    assertEquals(test.getThird(), null);
  }

  /**
   * Tests Triple<null, Object, Object>.
   */
  @Test
  public void testTripleNullObjectObject() {
    final Triple<String, String, String> test = Triple.of(null, "B", "C");
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getThird(), "C");
  }

  /**
   * Tests Triple<null, Object, null>.
   */
  @Test
  public void testTripleNullObjectNull() {
    final Triple<String, String, String> test = Triple.of(null, "B", null);
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getThird(), null);
  }

  /**
   * Tests Triple<null, null, Object>.
   */
  @Test
  public void testTripleNullNullObject() {
    final Triple<String, String, String> test = Triple.of(null, null, "C");
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), null);
    assertEquals(test.getThird(), "C");
  }

  /**
   * Tests Triple<null, null, null>.
   */
  @Test
  public void testTripleNullNullNull() {
    final Triple<String, String, String> test = Triple.of(null, null, null);
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), null);
    assertEquals(test.getThird(), null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests compareTo().
   */
  @Test
  public void compareTo() {
    final Triple<String, String, String> abc = Triple.of("A", "B", "C");
    final Triple<String, String, String> abd = Triple.of("A", "B", "D");
    final Triple<String, String, String> acc = Triple.of("A", "C", "C");
    final Triple<String, String, String> bac = Triple.of("B", "A", "C");

    assertEquals(abc.compareTo(abc), 0);
    assertTrue(abc.compareTo(abd) < 0);
    assertTrue(abc.compareTo(acc) < 0);
    assertTrue(abc.compareTo(bac) < 0);

    assertTrue(abd.compareTo(abc) > 0);
    assertEquals(abd.compareTo(abd), 0);
    assertTrue(abd.compareTo(acc) < 0);
    assertTrue(abd.compareTo(bac) < 0);

    assertTrue(acc.compareTo(abc) > 0);
    assertTrue(acc.compareTo(abd) > 0);
    assertEquals(acc.compareTo(acc), 0);
    assertTrue(acc.compareTo(bac) < 0);

    assertTrue(bac.compareTo(abc) > 0);
    assertTrue(bac.compareTo(abd) > 0);
    assertTrue(bac.compareTo(acc) > 0);
    assertEquals(bac.compareTo(bac), 0);
  }

  /**
   * Tests compareTo().
   */
  @Test
  public void compareToNull() {
    final Triple<String, String, String> nnn = Triple.of(null, null, null);
    final Triple<String, String, String> naa = Triple.of(null, "A", "A");
    final Triple<String, String, String> ann = Triple.of("A", null, null);
    final Triple<String, String, String> ana = Triple.of("A", null, "A");
    final Triple<String, String, String> aan = Triple.of("A", "A", null);
    final Triple<String, String, String> aaa = Triple.of("A", "A", "A");

    assertEquals(nnn.compareTo(nnn), 0);
    assertTrue(nnn.compareTo(naa) < 0);
    assertTrue(nnn.compareTo(ann) < 0);
    assertTrue(nnn.compareTo(ana) < 0);
    assertTrue(nnn.compareTo(aan) < 0);
    assertTrue(nnn.compareTo(aaa) < 0);

    assertTrue(naa.compareTo(nnn) > 0);
    assertEquals(naa.compareTo(naa), 0);
    assertTrue(naa.compareTo(ann) < 0);
    assertTrue(naa.compareTo(ana) < 0);
    assertTrue(naa.compareTo(aan) < 0);
    assertTrue(naa.compareTo(aaa) < 0);

    assertTrue(ann.compareTo(nnn) > 0);
    assertTrue(ann.compareTo(naa) > 0);
    assertEquals(ann.compareTo(ann), 0);
    assertTrue(ann.compareTo(ana) < 0);
    assertTrue(ann.compareTo(aan) < 0);
    assertTrue(ann.compareTo(aaa) < 0);

    assertTrue(ana.compareTo(nnn) > 0);
    assertTrue(ana.compareTo(naa) > 0);
    assertTrue(ana.compareTo(ann) > 0);
    assertEquals(ana.compareTo(ana), 0);
    assertTrue(ana.compareTo(aan) < 0);
    assertTrue(ana.compareTo(aaa) < 0);

    assertTrue(aan.compareTo(nnn) > 0);
    assertTrue(aan.compareTo(naa) > 0);
    assertTrue(aan.compareTo(ann) > 0);
    assertTrue(aan.compareTo(ana) > 0);
    assertEquals(aan.compareTo(aan), 0);
    assertTrue(aan.compareTo(aaa) < 0);

    assertTrue(aaa.compareTo(nnn) > 0);
    assertTrue(aaa.compareTo(naa) > 0);
    assertTrue(aaa.compareTo(ann) > 0);
    assertTrue(aaa.compareTo(ana) > 0);
    assertTrue(aaa.compareTo(aan) > 0);
    assertEquals(aaa.compareTo(aaa), 0);
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final Triple<Integer, String, String> a = Triple.of(1, "Hello", "C");
    final Triple<Integer, String, String> b = Triple.of(1, "Goodbye", "C");
    final Triple<Integer, String, String> c = Triple.of(2, "Hello", "C");
    final Triple<Integer, String, String> d = Triple.of(2, "Goodbye", "C");
    final Triple<Integer, String, String> e = Triple.of(2, "Goodbye", "D");
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);
    assertEquals(a.equals(e), false);

    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);
    assertEquals(b.equals(e), false);

    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);
    assertEquals(c.equals(e), false);

    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);
    assertEquals(d.equals(e), false);

    assertEquals(e.equals(a), false);
    assertEquals(e.equals(b), false);
    assertEquals(e.equals(c), false);
    assertEquals(e.equals(d), false);
    assertEquals(e.equals(e), true);

    assertNotEquals("RUBBISH", e);
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsNull() {
    final Triple<Integer, String, String> a = Triple.of(1, "Hello", "C");
    final Triple<Integer, String, String> b = Triple.of(null, "Hello", "C");
    final Triple<Integer, String, String> c = Triple.of(1, null, "C");
    final Triple<Integer, String, String> d = Triple.of(null, null, "C");
    final Triple<Integer, String, String> e = Triple.of(null, null, null);
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);
    assertEquals(a.equals(e), false);

    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);
    assertEquals(b.equals(e), false);

    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);
    assertEquals(c.equals(e), false);

    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);
    assertEquals(d.equals(e), false);

    assertEquals(e.equals(a), false);
    assertEquals(e.equals(b), false);
    assertEquals(e.equals(c), false);
    assertEquals(e.equals(d), false);
    assertEquals(e.equals(e), true);
  }

  /**
   * Tests the hashCode().
   */
  @Test
  public void testHashCode() {
    final Triple<Integer, String, String> a = Triple.of(1, "Hello", "C");
    final Triple<Integer, String, String> b = Triple.of(null, "Hello", "C");
    final Triple<Integer, String, String> c = Triple.of(1, null, "C");
    final Triple<Integer, String, String> d = Triple.of(null, null, "C");
    final Triple<Integer, String, String> e = Triple.of(null, null, null);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(b.hashCode(), b.hashCode());
    assertEquals(c.hashCode(), c.hashCode());
    assertEquals(d.hashCode(), d.hashCode());
    assertEquals(e.hashCode(), e.hashCode());
    // can't test for different hash codes as they might not be different
  }

  /**
   * Tests the toList() method.
   */
  @Test
  public void toList() {
    final Triple<String, String, String> a = Triple.of("Jay-Z", "Black Album", "99 Problems");
    final List<String> asList = a.toList();
    assertNotNull(asList);
    assertEquals(3, asList.size());
    assertEquals("Jay-Z", asList.get(0));
    assertEquals("Black Album", asList.get(1));
    assertEquals("99 Problems", asList.get(2));
  }

  /**
   * Tests the toFirstPair() method.
   */
  @Test
  public void toFirstPair() {
    final Triple<String, String, String> a = Triple.of("Jay-Z", "Black Album", "99 Problems");
    final Pair<String, String> pair = a.toFirstPair();
    assertNotNull(pair);
    assertEquals("Jay-Z", pair.getFirst());
    assertEquals("Black Album", pair.getSecond());
  }

  /**
   * Tests the toSecondPair() method.
   */
  @Test
  public void toSecondPair() {
    final Triple<String, String, String> a = Triple.of("Jay-Z", "Black Album", "99 Problems");
    final Pair<String, String> pair = a.toSecondPair();
    assertNotNull(pair);
    assertEquals("Black Album", pair.getFirst());
    assertEquals("99 Problems", pair.getSecond());
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    final Triple<String, String, String> test = Triple.of("A", "B", "C");
    assertEquals("[A, B, C]", test.toString());
  }

  /**
   * Tests the bean.
   */
  @Test
  public void bean() {
    final Triple<String, String, String> triple = Triple.of("Jay-Z", "Black Album", "99 Problems");
    assertNotNull(triple.metaBean());
    assertNotNull(triple.metaBean().first());
    assertNotNull(triple.metaBean().second());
    assertNotNull(triple.metaBean().third());
    assertEquals("Jay-Z", triple.metaBean().first().get(triple));
    assertEquals("Black Album", triple.metaBean().second().get(triple));
    assertEquals("99 Problems", triple.metaBean().third().get(triple));
    assertEquals("Jay-Z", triple.property("first").get());
    assertEquals("Black Album", triple.property("second").get());
    assertEquals("99 Problems", triple.property("third").get());
  }

}
