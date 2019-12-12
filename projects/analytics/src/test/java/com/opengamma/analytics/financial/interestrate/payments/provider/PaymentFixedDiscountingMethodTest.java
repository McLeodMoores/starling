/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EurDepositGenerator;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.amount.StringAmount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the methods related to fixed coupons.
 */
@Test(groups = TestGroup.UNIT)
public class PaymentFixedDiscountingMethodTest {
  private static final WorkingDayCalendar EUR_CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final GeneratorDeposit DEPOSIT_EUR = new EurDepositGenerator(EUR_CALENDAR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final Period PAYMENT_PERIOD = Period.ofMonths(12);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator
      .getAdjustedDate(REFERENCE_DATE, PAYMENT_PERIOD, DEPOSIT_EUR.getBusinessDayConvention(), EUR_CALENDAR, DEPOSIT_EUR.isEndOfMonth());
  private static final double AMOUNT = 100000000;
  private static final PaymentFixedDefinition PAYMENT_DEFINITION = new PaymentFixedDefinition(DEPOSIT_EUR.getCurrency(), PAYMENT_DATE,
      AMOUNT);

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final PaymentFixed PAYMENT = PAYMENT_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final PaymentFixedDiscountingMethod METHOD = PaymentFixedDiscountingMethod.getInstance();

  /**
   * Tests the present value of fixed coupons.
   */
  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = METHOD.presentValue(PAYMENT, MULTICURVES);
    final double pvExpected = PAYMENT.getAmount() * MULTICURVES.getDiscountFactor(Currency.EUR, PAYMENT.getPaymentTime());
    assertEquals(pvExpected, pvComputed.getAmount(Currency.EUR), 1.0E-2);
  }

  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed payments.
   */
  @Test
  public void presentValueParallelCurveSensitivity() {
    final StringAmount pvpcsComputed = METHOD.presentValueParallelCurveSensitivity(PAYMENT, MULTICURVES);
    final double pvpcsExpected = -PAYMENT.getPaymentTime() * PAYMENT.getAmount()
        * MULTICURVES.getDiscountFactor(Currency.EUR, PAYMENT.getPaymentTime());
    assertEquals(1, pvpcsComputed.getMap().size());
    assertEquals(pvpcsExpected, pvpcsComputed.getMap().get("EUR Dsc"), 1e-2);
  }

}
