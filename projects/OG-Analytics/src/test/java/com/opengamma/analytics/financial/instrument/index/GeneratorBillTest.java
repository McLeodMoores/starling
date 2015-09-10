/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.date.WeekendWorkingDayCalendar;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Unit tests for {@link GeneratorBill}.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorBillTest {

  private static final Currency EUR = Currency.EUR;
  private static final WorkingDayCalendar CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final Calendar OLD_CALENDAR = new MondayToFridayCalendar("TARGET");

  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final int SETTLEMENT_DAYS = 2;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");

  private static final String ISSUER_BEL = "BELGIUM GOVT";
  private static final ZonedDateTime END_DATE = DateUtils.getUTCDate(2012, 9, 20);
  private static final double NOTIONAL = 1000;
  private static final BillSecurityDefinition BILL_BEL_SEC_DEFINITION =
      new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, CALENDAR, YIELD_CONVENTION, ACT360, ISSUER_BEL);

  private static final String GENERATOR_BILL_NAME = "BE0312683528";
  private static final GeneratorBill GENERATOR_BILL = new GeneratorBill(GENERATOR_BILL_NAME, BILL_BEL_SEC_DEFINITION);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 21);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new GeneratorBill(null, BILL_BEL_SEC_DEFINITION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlying() {
    new GeneratorBill(GENERATOR_BILL_NAME, null);
  }

  @Test
  public void generateInstrument() {
    final double marketQuote = -0.0001;
    final double notional = 123000;
    final double quantity = 123;
    final GeneratorAttribute attribute = new GeneratorAttribute();
    final BillTransactionDefinition billGenerated = GENERATOR_BILL.generateInstrument(REFERENCE_DATE, marketQuote, notional, attribute);
    final ZonedDateTime dettleDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, OLD_CALENDAR);
    final BillTransactionDefinition billExpected = BillTransactionDefinition.fromYield(BILL_BEL_SEC_DEFINITION, quantity, dettleDate, marketQuote, CALENDAR);
    assertEquals("Bill Generator: generate instrument", billExpected, billGenerated);
  }

}
