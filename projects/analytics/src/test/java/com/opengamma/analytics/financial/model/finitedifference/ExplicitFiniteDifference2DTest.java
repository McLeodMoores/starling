/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 *
 * @deprecated Deprecated
 */
@Deprecated
@Test(groups = TestGroup.UNIT_SLOW)
public class ExplicitFiniteDifference2DTest {

  private static final HestonPDETestCase HESTON_TESTER = new HestonPDETestCase();
  private static final SpreadOptionPDETestCase SPREAD_OPTION_TESTER = new SpreadOptionPDETestCase();
  private static final ConvectionDiffusionPDESolver2D SOLVER = new ExplicitFiniteDifference2D();

  @Test
  public void testSpreadOption() {

    final int timeSteps = 5000;
    final int xSteps = 100;
    final int ySteps = 100;

    SPREAD_OPTION_TESTER.testAgainstBSPrice(SOLVER, timeSteps, xSteps, ySteps);
  }

  @Test
  public void testHeston() {

    final int timeSteps = 5000;
    final int xSteps = 80;
    final int ySteps = 80;
    final boolean print = false; // make sure this is false before commits

    HESTON_TESTER.testCallPrice(SOLVER, timeSteps, xSteps, ySteps, print);
  }

}
