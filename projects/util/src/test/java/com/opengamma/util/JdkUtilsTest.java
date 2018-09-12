/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link JdkUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class JdkUtilsTest {

  /**
   * Tests the constructor via reflection.
   *
   * @throws Exception  if there is a problem constructing the object
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testConstructor() throws Exception {
    final Constructor<?>[] cons = JdkUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    final Constructor<JdkUtils> con = (Constructor<JdkUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that trailing zeros are stripped.
   */
  @Test
  public void testStrip() {
    assertEquals(BigDecimal.valueOf(0, 0), JdkUtils.stripTrailingZeros(BigDecimal.valueOf(0, 2)));
    assertEquals(BigDecimal.valueOf(1, 0), JdkUtils.stripTrailingZeros(BigDecimal.valueOf(1, 0)));
  }

}
