/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.tuple;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link Pairs}.
 */
@Test(groups = TestGroup.UNIT)
public class PairsTest {

  /**
   * Tests the ofNulls() method.
   */
  @Test
  public void testOfNulls() {
    final Pair<Object, Object> p = Pairs.ofNulls();
    assertNull(p.getFirst());
    assertNull(p.getSecond());
    assertEquals(Pairs.ofNulls(), p);
    assertNotSame(Pairs.ofNulls(), p);
  }

  /**
   * Tests the ofOptimized() method.
   */
  @Test
  public void testOfOptimized() {
    assertTrue(Pairs.ofOptimized(Double.valueOf(1.2), Double.valueOf(2.3)) instanceof DoublesPair);
    assertTrue(Pairs.ofOptimized(Integer.valueOf(1), Double.valueOf(2.3)) instanceof IntDoublePair);
    assertTrue(Pairs.ofOptimized(Long.valueOf(1L), Double.valueOf(2.3)) instanceof LongDoublePair);
    assertTrue(Pairs.ofOptimized("1", Double.valueOf(2.3)) instanceof ObjectsPair);
    assertTrue(Pairs.ofOptimized(Integer.valueOf(1), "A") instanceof IntObjectPair);
    assertTrue(Pairs.ofOptimized(Long.valueOf(1L), "A") instanceof LongObjectPair);
    assertTrue(Pairs.ofOptimized("1", "A") instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOf() {
    assertTrue(Pairs.of((Object) Double.valueOf(1.2), (Object) Double.valueOf(2.3)) instanceof ObjectsPair);
    assertTrue(Pairs.of((Object) Integer.valueOf(1), (Object) Double.valueOf(2.3)) instanceof ObjectsPair);
    assertTrue(Pairs.of((Object) Long.valueOf(1L), (Object) Double.valueOf(2.3)) instanceof ObjectsPair);
    assertTrue(Pairs.of((Object) "1", (Object) Double.valueOf(2.3)) instanceof ObjectsPair);
    assertTrue(Pairs.of((Object) Integer.valueOf(1), (Object) "A") instanceof ObjectsPair);
    assertTrue(Pairs.of((Object) Long.valueOf(1L), (Object) "A") instanceof ObjectsPair);
    assertTrue(Pairs.of((Object) "1", (Object) "A") instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfDoubleDouble() {
    assertTrue(Pairs.of(Double.valueOf(1.2), Double.valueOf(2.3)) instanceof DoublesPair);
    assertTrue(Pairs.of((Double) null, Double.valueOf(2.3)) instanceof ObjectsPair);
    assertTrue(Pairs.of(Double.valueOf(1.2), (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of((Double) null, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(Double.valueOf(1.2), 2.3d) instanceof DoublesPair);
    assertTrue(Pairs.of((Double) null, 2.3d) instanceof ObjectsPair);
    assertTrue(Pairs.of(1.2d, Double.valueOf(2.3)) instanceof DoublesPair);
    assertTrue(Pairs.of(1.2d, null) instanceof ObjectsPair);
    assertTrue(Pairs.of(1.2d, 2.3d) instanceof DoublesPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfDoubleBoolean() {
    assertTrue(Pairs.of(Double.valueOf(1.2), true) instanceof ObjectsPair);
    assertTrue(Pairs.of((Double) null, true) instanceof ObjectsPair);
    assertTrue(Pairs.of(1.2d, true) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfIntegerObject() {
    assertTrue(Pairs.of(Integer.valueOf(1), "E") instanceof IntObjectPair);
    assertTrue(Pairs.of((Integer) null, "Y") instanceof ObjectsPair);
    assertTrue(Pairs.of(1, "E") instanceof IntObjectPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfIntegerDouble() {
    assertTrue(Pairs.of(Integer.valueOf(1), Double.valueOf(1.2)) instanceof IntDoublePair);
    assertTrue(Pairs.of((Integer) null, Double.valueOf(1.2)) instanceof ObjectsPair);
    assertTrue(Pairs.of(Integer.valueOf(1), (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of((Integer) null, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(Integer.valueOf(1), 1.2d) instanceof IntDoublePair);
    assertTrue(Pairs.of((Integer) null, 1.2d) instanceof ObjectsPair);
    assertTrue(Pairs.of(Integer.valueOf(1), (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of((Integer) null, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(1, Double.valueOf(1.2)) instanceof IntDoublePair);
    assertTrue(Pairs.of(1, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(1, 1.2d) instanceof IntDoublePair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfIntegerLong() {
    assertTrue(Pairs.of(Integer.valueOf(1), 2L) instanceof ObjectsPair);
    assertTrue(Pairs.of((Integer) null, 2L) instanceof ObjectsPair);
    assertTrue(Pairs.of(1, 2L) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfIntegerInteger() {
    assertTrue(Pairs.of(Integer.valueOf(1), Integer.valueOf(2)) instanceof IntObjectPair);
    assertTrue(Pairs.of((Integer) null, Integer.valueOf(2)) instanceof ObjectsPair);
    assertTrue(Pairs.of(Integer.valueOf(1), (Integer) null) instanceof IntObjectPair);
    assertTrue(Pairs.of((Integer) null, (Integer) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(Integer.valueOf(1), 2) instanceof ObjectsPair);
    assertTrue(Pairs.of((Integer) null, 2) instanceof ObjectsPair);
    assertTrue(Pairs.of(Integer.valueOf(1), (Integer) null) instanceof IntObjectPair);
    assertTrue(Pairs.of(1, 2) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfIntegerBoolean() {
    assertTrue(Pairs.of(Integer.valueOf(1), true) instanceof ObjectsPair);
    assertTrue(Pairs.of((Integer) null, true) instanceof ObjectsPair);
    assertTrue(Pairs.of(1, true) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfLongObject() {
    assertTrue(Pairs.of(Long.valueOf(1), "E") instanceof LongObjectPair);
    assertTrue(Pairs.of((Long) null, "Y") instanceof ObjectsPair);
    assertTrue(Pairs.of(1L, "E") instanceof LongObjectPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfLongDouble() {
    assertTrue(Pairs.of(Long.valueOf(1), Double.valueOf(1.2)) instanceof LongDoublePair);
    assertTrue(Pairs.of((Long) null, Double.valueOf(1.2)) instanceof ObjectsPair);
    assertTrue(Pairs.of(Long.valueOf(1), (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of((Long) null, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(Long.valueOf(1), 1.2d) instanceof LongDoublePair);
    assertTrue(Pairs.of((Long) null, 1.2d) instanceof ObjectsPair);
    assertTrue(Pairs.of(Long.valueOf(1), (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of((Long) null, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(1L, Double.valueOf(1.2)) instanceof LongDoublePair);
    assertTrue(Pairs.of(1L, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(1L, 1.2d) instanceof LongDoublePair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfLongLong() {
    assertTrue(Pairs.of(Long.valueOf(1), Long.valueOf(2)) instanceof LongObjectPair);
    assertTrue(Pairs.of((Long) null, Long.valueOf(2)) instanceof ObjectsPair);
    assertTrue(Pairs.of(Long.valueOf(1), (Long) null) instanceof LongObjectPair);
    assertTrue(Pairs.of((Long) null, (Long) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(Long.valueOf(1), 2L) instanceof ObjectsPair);
    assertTrue(Pairs.of((Long) null, 2L) instanceof ObjectsPair);
    assertTrue(Pairs.of(Long.valueOf(1), (Long) null) instanceof LongObjectPair);
    assertTrue(Pairs.of(1L, 2L) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfLongInteger() {
    assertTrue(Pairs.of(Long.valueOf(1), 2) instanceof ObjectsPair);
    assertTrue(Pairs.of((Long) null, 2) instanceof ObjectsPair);
    assertTrue(Pairs.of(1L, 2) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfLongBoolean() {
    assertTrue(Pairs.of(Long.valueOf(1), true) instanceof ObjectsPair);
    assertTrue(Pairs.of((Long) null, true) instanceof ObjectsPair);
    assertTrue(Pairs.of(1L, true) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfBooleanObject() {
    assertTrue(Pairs.of(Boolean.FALSE, "E") instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, "Y") instanceof ObjectsPair);
    assertTrue(Pairs.of(false, "E") instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfBooleanDouble() {
    assertTrue(Pairs.of(Boolean.FALSE, Double.valueOf(1.2)) instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, Double.valueOf(1.2)) instanceof ObjectsPair);
    assertTrue(Pairs.of(Boolean.FALSE, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(Boolean.FALSE, 1.2d) instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, 1.2d) instanceof ObjectsPair);
    assertTrue(Pairs.of(Boolean.FALSE, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(true, Double.valueOf(1.2)) instanceof ObjectsPair);
    assertTrue(Pairs.of(false, (Double) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(true, 1.2d) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfBooleanBoolean() {
    assertTrue(Pairs.of(Boolean.FALSE, Boolean.TRUE) instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, Boolean.TRUE) instanceof ObjectsPair);
    assertTrue(Pairs.of(Boolean.TRUE, (Boolean) null) instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, (Boolean) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(Boolean.TRUE, true) instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, true) instanceof ObjectsPair);
    assertTrue(Pairs.of(Boolean.FALSE, (Boolean) null) instanceof ObjectsPair);
    assertTrue(Pairs.of(true, false) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfBooleanInteger() {
    assertTrue(Pairs.of(Boolean.FALSE, 2) instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, 2) instanceof ObjectsPair);
    assertTrue(Pairs.of(true, 2) instanceof ObjectsPair);
  }

  /**
   * Tests the of() method.
   */
  @Test
  public void testOfBooleanLong() {
    assertTrue(Pairs.of(Boolean.TRUE, Long.valueOf(1)) instanceof ObjectsPair);
    assertTrue(Pairs.of((Boolean) null, 2L) instanceof ObjectsPair);
    assertTrue(Pairs.of(true, 2L) instanceof ObjectsPair);
  }
}
