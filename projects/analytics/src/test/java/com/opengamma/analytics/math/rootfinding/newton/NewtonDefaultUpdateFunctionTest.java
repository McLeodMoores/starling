/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NewtonDefaultUpdateFunctionTest {
  private static final NewtonDefaultUpdateFunction F = new NewtonDefaultUpdateFunction();

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    F.getUpdatedMatrix(null, null, null, null, null);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    F.getUpdatedMatrix(x -> null, null, null, null, null);
  }

}
