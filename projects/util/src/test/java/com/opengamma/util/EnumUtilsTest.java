/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.RoundingMode;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link EnumUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class EnumUtilsTest {

  /**
   * Tests the constructor via reflection.
   *
   * @throws Exception  if there is a problem constructing the object
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testConstructor() throws Exception {
    final Constructor<?>[] cons = EnumUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    final Constructor<EnumUtils> con = (Constructor<EnumUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that null is returned if there is no enum value.
   */
  @Test
  public void testSafeValueOf() {
    assertEquals(RoundingMode.FLOOR, EnumUtils.safeValueOf(RoundingMode.class, "FLOOR"));
    assertEquals(null, EnumUtils.safeValueOf(RoundingMode.class, null));
    assertEquals(null, EnumUtils.<RoundingMode>safeValueOf(null, "FLOOR"));
    assertEquals(null, EnumUtils.safeValueOf(RoundingMode.class, "RUBBISH"));
  }

}
