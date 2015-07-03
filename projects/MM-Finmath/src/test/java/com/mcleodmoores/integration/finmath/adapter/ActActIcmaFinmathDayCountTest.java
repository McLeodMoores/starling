/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ActActIcmaFinmathDayCount}.
 */
@Test
public class ActActIcmaFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test(expectedExceptions = NotImplementedException.class)
  public void test() {
    new ActActIcmaFinmathDayCount();
  }
}
