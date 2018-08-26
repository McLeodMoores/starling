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
public class RecoveryRateModelConstantTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final static double RECOVERY_RATE = 0.563;

  RecoveryRateModelConstant RECOVERY_RATE_MODEL = new RecoveryRateModelConstant(RECOVERY_RATE);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test(enabled = false)
  public void ConstantRecoveryRateModelTest() {

    System.out.println("Running constant recovery rate model test ...");

    System.out.println(RECOVERY_RATE_MODEL.getRecoveryRate());

  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
