/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CompareUtilsTest {

  @SuppressWarnings("unchecked")
  public void test_constructor() throws Exception {
    Constructor<?>[] cons = CompareUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    Constructor<CompareUtils> con = (Constructor<CompareUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  public void test_max() {
    assertEquals(null, CompareUtils.<String>max(null, null));
    assertEquals("A", CompareUtils.max(null, "A"));
    assertEquals("A", CompareUtils.max("A", null));
    Integer a = new Integer(1); // need to use new, not autoboxing
    Integer b = new Integer(1); // need to use new, not autoboxing
    assertSame(a, CompareUtils.max(a, b));  // as we test for same here
    assertEquals("B", CompareUtils.max("A", "B"));
    assertEquals("B", CompareUtils.max("B", "A"));
  }

  public void test_min() {
    assertEquals(null, CompareUtils.<String>min(null, null));
    assertEquals("A", CompareUtils.min(null, "A"));
    assertEquals("A", CompareUtils.min("A", null));
    Integer a = new Integer(1); // need to use new, not autoboxing
    Integer b = new Integer(1); // need to use new, not autoboxing
    assertSame(a, CompareUtils.min(a, b));  // as we test for same here
    assertEquals("A", CompareUtils.min("A", "B"));
    assertEquals("A", CompareUtils.min("B", "A"));
  }

  public void test_compareWithNullLow() {
    assertTrue(CompareUtils.compareWithNullLow(null, null) == 0);
    assertTrue(CompareUtils.compareWithNullLow(null, "Test") < 0);
    assertTrue(CompareUtils.compareWithNullLow("Test", null) > 0);
    assertTrue(CompareUtils.compareWithNullLow("Test", "Test") == 0);
    assertTrue(CompareUtils.compareWithNullLow("AAAA", "BBBB") == "AAAA".compareTo("BBBB"));
  }

  public void test_compareWithNullHigh() {
    assertTrue(CompareUtils.compareWithNullHigh(null, null) == 0);
    assertTrue(CompareUtils.compareWithNullHigh(null, "Test") > 0);
    assertTrue(CompareUtils.compareWithNullHigh("Test", null) < 0);
    assertTrue(CompareUtils.compareWithNullHigh("Test", "Test") == 0);
    assertTrue(CompareUtils.compareWithNullHigh("AAAA", "BBBB") == "AAAA".compareTo("BBBB"));
  }

  //-------------------------------------------------------------------------
  public void test_closeEquals() {
    assertEquals(CompareUtils.closeEquals(0.2d, 0.2d), true);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.3d), false);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.1d), false);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.2000000000000001d), true);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.1999999999999999d), true);
    assertEquals(CompareUtils.closeEquals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), true);
    assertEquals(CompareUtils.closeEquals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), true);
    assertEquals(CompareUtils.closeEquals(Double.NaN, Double.NaN), false);
    assertEquals(CompareUtils.closeEquals(Double.POSITIVE_INFINITY, Double.NaN), false);
    assertEquals(CompareUtils.closeEquals(Double.NaN, Double.POSITIVE_INFINITY), false);
    assertEquals(CompareUtils.closeEquals(Double.NEGATIVE_INFINITY, Double.NaN), false);
    assertEquals(CompareUtils.closeEquals(Double.NaN, Double.NEGATIVE_INFINITY), false);
  }

  public void test_closeEquals_tolerance() {
    assertEquals(CompareUtils.closeEquals(0.2d, 0.2d, 0.0001d), true);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.3d, 0.0001d), false);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.1d, 0.0001d), false);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.2002d, 0.0001d), false);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.2001d, 0.0001d), true);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.20009d, 0.0001d), true);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.2000001d, 0.0001d), true);
    assertEquals(CompareUtils.closeEquals(0.2d, 0.1999999d, 0.0001d), true);
    assertEquals(CompareUtils.closeEquals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0001d), true);
    assertEquals(CompareUtils.closeEquals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.0001d), true);
    assertEquals(CompareUtils.closeEquals(Double.NaN, Double.NaN, 0.0001d), false);
    assertEquals(CompareUtils.closeEquals(Double.POSITIVE_INFINITY, Double.NaN, 0.0001d), false);
    assertEquals(CompareUtils.closeEquals(Double.NaN, Double.POSITIVE_INFINITY, 0.0001d), false);
    assertEquals(CompareUtils.closeEquals(Double.NEGATIVE_INFINITY, Double.NaN, 0.0001d), false);
    assertEquals(CompareUtils.closeEquals(Double.NaN, Double.NEGATIVE_INFINITY, 0.0001d), false);
  }

  public void test_compareWithTolerance() {
    assertEquals(CompareUtils.compareWithTolerance(0.2d, 0.2d, 0.0001d), 0);
    assertEquals(CompareUtils.compareWithTolerance(0.2d, 0.3d, 0.0001d), -1);
    assertEquals(CompareUtils.compareWithTolerance(0.2d, 0.1d, 0.0001d), 1);
    assertEquals(CompareUtils.compareWithTolerance(0.2d, 0.2002d, 0.0001d), -1);
    assertEquals(CompareUtils.compareWithTolerance(0.2d, 0.2001d, 0.0001d), 0);
    assertEquals(CompareUtils.compareWithTolerance(0.2d, 0.20009d, 0.0001d), 0);
    assertEquals(CompareUtils.compareWithTolerance(0.2d, 0.2000001d, 0.0001d), 0);
    assertEquals(CompareUtils.compareWithTolerance(0.2d, 0.1999999d, 0.0001d), 0);
    assertEquals(CompareUtils.compareWithTolerance(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0001d), 0);
    assertEquals(CompareUtils.compareWithTolerance(Double.POSITIVE_INFINITY, 1.0d, 0.0001d), 1);
    assertEquals(CompareUtils.compareWithTolerance(1.0d, Double.POSITIVE_INFINITY, 0.0001d), -1);
    assertEquals(CompareUtils.compareWithTolerance(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.0001d), 0);
    assertEquals(CompareUtils.compareWithTolerance(Double.NEGATIVE_INFINITY, 1.0d, 0.0001d), -1);
    assertEquals(CompareUtils.compareWithTolerance(1.0d, Double.NEGATIVE_INFINITY, 0.0001d), 1);
    assertEquals(CompareUtils.compareWithTolerance(Double.NaN, Double.NaN, 0.0001d), 1);  // weird case
  }

}
