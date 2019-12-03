/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link DataNotFoundException}.
 */
@Test(groups = TestGroup.UNIT)
public class DataNotFoundExceptionTest {

  /**
   * Tests the string constructor.
   */
  @Test
  public void testConstructorString() {
    final DataNotFoundException test = new DataNotFoundException("Msg");
    assertEquals("Msg", test.getMessage());
    assertEquals(null, test.getCause());
  }

  /**
   * Tests the throwable constructor.
   */
  @Test
  public void testConstructorStringThrowable() {
    final Throwable th = new NullPointerException();
    final DataNotFoundException test = new DataNotFoundException("Msg", th);
    assertEquals(true, test.getMessage().contains("Msg"));
    assertSame(th, test.getCause());
  }

}
