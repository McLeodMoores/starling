/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TurningPointIIDHypothesisTest extends IIDHypothesisTestCase {
  private static final IIDHypothesis TURNING_POINT = new TurningPointIIDHypothesis(0.05);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new TurningPointIIDHypothesis(-0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new TurningPointIIDHypothesis(1.5);
  }

  @Test
  public void test() {
    super.assertNullTS(TURNING_POINT);
    super.assertEmptyTS(TURNING_POINT);
    assertTrue(TURNING_POINT.apply(RANDOM));
    assertTrue(TURNING_POINT.apply(SIGNAL));
    assertFalse(TURNING_POINT.apply(INCREASING));
  }
}
