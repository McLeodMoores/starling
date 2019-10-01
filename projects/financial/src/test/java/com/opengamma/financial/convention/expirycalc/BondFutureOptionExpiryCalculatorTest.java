/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureOptionExpiryCalculatorTest {

  private static final BondFutureOptionExpiryCalculator CALCULATOR = BondFutureOptionExpiryCalculator.getInstance();
  static final WorkingDayCalendar WEEKEND_CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  private static final WorkingDayCalendar CALENDAR = new MyCalendar();
  private static final LocalDate AUGUST = LocalDate.of(2012, 8, 1);
  private static final LocalDate SEPTEMBER_START = LocalDate.of(2012, 9, 1);
  private static final LocalDate SEPTEMBER_END = LocalDate.of(2012, 9, 29);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeN() {
    CALCULATOR.getExpiryDate(-1, AUGUST, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroN() {
    CALCULATOR.getExpiryDate(0, AUGUST, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    CALCULATOR.getExpiryDate(1, null, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    CALCULATOR.getExpiryDate(2, AUGUST, (WorkingDayCalendar) null);
  }

  // @Test
  public void testExpiryMonthBeforeExpiry() {
    assertEquals(LocalDate.of(2012, 9, 21), CALCULATOR.getExpiryDate(1, SEPTEMBER_START, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 9, 21), CALCULATOR.getExpiryDate(1, SEPTEMBER_START, CALENDAR));
    assertEquals(LocalDate.of(2012, 10, 26), CALCULATOR.getExpiryDate(2, SEPTEMBER_START, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 10, 26), CALCULATOR.getExpiryDate(2, SEPTEMBER_START, CALENDAR));
    assertEquals(LocalDate.of(2012, 11, 23), CALCULATOR.getExpiryDate(3, SEPTEMBER_START, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 11, 22), CALCULATOR.getExpiryDate(3, SEPTEMBER_START, CALENDAR));
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(4, SEPTEMBER_START, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(4, SEPTEMBER_START, CALENDAR));
    assertEquals(LocalDate.of(2013, 1, 25), CALCULATOR.getExpiryDate(5, SEPTEMBER_START, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 1, 25), CALCULATOR.getExpiryDate(5, SEPTEMBER_START, CALENDAR));
    assertEquals(LocalDate.of(2013, 2, 22), CALCULATOR.getExpiryDate(6, SEPTEMBER_START, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 2, 22), CALCULATOR.getExpiryDate(6, SEPTEMBER_START, CALENDAR));
    assertEquals(LocalDate.of(2013, 4, 26), CALCULATOR.getExpiryDate(8, SEPTEMBER_START, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 4, 19), CALCULATOR.getExpiryDate(8, SEPTEMBER_START, CALENDAR));
  }

  @Test
  public void testExpiryMonthAfterExpiry() {
    assertEquals(LocalDate.of(2012, 10, 26), CALCULATOR.getExpiryDate(1, SEPTEMBER_END, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 10, 26), CALCULATOR.getExpiryDate(1, SEPTEMBER_END, CALENDAR));
    assertEquals(LocalDate.of(2012, 11, 23), CALCULATOR.getExpiryDate(2, SEPTEMBER_END, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 11, 22), CALCULATOR.getExpiryDate(2, SEPTEMBER_END, CALENDAR));
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(3, SEPTEMBER_END, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012, 12, 21), CALCULATOR.getExpiryDate(3, SEPTEMBER_END, CALENDAR));
    assertEquals(LocalDate.of(2013, 1, 25), CALCULATOR.getExpiryDate(4, SEPTEMBER_END, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 1, 25), CALCULATOR.getExpiryDate(4, SEPTEMBER_END, CALENDAR));
    assertEquals(LocalDate.of(2013, 2, 22), CALCULATOR.getExpiryDate(5, SEPTEMBER_END, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 2, 22), CALCULATOR.getExpiryDate(5, SEPTEMBER_END, CALENDAR));
    assertEquals(LocalDate.of(2013, 3, 22), CALCULATOR.getExpiryDate(6, SEPTEMBER_END, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 3, 22), CALCULATOR.getExpiryDate(6, SEPTEMBER_END, CALENDAR));
    assertEquals(LocalDate.of(2013, 4, 26), CALCULATOR.getExpiryDate(7, SEPTEMBER_END, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 4, 19), CALCULATOR.getExpiryDate(7, SEPTEMBER_END, CALENDAR));

  }

  private static class MyCalendar implements WorkingDayCalendar {
    private static final LocalDate HOLIDAY1 = LocalDate.of(2012, 12, 28);
    private static final LocalDate HOLIDAY2 = LocalDate.of(2012, 11, 23);
    private static final LocalDate HOLIDAY3 = LocalDate.of(2013, 2, 2);
    private static final LocalDate HOLIDAY4 = LocalDate.of(2013, 4, 29);

    MyCalendar() {
    }

    @Override
    public String getName() {
      return "MyCalendar";
    }

    @Override
    public boolean isWorkingDay(final LocalDate date) {
      return !isHoliday(date);
    }

    @Override
    public boolean isHoliday(final LocalDate date) {
      if (date.equals(HOLIDAY1) || date.equals(HOLIDAY2) || date.equals(HOLIDAY3) || date.equals(HOLIDAY4)) {
        return true;
      }
      return WEEKEND_CALENDAR.isHoliday(date);
    }

    @Override
    public boolean isWeekend(final LocalDate date) {
      return WEEKEND_CALENDAR.isWeekend(date);
    }

  }
}
