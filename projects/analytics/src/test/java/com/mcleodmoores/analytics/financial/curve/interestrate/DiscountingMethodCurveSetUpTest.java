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
    TestUtils.testNullBuilderMethodInputs(DiscountingMethodCurveSetUp.class, CurveSetUpInterface.class, "withKnownBundle");
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

  /**
   * Tests that the builder cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBuilder() {
    new DiscountingMethodCurveSetUp(null);
  }

  /**
   * Tests that the building() method cannot be used twice.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuilding() {
    new DiscountingMethodCurveSetUp().building("A").building("B");
  }

  /**
   * Tests that the buildingFirst() method cannot be used twice.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildingFirst() {
    new DiscountingMethodCurveSetUp().building("A").buildingFirst("B");
  }

  /**
   * Tests that the building() method cannot be used twice.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testThenBuilding() {
    new DiscountingMethodCurveSetUp().thenBuilding("A");
  }

  /**
   * Test duplicated curve type.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testDuplicatedCurveType() {
    new DiscountingMethodCurveSetUp().using("A").using("A");
  }
}
