/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.date;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link DefaultDateAdjustmentCalculator}.
 */
public class DefaultDateAdjustmentCalculatorTest {
  /** The calculator */
  private static final TenorOffsetDateAdjustmentCalculator CALCULATOR = DefaultDateAdjustmentCalculator.getInstance();
  /** Holiday dates */
  private static final Collection<LocalDate> DATES = Arrays.asList(
      LocalDate.of(2015, 9, 14),
      LocalDate.of(2015, 10, 9),
      LocalDate.of(2015, 12, 31));
  /** A working day calendar */
  private static final WorkingDayCalendar WORKING_DAY_CALENDAR = new SimpleWorkingDayCalendar("Simple", DATES, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

  /**
   * Tests the behaviour when the date to adjust is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    CALCULATOR.getSettlementDate(null, Tenor.ONE_MONTH, BusinessDayConventions.FOLLOWING, WORKING_DAY_CALENDAR);
  }

  /**
   * Tests the behaviour when the tenor to adjust by is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTenor() {
    CALCULATOR.getSettlementDate(LocalDate.of(2015, 1, 1), null, BusinessDayConventions.FOLLOWING, WORKING_DAY_CALENDAR);
  }

  /**
   * Tests the behaviour when the business day convention is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDayConvention() {
    CALCULATOR.getSettlementDate(LocalDate.of(2015, 1, 1), Tenor.ONE_MONTH, null, WORKING_DAY_CALENDAR);
  }

  /**
   * Tests the behaviour when the working day calendar is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWorkingDayCalendar() {
    CALCULATOR.getSettlementDate(LocalDate.of(2015, 1, 1), Tenor.ONE_MONTH, BusinessDayConventions.FOLLOWING, null);
  }

  /**
   * Tests the date adjustment.
   */
  @Test
  public void testAdjustment() {
    // settlement date does not fall on a holiday
    assertEquals(CALCULATOR.getSettlementDate(LocalDate.of(2015, 8, 7), Tenor.ONE_MONTH, BusinessDayConventions.FOLLOWING, WORKING_DAY_CALENDAR),
        LocalDate.of(2015, 9, 7));
    assertEquals(CALCULATOR.getSettlementDate(LocalDate.of(2015, 9, 7), Tenor.ON, BusinessDayConventions.FOLLOWING, WORKING_DAY_CALENDAR),
        LocalDate.of(2015, 9, 8));
    assertEquals(CALCULATOR.getSettlementDate(LocalDate.of(2015, 9, 7), Tenor.TN, BusinessDayConventions.FOLLOWING, WORKING_DAY_CALENDAR),
        LocalDate.of(2015, 9, 9));
    assertEquals(CALCULATOR.getSettlementDate(LocalDate.of(2015, 9, 7), Tenor.SN, BusinessDayConventions.FOLLOWING, WORKING_DAY_CALENDAR),
        LocalDate.of(2015, 9, 8));
  }

}
