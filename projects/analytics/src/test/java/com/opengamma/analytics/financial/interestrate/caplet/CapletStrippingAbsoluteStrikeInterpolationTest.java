/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.caplet;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CapletStrippingAbsoluteStrikeInterpolationTest extends CapletStrippingAbsoluteStrikeTest {

  @Override
  protected CapletStrippingAbsoluteStrike getStripper(final List<CapFloor> caps) {
    return new CapletStrippingAbsoluteStrikeInterpolation(caps, getYieldCurves());
  }

  @Test
  public void test() {
    final boolean print = false;
    testVolStripping(print);
  }

  @Test
  public void timingTest() {
    timingTest(1, 0);
  }

}
