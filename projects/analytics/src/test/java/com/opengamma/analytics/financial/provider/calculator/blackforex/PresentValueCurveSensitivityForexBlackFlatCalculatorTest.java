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
import static org.testng.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Unit tests for {@link PresentValueCurveSensitivityForexBlackFlatCalculator}.
 */
@Test(groups = TestGroup.UNIT)
public class PresentValueCurveSensitivityForexBlackFlatCalculatorTest {
  /** The calculator being tested. */
  private static final PresentValueCurveSensitivityForexBlackFlatCalculator CALCULATOR = PresentValueCurveSensitivityForexBlackFlatCalculator.getInstance();

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
    final MultipleCurrencyMulticurveSensitivity longCall = LONG_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
    final MultipleCurrencyMulticurveSensitivity shortCall = SHORT_VANILLA_CALL_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
    assertEqualsAfterScaling(longCall, shortCall, -1);
    final MultipleCurrencyMulticurveSensitivity longPut = LONG_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
    final MultipleCurrencyMulticurveSensitivity shortPut = SHORT_VANILLA_PUT_OPTION.toDerivative(VALUATION_DATE).accept(CALCULATOR, MARKET_DATA_WITHOUT_SMILE);
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
  private static void assertEqualsAfterScaling(final MultipleCurrencyMulticurveSensitivity sensitivity1, final MultipleCurrencyMulticurveSensitivity sensitivity2, final double scale) {
    final Map<Currency, MulticurveSensitivity> values1 = sensitivity1.getSensitivities();
    final Map<Currency, MulticurveSensitivity> values2 = sensitivity2.getSensitivities();
    assertEquals(values1.size(), values2.size());
    for (final Map.Entry<Currency, MulticurveSensitivity> entry1 : values1.entrySet()) {
      final MulticurveSensitivity value1 = entry1.getValue();
      final MulticurveSensitivity value2 = values2.get(entry1.getKey());
      assertNotNull(value2);
      final Map<String, List<DoublesPair>> y1 = value1.getYieldDiscountingSensitivities();
      final Map<String, List<DoublesPair>> y2 = value2.getYieldDiscountingSensitivities();
      assertEquals(y1.size(), y2.size());
      for (final Map.Entry<String, List<DoublesPair>> entry2 : y1.entrySet()) {
        final List<DoublesPair> l1 = entry2.getValue();
        final List<DoublesPair> l2 = y2.get(entry2.getKey());
        assertNotNull(l2);
        assertEquals(l1.size(), l2.size());
        for (int i = 0; i < l1.size(); i++) {
          assertEquals(l1.get(i).getFirst(), l2.get(i).getFirst(), 2e-15);
          assertEquals(l1.get(i).getSecond(), l2.get(i).getSecond() * scale, 2e-15);
        }
      }
      final Map<String, List<ForwardSensitivity>> f1 = value1.getForwardSensitivities();
      final Map<String, List<ForwardSensitivity>> f2 = value2.getForwardSensitivities();
      assertEquals(f1.size(), f2.size());
      for (final Map.Entry<String, List<ForwardSensitivity>> entry2 : f1.entrySet()) {
        final List<ForwardSensitivity> l1 = entry2.getValue();
        final List<ForwardSensitivity> l2 = f2.get(entry2.getKey());
        assertNotNull(l2);
        assertEquals(l1.size(), l2.size());
        for (int i = 0; i < l1.size(); i++) {
          assertEquals(l1.get(i).getStartTime(), l2.get(i).getStartTime(), 2e-15);
          assertEquals(l1.get(i).getEndTime(), l2.get(i).getEndTime(), 2e-15);
          assertEquals(l1.get(i).getAccrualFactor(), l2.get(i).getAccrualFactor(), 2e-15);
          assertEquals(l1.get(i).getValue(), l2.get(i).getValue() * scale, 2e-15);
        }
      }
    }
  }
}
