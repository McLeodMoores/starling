/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SealableUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class SealableUtilsTest {

  /**
   * Tests the constructor via reflection.
   *
   * @throws Exception  if there is a problem constructing the object
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testConstructor() throws Exception {
    final Constructor<?>[] cons = SealableUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    final Constructor<SealableUtils> con = (Constructor<SealableUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  /**
   * Test implementation.
   */
  private static class MockSealed implements Sealable {
    @Override
    public void seal() {
    }
    @Override
    public boolean isSealed() {
      return true;
    }
  }
  /**
   * Test implementation.
   */
  private static class MockUnsealed implements Sealable {
    @Override
    public void seal() {
    }
    @Override
    public boolean isSealed() {
      return false;
    }
  }

  /**
   * Tests that a class is sealed.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testSealed() {
    SealableUtils.checkSealed(new MockSealed());
  }

  /**
   * Tests that a class is unsealed.
   */
  public void testUnsealed() {
    SealableUtils.checkSealed(new MockUnsealed());
  }

}
