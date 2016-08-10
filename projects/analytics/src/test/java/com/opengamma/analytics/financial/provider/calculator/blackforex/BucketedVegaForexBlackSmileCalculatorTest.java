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

import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link BucketedVegaForexBlackSmileCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class BucketedVegaForexBlackSmileCalculatorTest {
  /** The calculator being tested. */
  private static final BucketedVegaForexBlackSmileCalculator CALCULATOR = BucketedVegaForexBlackSmileCalculator.getInstance();

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
   * Tests that long and short vanilla options give the same results with different signs.
   */
  @Test
  public void testLongShortVanillaOption() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle longCall = LONG_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle shortCall = SHORT_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle longPut = LONG_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle shortPut = SHORT_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
  }

  /**
   * Tests that long and short non-deliverable options give the same results with different signs.
   */
  @Test
  public void testLongShortNonDeliverableOption() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle longCall = LONG_ND_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle shortCall = SHORT_ND_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle longPut = LONG_ND_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle shortPut = SHORT_ND_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
  }

  /**
   * Tests that long and short digital options give the same results with different signs.
   */
  @Test
  public void testLongShortDigitalOption() {
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle longCall = LONG_DIGITAL_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle shortCall = SHORT_DIGITAL_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle longPut = LONG_DIGITAL_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilityNodeSensitivityDataBundle shortPut = SHORT_DIGITAL_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
  }

  /**
   * Tests that long and short single barrier options give the same results with different signs.
   */
  @Test
  public void testLongShortSingleBarrierOption() {
    PresentValueForexBlackVolatilityNodeSensitivityDataBundle longCall = LONG_DOWN_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    PresentValueForexBlackVolatilityNodeSensitivityDataBundle shortCall = SHORT_DOWN_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    PresentValueForexBlackVolatilityNodeSensitivityDataBundle longPut= LONG_DOWN_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    PresentValueForexBlackVolatilityNodeSensitivityDataBundle shortPut = SHORT_DOWN_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
    longCall = LONG_DOWN_KNOCK_OUT_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortCall = SHORT_DOWN_KNOCK_OUT_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    longPut= LONG_DOWN_KNOCK_OUT_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortPut = SHORT_DOWN_KNOCK_OUT_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
    longCall = LONG_UP_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortCall = SHORT_UP_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    longPut= LONG_UP_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortPut = SHORT_UP_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
    longCall = LONG_UP_KNOCK_OUT_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortCall = SHORT_UP_KNOCK_OUT_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    longPut= LONG_UP_KNOCK_OUT_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    shortPut = SHORT_UP_KNOCK_OUT_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
  }

  /**
   * Tests that sensitivity values are equal after scaling.
   * @param sensitivity1  the first sensitivities
   * @param sensitivity2  the second sensitivities
   * @param scale  the scale
   */
  private static void assertEqualsAfterScaling(final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensitivity1, final PresentValueForexBlackVolatilityNodeSensitivityDataBundle sensitivity2, final double scale) {
    assertEquals(sensitivity1.getCurrencyPair(), sensitivity2.getCurrencyPair());
    assertEquals(sensitivity1.getDelta(), sensitivity2.getDelta());
    assertEquals(sensitivity1.getExpiries(), sensitivity2.getExpiries());
    for (int i = 0; i < sensitivity1.getVega().getNumberOfRows(); i++) {
      for (int j = 0; j < sensitivity1.getVega().getNumberOfColumns(); j++) {
        assertEquals(sensitivity1.getVega().getEntry(i, j), sensitivity2.getVega().getEntry(i, j) * scale, 2e-15);
      }
    }
  }

}
