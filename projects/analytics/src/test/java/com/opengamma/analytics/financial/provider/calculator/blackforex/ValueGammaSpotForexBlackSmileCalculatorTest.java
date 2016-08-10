/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DIGITAL_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DOWN_KNOCK_IN_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_ND_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_VANILLA_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_VANILLA_PUT_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.MARKET_DATA_WITH_SMILE;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_VANILLA_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_VANILLA_PUT_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.VALUATION_DATE;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ValueGammaSpotForexBlackSmileCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class ValueGammaSpotForexBlackSmileCalculatorTest {
  /** The calculator being tested. */
  private static final ValueGammaSpotForexBlackSmileCalculator CALCULATOR = ValueGammaSpotForexBlackSmileCalculator.getInstance();

  /**
   * Tests that market data is required.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoData() {
    LONG_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR);
  }

  /**
   * Tests that long and short vanilla options give the same results.
   */
  @Test
  public void testLongShortVanillaOption() {
    final double longCall = LONG_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE).getAmount();
    final double shortCall = SHORT_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE).getAmount();
    assertEquals(longCall, shortCall, 2e-15);
    final double longPut = LONG_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE).getAmount();
    final double shortPut = SHORT_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE).getAmount();
    assertEquals(longPut, shortPut, 2e-15);
    assertEquals(longCall, longPut, 2e-15);
    assertEquals(longCall, shortPut, 2e-15);
  }

  /**
   * Non-deliverable options are not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLongShortNonDeliverableOption() {
    LONG_ND_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
  }

  /**
   * Digital options are not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLongShortDigitalOption() {
    LONG_DIGITAL_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
  }

  /**
   * Single barrier options are not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLongShortSingleBarrierOption() {
    LONG_DOWN_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
  }

}
