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
    TestUtils.testNullBuilderMethodInputs(DiscountingMethodCurveSetUp.class, CurveSetUpInterface.class, "removeNodes", "withKnownBundle");
  }

  /**
   * Tests that empty collections / arrays cannot be passed into builder methods.
   */
  @Test
  public void testEmptyBuilderMethodInputs() {
    TestUtils.testEmptyBuilderMethodInputs(DiscountingMethodCurveSetUp.class, CurveSetUpInterface.class);
  }

  /**
   * Tests that the tolerances and max steps must be greater than zero.
   */
  @Test
  public void testNegativeBuilderMethodInputs() {
    TestUtils.testBuilderMethodsLowerRange(DiscountingMethodCurveSetUp.class, CurveSetUpInterface.class, 0, false);
  }
}
