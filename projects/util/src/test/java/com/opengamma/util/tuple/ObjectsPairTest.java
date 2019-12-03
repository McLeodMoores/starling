/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ObjectsPair}.
 */
@Test(groups = TestGroup.UNIT)
public class ObjectsPairTest {

  /**
   * Tests construction with two objects.
   */
  @Test
  public void testOfObjectObject() {
    final ObjectsPair<String, String> test = ObjectsPair.of("A", "B");
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
  }

  /**
   * Test that the second value can be null.
   */
  @Test
  public void testObjectsPairObjectNull() {
    final ObjectsPair<String, String> test = ObjectsPair.of("A", null);
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), null);
  }

  /**
   * Tests that the first value can be null.
   */
  @Test
  public void testObjectsPairNullObject() {
    final ObjectsPair<String, String> test = ObjectsPair.of(null, "B");
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), "B");
  }

  /**
   * Tests that both values can be null.
   */
  @Test
  public void testObjectsPairNullNull() {
    final ObjectsPair<String, String> test = ObjectsPair.of(null, null);
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the compareTo() method.
   */
  @Test
  public void compareTo() {
    final ObjectsPair<String, String> ab = ObjectsPair.of("A", "B");
    final ObjectsPair<String, String> ad = ObjectsPair.of("A", "D");
    final ObjectsPair<String, String> ba = ObjectsPair.of("B", "A");

    assertTrue(ab.compareTo(ab) == 0);
    assertTrue(ab.compareTo(ad) < 0);
    assertTrue(ab.compareTo(ba) < 0);

    assertTrue(ad.compareTo(ab) > 0);
    assertTrue(ad.compareTo(ad) == 0);
    assertTrue(ad.compareTo(ba) < 0);

    assertTrue(ba.compareTo(ab) > 0);
    assertTrue(ba.compareTo(ad) > 0);
    assertTrue(ba.compareTo(ba) == 0);
  }

  /**
   * Tests the compareTo() method.
   */
  @Test
  public void compareToNull() {
    final ObjectsPair<String, String> nn = ObjectsPair.of(null, null);
    final ObjectsPair<String, String> na = ObjectsPair.of(null, "A");
    final ObjectsPair<String, String> an = ObjectsPair.of("A", null);
    final ObjectsPair<String, String> aa = ObjectsPair.of("A", "A");

    assertTrue(nn.compareTo(nn) == 0);
    assertTrue(nn.compareTo(na) < 0);
    assertTrue(nn.compareTo(an) < 0);
    assertTrue(nn.compareTo(aa) < 0);

    assertTrue(na.compareTo(nn) > 0);
    assertTrue(na.compareTo(na) == 0);
    assertTrue(na.compareTo(an) < 0);
    assertTrue(na.compareTo(aa) < 0);

    assertTrue(an.compareTo(nn) > 0);
    assertTrue(an.compareTo(na) > 0);
    assertTrue(an.compareTo(an) == 0);
    assertTrue(an.compareTo(aa) < 0);

    assertTrue(aa.compareTo(nn) > 0);
    assertTrue(aa.compareTo(na) > 0);
    assertTrue(aa.compareTo(an) > 0);
    assertTrue(aa.compareTo(aa) == 0);
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final ObjectsPair<Integer, String> a = ObjectsPair.of(1, "Hello");
    final ObjectsPair<Integer, String> b = ObjectsPair.of(1, "Goodbye");
    final ObjectsPair<Integer, String> c = ObjectsPair.of(2, "Hello");
    final ObjectsPair<Integer, String> d = ObjectsPair.of(2, "Goodbye");
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);

    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);

    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);

    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);

    assertNotEquals("RUBBISH", a);
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsNull() {
    final ObjectsPair<Integer, String> a = ObjectsPair.of(Integer.valueOf(1), "Hello");
    final ObjectsPair<Integer, String> b = ObjectsPair.of(null, "Hello");
    final ObjectsPair<Integer, String> c = ObjectsPair.of(Integer.valueOf(1), null);
    final ObjectsPair<Integer, String> d = ObjectsPair.of(null, null);
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);

    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);

    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);

    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final ObjectsPair<Integer, String> a = ObjectsPair.of(Integer.valueOf(1), "Hello");
    final ObjectsPair<Integer, String> b = ObjectsPair.of(null, "Hello");
    final ObjectsPair<Integer, String> c = ObjectsPair.of(Integer.valueOf(1), null);
    final ObjectsPair<Integer, String> d = ObjectsPair.of(null, null);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(b.hashCode(), b.hashCode());
    assertEquals(c.hashCode(), c.hashCode());
    assertEquals(d.hashCode(), d.hashCode());
    // can't test for different hash codes as they might not be different
  }

  /**
   * Tests the toList() method.
   */
  @Test
  public void toList() {
    final ObjectsPair<String, String> a = ObjectsPair.of("Jay-Z", "Black Album");
    final List<String> asList = a.toList();
    assertNotNull(asList);
    assertEquals(2, asList.size());
    assertEquals("Jay-Z", asList.get(0));
    assertEquals("Black Album", asList.get(1));
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    final ObjectsPair<String, String> test = ObjectsPair.of("A", "B");
    assertEquals("[A, B]", test.toString());
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ObjectsPair<String, String> pair = ObjectsPair.of("Jay-Z", "Black Album");
    assertNotNull(pair.metaBean());
    assertNotNull(pair.metaBean().first());
    assertNotNull(pair.metaBean().second());
    assertEquals("Jay-Z", pair.metaBean().first().get(pair));
    assertEquals("Black Album", pair.metaBean().second().get(pair));
    assertEquals("Jay-Z", pair.property("first").get());
    assertEquals("Black Album", pair.property("second").get());
  }

}
