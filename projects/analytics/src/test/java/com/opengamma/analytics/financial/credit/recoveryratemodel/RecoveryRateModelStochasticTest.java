/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.recoveryratemodel;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RecoveryRateModelStochasticTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final double A = 0.75;
  private static final double B = 0.75;

  private static final double X = 0.7;

  private static final RecoveryRateModelStochastic RECOVERY_RATE_MODEL = new RecoveryRateModelStochastic(A, B, X);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(enabled = false)
  public void ConstantRecoveryRateModelTest() {

    System.out.println("Running constant recovery rate model test ...");

    RecoveryRateModelStochastic recRateModel = RECOVERY_RATE_MODEL;

    for (double x = 0.01; x <= 1.0; x += 0.01) {

      recRateModel = recRateModel.sampleRecoveryRate(x);

      System.out.println("a = " + A + "\t" + "b = " + B + "\t" + "x = " + "\t" + x + "\t" + "delta = " + "\t" + recRateModel.getRecoveryRate());
    }
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

}
