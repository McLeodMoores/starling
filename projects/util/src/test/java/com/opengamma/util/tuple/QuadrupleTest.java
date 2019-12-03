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
 * Test {@link Quadruple}.
 */
@Test(groups = TestGroup.UNIT)
public class QuadrupleTest {
  private static final boolean[][] P = new boolean[][] {
      new boolean[] { false, false, false, false},
      new boolean[] { false, false, false, true},
      new boolean[] { false, false, true, false},
      new boolean[] { false, false, true, true},
      new boolean[] { false, true, false, false},
      new boolean[] { false, true, false, true},
      new boolean[] { false, true, true, false},
      new boolean[] { false, true, true, true},
      new boolean[] { true, false, false, false},
      new boolean[] { true, false, false, true},
      new boolean[] { true, false, true, false},
      new boolean[] { true, false, true, true},
      new boolean[] { true, true, false, false},
      new boolean[] { true, true, false, true},
      new boolean[] { true, true, true, false},
      new boolean[] { true, true, true, true},
  };

  /**
   * Tests Quadruple<Object, Object, Object, Float>.
   */
  @Test
  public void testQuadrupleObjectObjectObject() {
    final Quadruple<String, String, String, Float> test = Quadruple.of("A", "B", "C", 2F);
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getThird(), "C");
    assertEquals(test.getFourth(), 2F, 1e-15);
  }

  /**
   * Tests with null entries.
   */
  @Test
  public void testNullEntries() {
    final Object[] inputs = new Object[] { "A", "B", "C", 2L };
    for (final boolean[] element : P) {
      final Quadruple<Object, Object, Object, Object> test = Quadruple.of(
          element[0] ? inputs[0] : null,
          element[1] ? inputs[1] : null,
          element[2] ? inputs[2] : null,
          element[3] ? inputs[3] : null);
      assertEquals(test.getFirst(), element[0] ? inputs[0] : null);
      assertEquals(test.getSecond(), element[1] ? inputs[1] : null);
      assertEquals(test.getThird(), element[2] ? inputs[2] : null);
      assertEquals(test.getFourth(), element[3] ? inputs[3] : null);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests compareTo().
   */
  @Test
  public void compareTo() {
    final Quadruple<String, String, String, String> abc = Quadruple.of("A", "B", "C", "E");
    final Quadruple<String, String, String, String> abd = Quadruple.of("A", "B", "D", "E");
    final Quadruple<String, String, String, String> acc = Quadruple.of("A", "C", "C", "E");
    final Quadruple<String, String, String, String> bac = Quadruple.of("B", "A", "C", "E");

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
   * Tests the equals() method.
   */
  @Test
  public void testEquals() {
    final Quadruple<Integer, String, String, String> a = Quadruple.of(1, "Hello", "C", "E");
    final Quadruple<Integer, String, String, String> b = Quadruple.of(1, "Goodbye", "C", "E");
    final Quadruple<Integer, String, String, String> c = Quadruple.of(2, "Hello", "C", "E");
    final Quadruple<Integer, String, String, String> d = Quadruple.of(2, "Goodbye", "C", "E");
    final Quadruple<Integer, String, String, String> e = Quadruple.of(2, "Goodbye", "D", "E");

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
    final Quadruple<Integer, String, String, Long> a = Quadruple.of(1, "Hello", "C", 2L);
    final Quadruple<Integer, String, String, Long> b = Quadruple.of(null, "Hello", "C", 2L);
    final Quadruple<Integer, String, String, Long> c = Quadruple.of(1, null, "C", 2L);
    final Quadruple<Integer, String, String, Long> d = Quadruple.of(null, null, "C", 2L);
    final Quadruple<Integer, String, String, Long> e = Quadruple.of(null, null, null, 2L);
    final Quadruple<Integer, String, String, Long> f = Quadruple.of(null, null, null, null);

    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);
    assertEquals(a.equals(e), false);
    assertEquals(a.equals(f), false);

    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);
    assertEquals(b.equals(e), false);
    assertEquals(b.equals(f), false);

    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);
    assertEquals(c.equals(e), false);
    assertEquals(c.equals(f), false);

    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);
    assertEquals(d.equals(e), false);
    assertEquals(d.equals(f), false);

    assertEquals(e.equals(a), false);
    assertEquals(e.equals(b), false);
    assertEquals(e.equals(c), false);
    assertEquals(e.equals(d), false);
    assertEquals(e.equals(e), true);
    assertEquals(e.equals(f), false);

    assertEquals(f.equals(a), false);
    assertEquals(f.equals(b), false);
    assertEquals(f.equals(c), false);
    assertEquals(f.equals(d), false);
    assertEquals(f.equals(e), false);
    assertEquals(f.equals(f), true);
  }

  /**
   * Tests the hashCode().
   */
  @Test
  public void testHashCode() {
    final Quadruple<Integer, String, String, Long> a = Quadruple.of(1, "Hello", "C", 2L);
    final Quadruple<Integer, String, String, Long> b = Quadruple.of(null, "Hello", "C", 2L);
    final Quadruple<Integer, String, String, Long> c = Quadruple.of(1, null, "C", 2L);
    final Quadruple<Integer, String, String, Long> d = Quadruple.of(null, null, "C", 2L);
    final Quadruple<Integer, String, String, Long> e = Quadruple.of(null, null, null, 2L);
    final Quadruple<Integer, String, String, Long> f = Quadruple.of(null, null, null, null);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(b.hashCode(), b.hashCode());
    assertEquals(c.hashCode(), c.hashCode());
    assertEquals(d.hashCode(), d.hashCode());
    assertEquals(e.hashCode(), e.hashCode());
    assertEquals(f.hashCode(), f.hashCode());
    // can't test for different hash codes as they might not be different
  }

  /**
   * Tests the toList() method.
   */
  @Test
  public void toList() {
    final Quadruple<String, String, String, String> a = Quadruple.of("Jay-Z", "Black Album", "99 Problems", "Ain't One");
    final List<String> asList = a.toList();
    assertNotNull(asList);
    assertEquals(4, asList.size());
    assertEquals("Jay-Z", asList.get(0));
    assertEquals("Black Album", asList.get(1));
    assertEquals("99 Problems", asList.get(2));
    assertEquals("Ain't One", asList.get(3));
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToString() {
    final Quadruple<String, String, String, String> test = Quadruple.of("A", "B", "C", "D");
    assertEquals("[A, B, C, D]", test.toString());
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final Quadruple<String, String, String, String> quadruple = Quadruple.of("Jay-Z", "Black Album", "99 Problems", "Ain't One");
    assertNotNull(quadruple.metaBean());
    assertNotNull(quadruple.metaBean().first());
    assertNotNull(quadruple.metaBean().second());
    assertNotNull(quadruple.metaBean().third());
    assertEquals("Jay-Z", quadruple.metaBean().first().get(quadruple));
    assertEquals("Black Album", quadruple.metaBean().second().get(quadruple));
    assertEquals("99 Problems", quadruple.metaBean().third().get(quadruple));
    assertEquals("Jay-Z", quadruple.property("first").get());
    assertEquals("Black Album", quadruple.property("second").get());
    assertEquals("99 Problems", quadruple.property("third").get());
    assertEquals("Ain't One", quadruple.property("fourth").get());
  }

}
