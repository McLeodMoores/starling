/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EquitySecurityFudgeEncodingTest extends FinancialTestBase {

  private static final EquitySecurity REF= new EquitySecurity("A", "B", "C", Currency.USD);

  @Test
  public void testCycle() {
    assertEquals(REF, cycleObject(EquitySecurity.class, REF));
  }

}
