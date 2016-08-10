/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DIGITAL_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_DOWN_KNOCK_IN_CALL;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_ND_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_VANILLA_CALL_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.LONG_VANILLA_PUT_OPTION;
import static com.opengamma.analytics.financial.provider.calculator.blackforex.FxCalculatorsTestUtils.MARKET_DATA_WITHOUT_SMILE;
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
 * Unit tests for {@link PresentValueForexVolatilitySensitivityForexBlackFlatCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class PresentValueForexVolatilitySensitivityForexBlackFlatCalculatorTest {
  /** The calculator being tested. */
  private static final PresentValueForexVolatilitySensitivityForexBlackFlatCalculator CALCULATOR = PresentValueForexVolatilitySensitivityForexBlackFlatCalculator.getInstance();

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
    final PresentValueForexBlackVolatilitySensitivity longCall = LONG_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
    final PresentValueForexBlackVolatilitySensitivity shortCall = SHORT_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    final PresentValueForexBlackVolatilitySensitivity longPut = LONG_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
    final PresentValueForexBlackVolatilitySensitivity shortPut = SHORT_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
    assertEqualsAfterScaling(longPut, shortPut, -1);
  }

  /**
   * Non-deliverable options are not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLongShortNonDeliverableOption() {
    LONG_ND_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
  }

  /**
   * Digital options are not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLongShortDigitalOption() {
    LONG_DIGITAL_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
  }

  /**
   * Single barrier options are not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLongShortSingleBarrierOption() {
    LONG_DOWN_KNOCK_IN_CALL.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
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
