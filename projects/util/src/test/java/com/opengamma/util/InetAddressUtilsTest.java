/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link InetAddressUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class InetAddressUtilsTest {

  /**
   * Tests the constructor via reflection.
   *
   * @throws Exception  if there is a problem constructing the object
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testConstructor() throws Exception {
    final Constructor<?>[] cons = InetAddressUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    final Constructor<InetAddressUtils> con = (Constructor<InetAddressUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the local host name is not null.
   */
  @Test
  public void testLocalHostName() {
    assertNotNull(InetAddressUtils.getLocalHostName());
  }

}
