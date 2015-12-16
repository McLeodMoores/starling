/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.financial.convention.FinancialConventionVisitor;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link QuandlFinancialConventionVisitorAdapter}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlFinancialConventionAdapterVisitorTest {
  /** The visitor */
  private static final FinancialConventionVisitor<?> ADAPTER = new QuandlFinancialConventionVisitorAdapter<>();

  /**
   * Tests the exception thrown for Quandl conventions.
   */
  @Test
  public void testQuandlConvention() {
    try {
      ConventionTestInstances.QUANDL_FED_FUNDS_FUTURE.accept(ADAPTER);
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      ConventionTestInstances.QUANDL_USD_3M_3M_STIR_FUTURE.accept(ADAPTER);
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
  }

  /**
   * Tests the exception thrown for a non-Quandl convention.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNonQuandlConvention() {
    ConventionTestInstances.DEPOSIT.accept(ADAPTER);
  }
}
