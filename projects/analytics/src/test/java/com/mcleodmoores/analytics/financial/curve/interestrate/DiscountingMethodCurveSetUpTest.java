/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class DiscountingMethodCurveSetUpTest {

  /**
   * Tests that nulls cannot be passed into builder methods.
   */
  @Test
  public void testNullBuilderMethodInputs() {
    TestUtils.testNullBuilderMethodInputs(DiscountingMethodCurveSetUp.class, CurveSetUpInterface.class);
  }

}
