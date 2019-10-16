/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.EmptyWorkingDayCalendar;
import com.mcleodmoores.date.SimpleWorkingDayCalendar;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TenorUtilsTest {
  /** Empty holiday calendar */
  private static final WorkingDayCalendar NO_HOLIDAYS = EmptyWorkingDayCalendar.INSTANCE;
  /** Holiday calendar containing only weekends */
  private static final WorkingDayCalendar WEEKEND_CALENDAR = WeekendWorkingDayCalendar.SATURDAY_SUNDAY;
  /** Holiday calendar containing weekends and 1/1/2014 */
  private static final WorkingDayCalendar CALENDAR = new SimpleWorkingDayCalendar("test", Arrays.asList(LocalDate.of(2013, 1, 1), LocalDate.of(2014, 1, 1)),
      DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdjustZonedDateTime1() {
    final ZonedDateTime zonedDateTime = DateUtils.getUTCDate(2013, 12, 31);
    assertEquals(DateUtils.getUTCDate(2014, 12, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_YEAR));
    assertEquals(DateUtils.getUTCDate(2014, 1, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_MONTH));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_DAY));
    TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ON);
  }

  /**
   *
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testDeprecatedAdjustZonedDateTime2() {
    final ZonedDateTime zonedDateTime = DateUtils.getUTCDate(2013, 12, 31);
    assertEquals(DateUtils.getUTCDate(2014, 12, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_YEAR, CalendarAdapter.of(CALENDAR), 0));
    assertEquals(DateUtils.getUTCDate(2014, 1, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_MONTH, CalendarAdapter.of(CALENDAR), 0));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_DAY, CalendarAdapter.of(CALENDAR), 0));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.ON, CalendarAdapter.of(NO_HOLIDAYS), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.TN, CalendarAdapter.of(NO_HOLIDAYS), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.SN, CalendarAdapter.of(NO_HOLIDAYS), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.ON, CalendarAdapter.of(WEEKEND_CALENDAR), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.TN, CalendarAdapter.of(WEEKEND_CALENDAR), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.SN, CalendarAdapter.of(WEEKEND_CALENDAR), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.ON, CalendarAdapter.of(NO_HOLIDAYS), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.TN, CalendarAdapter.of(NO_HOLIDAYS), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.SN, CalendarAdapter.of(NO_HOLIDAYS), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.ON, CalendarAdapter.of(WEEKEND_CALENDAR), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.TN, CalendarAdapter.of(WEEKEND_CALENDAR), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3),
        TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.SN, CalendarAdapter.of(WEEKEND_CALENDAR), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.ON, CalendarAdapter.of(CALENDAR), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.TN, CalendarAdapter.of(CALENDAR), 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.SN, CalendarAdapter.of(CALENDAR), 2));
  }

  /**
   *
   */
  @Test
  public void testAdjustZonedDateTime2() {
    final ZonedDateTime zonedDateTime = DateUtils.getUTCDate(2013, 12, 31);
    assertEquals(DateUtils.getUTCDate(2014, 12, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_YEAR, CALENDAR, 0));
    assertEquals(DateUtils.getUTCDate(2014, 1, 31), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_MONTH, CALENDAR, 0));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(zonedDateTime, Tenor.ONE_DAY, CALENDAR, 0));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.ON, NO_HOLIDAYS, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.TN, NO_HOLIDAYS, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.SN, NO_HOLIDAYS, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.ON, WEEKEND_CALENDAR, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 1), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.TN, WEEKEND_CALENDAR, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.SN, WEEKEND_CALENDAR, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.ON, NO_HOLIDAYS, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.TN, NO_HOLIDAYS, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.SN, NO_HOLIDAYS, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.ON, WEEKEND_CALENDAR, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.TN, WEEKEND_CALENDAR, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 3), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2014, 1, 1), Tenor.SN, WEEKEND_CALENDAR, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.ON, CALENDAR, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.TN, CALENDAR, 2));
    assertEquals(DateUtils.getUTCDate(2014, 1, 2), TenorUtils.adjustDateByTenor(DateUtils.getUTCDate(2013, 12, 31), Tenor.SN, CALENDAR, 2));
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdjustLocalDateTime1() {
    final LocalDateTime localDateTime = LocalDateTime.of(2013, 12, 31, 11, 0);
    assertEquals(LocalDateTime.of(2014, 12, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_YEAR));
    assertEquals(LocalDateTime.of(2014, 1, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_MONTH));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_DAY));
    TenorUtils.adjustDateByTenor(localDateTime, Tenor.ON);
  }

  /**
   *
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testDeprecatedAdjustLocalDateTime2() {
    final int spotDays = 2;
    final LocalDateTime localDateTime = LocalDateTime.of(2013, 12, 31, 11, 0);
    assertEquals(LocalDateTime.of(2014, 12, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_YEAR, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_MONTH, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_DAY, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.ON, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.TN, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.SN, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.ON, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.TN, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.SN, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.ON, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.TN, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 4, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.SN, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.ON, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.TN, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 6, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.SN, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.ON, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.TN, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 6, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.SN, CalendarAdapter.of(CALENDAR), spotDays));
  }

  /**
   *
   */
  @Test
  public void testAdjustLocalDateTime2() {
    final int spotDays = 2;
    final LocalDateTime localDateTime = LocalDateTime.of(2013, 12, 31, 11, 0);
    assertEquals(LocalDateTime.of(2014, 12, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_YEAR, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 31, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_MONTH, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(localDateTime, Tenor.ONE_DAY, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 1, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0),
        TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 4, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 6, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2014, 1, 1, 11, 0), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 2, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.ON, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 3, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.TN, CALENDAR, spotDays));
    assertEquals(LocalDateTime.of(2014, 1, 6, 11, 0), TenorUtils.adjustDateByTenor(LocalDateTime.of(2013, 12, 31, 11, 0), Tenor.SN, CALENDAR, spotDays));
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAdjustLocalDate1() {
    final LocalDate localDate = LocalDate.of(2013, 12, 31);
    assertEquals(LocalDate.of(2014, 12, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_YEAR));
    assertEquals(LocalDate.of(2014, 1, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_MONTH));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_DAY));
    TenorUtils.adjustDateByTenor(localDate, Tenor.ON);
  }

  /**
   *
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testDeprecatedAdjustLocalDate2() {
    final int spotDays = 2;
    final LocalDate localDate = LocalDate.of(2013, 12, 31);
    assertEquals(LocalDate.of(2014, 12, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_YEAR, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_MONTH, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_DAY, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.ON, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.TN, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.SN, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.ON, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.TN, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.SN, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.ON, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.TN, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDate.of(2014, 1, 4), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.SN, CalendarAdapter.of(NO_HOLIDAYS), spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.ON, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.TN, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 6), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.SN, CalendarAdapter.of(WEEKEND_CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.ON, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.TN, CalendarAdapter.of(CALENDAR), spotDays));
    assertEquals(LocalDate.of(2014, 1, 6), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.SN, CalendarAdapter.of(CALENDAR), spotDays));
  }

  /**
   *
   */
  @Test
  public void testAdjustLocalDate2() {
    final int spotDays = 2;
    final LocalDate localDate = LocalDate.of(2013, 12, 31);
    assertEquals(LocalDate.of(2014, 12, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_YEAR, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 31), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_MONTH, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(localDate, Tenor.ONE_DAY, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 1), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.ON, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.TN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 4), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.SN, NO_HOLIDAYS, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.ON, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.TN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 6), TenorUtils.adjustDateByTenor(LocalDate.of(2014, 1, 1), Tenor.SN, WEEKEND_CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 2), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.ON, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 3), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.TN, CALENDAR, spotDays));
    assertEquals(LocalDate.of(2014, 1, 6), TenorUtils.adjustDateByTenor(LocalDate.of(2013, 12, 31), Tenor.SN, CALENDAR, spotDays));
  }

  /**
   *
   */
  @Test
  public void plus() {
    final Tenor d1 = Tenor.ONE_DAY;
    final Tenor d2 = Tenor.TWO_DAYS;
    final Tenor w1 = Tenor.ONE_WEEK;
    final Tenor m1 = Tenor.ONE_MONTH;
    final Tenor y3 = Tenor.THREE_YEARS;
    final Tenor on = Tenor.ON;
    final Tenor tn = Tenor.TN;
    final Tenor pZ = Tenor.of(Period.ZERO);
    final Tenor p0D = Tenor.of(Period.ofDays(0));
    assertEquals("Tenor: plus", d2, TenorUtils.plus(d1, d1));
    assertEquals("Tenor: plus", Tenor.of(Period.ofDays(8)), TenorUtils.plus(d1, w1));
    assertEquals("Tenor: plus", Tenor.of(Period.of(3, 1, 0)), TenorUtils.plus(m1, y3));
    assertEquals("Tenor: plus", Tenor.of(Period.of(3, 1, 0)), TenorUtils.plus(y3, m1));
    assertEquals("Tenor: plus", tn, TenorUtils.plus(on, on));
    assertEquals("Tenor: plus", pZ, p0D);
    assertEquals("Tenor: plus", y3, TenorUtils.plus(pZ, y3));
    assertEquals("Tenor: plus", y3, TenorUtils.plus(y3, pZ));
    assertEquals("Tenor: plus", y3, TenorUtils.plus(y3, p0D));
  }

}
