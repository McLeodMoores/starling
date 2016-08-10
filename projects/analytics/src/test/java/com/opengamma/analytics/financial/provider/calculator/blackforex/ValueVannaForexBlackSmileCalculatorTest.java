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

import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ValueVannaForexBlackSmileCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class ValueVannaForexBlackSmileCalculatorTest {
  /** The calculator being tested. */
  private static final ValueVannaForexBlackSmileCalculator CALCULATOR = ValueVannaForexBlackSmileCalculator.getInstance();

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
    final CurrencyAmount longCall = LONG_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final CurrencyAmount shortCall = SHORT_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    final CurrencyAmount longPut = LONG_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final CurrencyAmount shortPut = SHORT_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
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


  /**
   * Tests that sensitivity values are equal after scaling.
   * @param sensitivity1  the first sensitivities
   * @param sensitivity2  the second sensitivities
   * @param scale  the scale
   */
  private static void assertEqualsAfterScaling(final CurrencyAmount sensitivity1, final CurrencyAmount sensitivity2, final double scale) {
    assertEquals(sensitivity1, sensitivity2.multipliedBy(scale));
  }
}
