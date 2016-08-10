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

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Unit tests for {@link PresentValueForexVolatilitySensitivityForexBlackSmileCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class PresentValueForexVolatilitySensitivityForexBlackSmileCalculatorTest {
  /** The calculator being tested. */
  private static final PresentValueForexVolatilitySensitivityForexBlackSmileCalculator CALCULATOR = PresentValueForexVolatilitySensitivityForexBlackSmileCalculator.getInstance();

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
    final PresentValueForexBlackVolatilitySensitivity longCall = LONG_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilitySensitivity shortCall = SHORT_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    final PresentValueForexBlackVolatilitySensitivity longPut = LONG_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilitySensitivity shortPut = SHORT_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
  }

  /**
   * Tests that long and short non-deliverable options give the same results with different signs.
   */
  @Test
  public void testLongShortNonDeliverableOption() {
    final PresentValueForexBlackVolatilitySensitivity longCall = LONG_ND_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilitySensitivity shortCall = SHORT_ND_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    final PresentValueForexBlackVolatilitySensitivity longPut = LONG_ND_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilitySensitivity shortPut = SHORT_ND_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
  }

  /**
   * Tests that long and short digital options give the same results with different signs.
   */
  @Test
  public void testLongShortDigitalOption() {
    final PresentValueForexBlackVolatilitySensitivity longCall = LONG_DIGITAL_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilitySensitivity shortCall = SHORT_DIGITAL_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    final PresentValueForexBlackVolatilitySensitivity longPut = LONG_DIGITAL_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    final PresentValueForexBlackVolatilitySensitivity shortPut = SHORT_DIGITAL_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
  }

  /**
   * Tests that long and short single barrier options give the same results with different signs.
   */
  @Test
  public void testLongShortSingleBarrierOption() {
    PresentValueForexBlackVolatilitySensitivity longCall = LONG_DOWN_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    PresentValueForexBlackVolatilitySensitivity shortCall = SHORT_DOWN_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    PresentValueForexBlackVolatilitySensitivity longPut= LONG_DOWN_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
    PresentValueForexBlackVolatilitySensitivity shortPut = SHORT_DOWN_KNOCK_IN_PUT.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITH_SMILE);
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
  private static void assertEqualsAfterScaling(final PresentValueForexBlackVolatilitySensitivity sensitivity1, final PresentValueForexBlackVolatilitySensitivity sensitivity2, final double scale) {
    assertEquals(sensitivity1.getCurrencyPair(), sensitivity2.getCurrencyPair());
    for (final Map.Entry<DoublesPair, Double> entry : sensitivity1.getVega().getMap().entrySet()) {
      final Double value2 = sensitivity2.getVega().getMap().get(entry.getKey());
      assertEquals(entry.getValue(), value2 * scale, 2e-15);
    }
  }
}
