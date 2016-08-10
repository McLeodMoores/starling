/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.FX_FORWARD;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DIGITAL_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DIGITAL_PUT_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DOWN_KNOCK_IN_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DOWN_KNOCK_IN_PUT;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DOWN_KNOCK_OUT_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DOWN_KNOCK_OUT_PUT;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_ND_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_ND_PUT_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_UP_KNOCK_IN_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_UP_KNOCK_IN_PUT;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_UP_KNOCK_OUT_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_UP_KNOCK_OUT_PUT;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_VANILLA_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_VANILLA_PUT_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.MARKET_DATA_WITH_SMILE;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_DIGITAL_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_DIGITAL_PUT_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_DOWN_KNOCK_IN_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_DOWN_KNOCK_IN_PUT;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_DOWN_KNOCK_OUT_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_DOWN_KNOCK_OUT_PUT;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_ND_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_ND_PUT_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_UP_KNOCK_IN_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_UP_KNOCK_IN_PUT;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_UP_KNOCK_OUT_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_UP_KNOCK_OUT_PUT;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_VANILLA_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.SHORT_VANILLA_PUT_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.VALUATION_DATE;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link ImpliedVolatilityForexBlackSmileCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class ImpliedVolatilityForexBlackSmileCalculatorTest {
  /** The calculator being tested. */
  private static final ImpliedVolatilityForexBlackSmileCalculator CALCULATOR = ImpliedVolatilityForexBlackSmileCalculator.getInstance();

  /**
   * Tests that only FX options are supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNonOption() {
    FX_FORWARD.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
  }

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
    final double longCall = LONG_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final double shortCall = SHORT_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longCall, shortCall, 2e-15);
    final double longPut = LONG_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final double shortPut = SHORT_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longPut, shortPut, 2e-15);
    assertEquals(longCall, longPut, 2e-15);
    assertEquals(longCall, shortPut, 2e-15);
  }

  /**
   * Tests that long and short non-deliverable options give the same results.
   */
  @Test
  public void testLongShortNonDeliverableOption() {
    final double longCall = LONG_ND_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final double shortCall = SHORT_ND_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longCall, shortCall, 2e-15);
    final double longPut = LONG_ND_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final double shortPut = SHORT_ND_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longPut, shortPut, 2e-15);
    assertEquals(longCall, longPut, 2e-15);
    assertEquals(longCall, shortPut, 2e-15);
  }

  /**
   * Tests that long and short digital options give the same results.
   */
  @Test
  public void testLongShortDigitalOption() {
    final double longCall = LONG_DIGITAL_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final double shortCall = SHORT_DIGITAL_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longCall, shortCall, 2e-15);
    final double longPut = LONG_DIGITAL_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final double shortPut = SHORT_DIGITAL_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longPut, shortPut, 2e-15);
    assertEquals(longCall, longPut, 2e-15);
    assertEquals(longCall, shortPut, 2e-15);
  }

  /**
   * Tests that long and short single barrier options give the same results.
   */
  @Test
  public void testLongShortSingleBarrierOption() {
    double longCall = LONG_DOWN_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    double shortCall = SHORT_DOWN_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longCall, shortCall, 2e-15);
    double longPut= LONG_DOWN_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    double shortPut = SHORT_DOWN_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longPut, shortPut, 2e-15);
    assertEquals(longCall, longPut, 2e-15);
    assertEquals(longCall, shortPut, 2e-15);
    longCall = LONG_DOWN_KNOCK_OUT_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortCall = SHORT_DOWN_KNOCK_OUT_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longCall, shortCall, 2e-15);
    longPut= LONG_DOWN_KNOCK_OUT_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortPut = SHORT_DOWN_KNOCK_OUT_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longPut, shortPut, 2e-15);
    assertEquals(longCall, longPut, 2e-15);
    assertEquals(longCall, shortPut, 2e-15);
    longCall = LONG_UP_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortCall = SHORT_UP_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longCall, shortCall, 2e-15);
    longPut= LONG_UP_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortPut = SHORT_UP_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longCall, longPut, 2e-15);
    assertEquals(longCall, shortPut, 2e-15);
    assertEquals(longPut, shortPut, 2e-15);
    longCall = LONG_UP_KNOCK_OUT_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortCall = SHORT_UP_KNOCK_OUT_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longCall, shortCall, 2e-15);
    longPut= LONG_UP_KNOCK_OUT_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortPut = SHORT_UP_KNOCK_OUT_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEquals(longPut, shortPut, 2e-15);
    assertEquals(longCall, longPut, 2e-15);
    assertEquals(longCall, shortPut, 2e-15);
  }

}
