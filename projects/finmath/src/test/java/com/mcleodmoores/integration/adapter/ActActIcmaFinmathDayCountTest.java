/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ActActIcmaFinmathDayCount}.
 */
@Test(groups = TestGroup.UNIT)
public class ActActIcmaFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test(expectedExceptions = NotImplementedException.class)
  public void test() {
    new ActActIcmaFinmathDayCount();
  }
}
