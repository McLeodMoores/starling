/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFixedCompoundingONCompoundingDiscountingMethodTest {

  // Data
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 9, 25);

  private static final MulticurveProviderDiscount CURVES = MulticurveProviderDiscountDataSets.createMulticurveBrl();
  private static final BlackFlatSwaptionParameters BLACK = BlackDataSets.createBlackSwaptionBrl();
  private static final WorkingDayCalendar CALENDAR = BlackDataSets.getBrlCalendar();

  private static final GeneratorSwapFixedCompoundedONCompounded GENERATOR_OIS_BRL = (GeneratorSwapFixedCompoundedONCompounded) BLACK
      .getGeneratorSwap();

  private static final Period EXPIRY_TENOR = Period.ofMonths(26); // To be between nodes.
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_TENOR,
      GENERATOR_OIS_BRL.getBusinessDayConvention(), CALENDAR, GENERATOR_OIS_BRL.isEndOfMonth());
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, GENERATOR_OIS_BRL.getSpotLag(),
      CALENDAR);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final double NOTIONAL = 123456789.0;
  private static final double RATE = 0.02;
  private static final SwapFixedCompoundedONCompoundedDefinition SWAP_REC_DEFINITION = SwapFixedCompoundedONCompoundedDefinition.from(
      SETTLE_DATE, SWAP_TENOR,
      NOTIONAL, GENERATOR_OIS_BRL, RATE, false);
  @SuppressWarnings("unchecked")
  private static final Swap<CouponFixedAccruedCompounding, CouponONCompounded> SWAP_REC = (Swap<CouponFixedAccruedCompounding, CouponONCompounded>) SWAP_REC_DEFINITION
      .toDerivative(REFERENCE_DATE);

  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final SwapFixedCompoundingONCompoundingDiscountingMethod METHOD_SWAP = SwapFixedCompoundingONCompoundingDiscountingMethod
      .getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-10;

  /**
   *
   */
  @Test
  public void forward() {
    final double forward = METHOD_SWAP.forward(SWAP_REC, CURVES);
    final SwapFixedCompoundedONCompoundedDefinition swap0Definition = SwapFixedCompoundedONCompoundedDefinition.from(SETTLE_DATE,
        SWAP_TENOR, NOTIONAL, GENERATOR_OIS_BRL, forward, false);
    final MultipleCurrencyAmount pv0 = swap0Definition.toDerivative(REFERENCE_DATE).accept(PVC, CURVES);
    assertEquals(pv0.size(), 1);
    assertEquals(0.0, pv0.getAmount(Currency.BRL), TOLERANCE_PV);
  }

  /**
   *
   */
  @Test
  public void forwardModified() {
    final double forwardModified = METHOD_SWAP.forwardModified(SWAP_REC, CURVES);
    final double forward = METHOD_SWAP.forward(SWAP_REC, CURVES);
    final double forwardModifiedExpected = Math.pow(1.0d + forward, SWAP_REC.getFirstLeg().getNthPayment(0).getPaymentYearFraction())
        - 1.0d;
    assertEquals(forwardModifiedExpected, forwardModified, TOLERANCE_RATE);
  }

}
