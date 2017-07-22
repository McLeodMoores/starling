/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
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
 * Tests related to the construction of bills transaction.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedBillTransactionTest {

  private static final Currency EUR = Currency.EUR;
  private static final WorkingDayCalendar WEEKEND = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 16);

  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final int SETTLEMENT_DAYS = 2;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");

  private static final String ISSUER_BEL = "BELGIUM GOVT";
  private static final ZonedDateTime END_DATE = DateUtils.getUTCDate(2012, 2, 29);
  private static final double NOTIONAL = 1000;
  private static final BillSecurityDefinition BILL_SEC_DEFINITION = new BillSecurityDefinition(EUR, END_DATE, NOTIONAL, SETTLEMENT_DAYS, WEEKEND, YIELD_CONVENTION, ACT360, ISSUER_BEL);

  private static final double QUANTITY = 123456;
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, 3, CALENDAR);
  private static final double SETTLE_AMOUT = -NOTIONAL * QUANTITY * 99.95;

  private static final String DSC_NAME = "EUR Discounting";
  private static final String CREDIT_NAME = "EUR BELGIUM GOVT";

  private static final BillSecurity BILL_PURCHASE = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE, SETTLE_DATE, DSC_NAME, CREDIT_NAME);
  private static final BillSecurity BILL_STANDARD = BILL_SEC_DEFINITION.toDerivative(REFERENCE_DATE, DSC_NAME, CREDIT_NAME);
  private static final BillTransaction BILL_TRA = new BillTransaction(BILL_PURCHASE, QUANTITY, SETTLE_AMOUT, BILL_STANDARD);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPurchase() {
    new BillTransaction(null, QUANTITY, SETTLE_AMOUT, BILL_STANDARD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStandard() {
    new BillTransaction(BILL_PURCHASE, QUANTITY, SETTLE_AMOUT, null);
  }

  @Test
  /**
   * Tests the bill getters.
   */
  public void getters() {
    assertEquals("Bill transation: getter", BILL_PURCHASE, BILL_TRA.getBillPurchased());
    assertEquals("Bill transation: getter", QUANTITY, BILL_TRA.getQuantity());
    assertEquals("Bill transation: getter", SETTLE_AMOUT, BILL_TRA.getSettlementAmount());
    assertEquals("Bill transation: getter", BILL_STANDARD, BILL_TRA.getBillStandard());
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void equalHash() {
    assertEquals("Bill transaction: equal-hash code", BILL_TRA, BILL_TRA);
    final BillTransaction other = new BillTransaction(BILL_PURCHASE, QUANTITY, SETTLE_AMOUT, BILL_STANDARD);
    assertEquals("Bill transaction: equal-hash code", BILL_TRA, other);
    assertEquals("Bill transaction: equal-hash code", BILL_TRA.hashCode(), other.hashCode());
    BillTransaction modified;
    modified = new BillTransaction(BILL_STANDARD, QUANTITY, SETTLE_AMOUT, BILL_STANDARD);
    assertFalse("Bill transaction: equal-hash code", BILL_TRA.equals(modified));
    modified = new BillTransaction(BILL_PURCHASE, QUANTITY + 1.0, SETTLE_AMOUT, BILL_STANDARD);
    assertFalse("Bill transaction: equal-hash code", BILL_TRA.equals(modified));
    modified = new BillTransaction(BILL_PURCHASE, QUANTITY, SETTLE_AMOUT + 1.0, BILL_STANDARD);
    assertFalse("Bill transaction: equal-hash code", BILL_TRA.equals(modified));
    modified = new BillTransaction(BILL_PURCHASE, QUANTITY, SETTLE_AMOUT, BILL_PURCHASE);
    assertFalse("Bill transaction: equal-hash code", BILL_TRA.equals(modified));
  }
}
