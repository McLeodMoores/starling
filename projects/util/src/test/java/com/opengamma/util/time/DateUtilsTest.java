/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;
import static org.threeten.bp.Month.MARCH;

import java.util.Date;
import java.util.GregorianCalendar;

import org.fudgemsg.types.FudgeDate;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;

/**
 * Test DateUtils.
 */
@Test(groups = TestGroup.UNIT)
public class DateUtilsTest {
  private static final ZonedDateTime D1 = ZonedDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC);
  private static final ZonedDateTime D2 = ZonedDateTime.of(LocalDateTime.of(2001, 1, 1, 0, 0), ZoneOffset.UTC);
  private static final double EPS = 1e-9;

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDateInstant1() {
    DateUtils.getDifferenceInYears((Instant) null, D2.toInstant());
  }

  /**
   * Tests that the end date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDateInstant1() {
    DateUtils.getDifferenceInYears(D1.toInstant(), (Instant) null);
  }

  /**
   * Tests the difference in years, where the number of days per year is defined as 365.25.
   */
  @Test
  public void testDifferenceInYearsInstant1() {
    final double leapYearDays = 366;
    assertEquals(DateUtils.getDifferenceInYears(D1.toInstant(), D2.toInstant()) * DateUtils.DAYS_PER_YEAR / leapYearDays, 1, EPS);
    assertEquals(DateUtils.getDifferenceInYears(D2.toInstant(), D1.toInstant()) * DateUtils.DAYS_PER_YEAR / leapYearDays, -1, EPS);
    assertEquals(DateUtils.getDifferenceInYears(D1.toInstant(), D1.toInstant()) * DateUtils.DAYS_PER_YEAR / leapYearDays, 0, EPS);
  }

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDateInstant2() {
    DateUtils.getDifferenceInYears((Instant) null, D2.toInstant(), 365);
  }

  /**
   * Tests that the end date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDateInstant2() {
    DateUtils.getDifferenceInYears(D1.toInstant(), (Instant) null, 365);
  }

  /**
   * Tests the difference in years, where the number of days per year is user-defined.
   */
  @Test
  public void testDifferenceInYearsInstant2() {
    final double leapYearDays = 366;
    assertEquals(DateUtils.getDifferenceInYears(D1.toInstant(), D2.toInstant(), leapYearDays), 1, EPS);
    assertEquals(DateUtils.getDifferenceInYears(D2.toInstant(), D1.toInstant(), leapYearDays), -1, EPS);
  }

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDateZonedDateTime1() {
    DateUtils.getDifferenceInYears((ZonedDateTime) null, D2);
  }

  /**
   * Tests that the end date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDateZonedDateTime1() {
    DateUtils.getDifferenceInYears(D1, (ZonedDateTime) null);
  }

  /**
   * Tests the difference in years, where the number of days per year is defined as 365.25.
   */
  @Test
  public void testDifferenceInYearsZonedDateTime() {
    final double leapYearDays = 366;
    assertEquals(DateUtils.getDifferenceInYears(D1, D2) * DateUtils.DAYS_PER_YEAR / leapYearDays, 1, EPS);
    assertEquals(DateUtils.getDifferenceInYears(D2, D1) * DateUtils.DAYS_PER_YEAR / leapYearDays, -1, EPS);
  }

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDateLocalDate() {
    DateUtils.getDifferenceInYears((LocalDate) null, D2.toLocalDate());
  }

  /**
   * Tests that the end date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDateLocalDate() {
    DateUtils.getDifferenceInYears(D1.toLocalDate(), (LocalDate) null);
  }

  /**
   * Tests the difference in years, where the number of days per year is defined as 365.25.
   */
  @Test
  public void testDifferenceInYearsDate() {
    final double leapYearDays = 366;
    assertEquals(DateUtils.getDifferenceInYears(D1.toLocalDate(), D2.toLocalDate()) * DateUtils.DAYS_PER_YEAR / leapYearDays, 1, EPS);
    assertEquals(DateUtils.getDifferenceInYears(D2.toLocalDate(), D1.toLocalDate()) * DateUtils.DAYS_PER_YEAR / leapYearDays, -1, EPS);
  }

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDateOffsetWithYearFractionNullStart1() {
    DateUtils.getDateOffsetWithYearFraction((Instant) null, 1);
  }

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDateOffsetWithYearFractionNullStart2() {
    DateUtils.getDateOffsetWithYearFraction((ZonedDateTime) null, 1);
  }

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDateOffsetWithYearFractionNullStart3() {
    DateUtils.getDateOffsetWithYearFraction((Instant) null, 1, 366);
  }

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDateOffsetWithYearFractionNullStart4() {
    DateUtils.getDateOffsetWithYearFraction((ZonedDateTime) null, 1, 366);
  }

  /**
   * Tests the addition of a year fraction to a date.
   */
  @Test
  public void testDateOffsetWithYearFraction() {
    final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2001, 1, 1, 0, 0), ZoneOffset.UTC);
    final ZonedDateTime offsetDateWithFinancialYearDefinition = ZonedDateTime.of(LocalDateTime.of(2002, 1, 1, 6, 0), ZoneOffset.UTC);
    final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2002, 1, 1, 0, 0), ZoneOffset.UTC);
    final double daysPerYear = 365;
    assertEquals(DateUtils.getDateOffsetWithYearFraction(startDate.toInstant(), 1), offsetDateWithFinancialYearDefinition.toInstant());
    assertEquals(DateUtils.getDateOffsetWithYearFraction(startDate, 1), offsetDateWithFinancialYearDefinition);
    assertEquals(DateUtils.getDateOffsetWithYearFraction(startDate.toInstant(), 1, daysPerYear), endDate.toInstant());
    assertEquals(DateUtils.getDateOffsetWithYearFraction(startDate, 1, daysPerYear), endDate);
  }

  /**
   * Tests construction of a UTC date.
   */
  @Test
  public void testUTCDate() {
    final int year = 2009;
    final int month = 9;
    final int day = 1;
    ZonedDateTime date = DateUtils.getUTCDate(year, month, day);
    assertEquals(date.getYear(), year);
    assertEquals(date.getMonthValue(), month);
    assertEquals(date.getDayOfMonth(), day);
    assertEquals(date.getHour(), 0);
    assertEquals(date.getMinute(), 0);
    assertEquals(date.getZone(), ZoneOffset.UTC);
    final int hour = 6;
    final int minutes = 31;
    date = DateUtils.getUTCDate(year, month, day, hour, minutes);
    assertEquals(date.getYear(), year);
    assertEquals(date.getMonthValue(), month);
    assertEquals(date.getDayOfMonth(), day);
    assertEquals(date.getHour(), hour);
    assertEquals(date.getMinute(), minutes);
    assertEquals(date.getZone(), ZoneOffset.UTC);
  }

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDateZonedDateTime2() {
    DateUtils.getExactDaysBetween(null, D2);
  }

  /**
   * Tests that the end date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDateZonedDateTime2() {
    DateUtils.getExactDaysBetween(D1, null);
  }

  /**
   * Tests the number of 24 hour periods in between 2 dates.
   */
  @Test
  public void testExactDaysBetween() {
    assertEquals(DateUtils.getExactDaysBetween(D1, D2), 366, EPS);
    assertEquals(DateUtils.getExactDaysBetween(D2, D1), -366, EPS);
  }

  /**
   * Tests that the start date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDate() {
    DateUtils.getDaysBetween(null, true, D2, false);
  }

  /**
   * Tests that the end date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDate() {
    DateUtils.getDaysBetween(D1, true, null, false);
  }

  /**
   * Tests the number of days between two dates.
   */
  @Test
  public void testDaysBetween() {
    assertEquals(DateUtils.getDaysBetween(D1, false, D2, false), 365);
    assertEquals(DateUtils.getDaysBetween(D1, true, D2, false), 366);
    assertEquals(DateUtils.getDaysBetween(D1, false, D2, true), 366);
    assertEquals(DateUtils.getDaysBetween(D1, true, D2, true), 367);
    assertEquals(DateUtils.getDaysBetween(D1, D2), 366);

    assertEquals(DateUtils.getDaysBetween(D2, false, D1, false), 365);
    assertEquals(DateUtils.getDaysBetween(D2, true, D1, false), 366);
    assertEquals(DateUtils.getDaysBetween(D2, false, D1, true), 366);
    assertEquals(DateUtils.getDaysBetween(D2, true, D1, true), 367);
    assertEquals(DateUtils.getDaysBetween(D2, D1), 366);

    assertEquals(DateUtils.getDaysBetween(D1, false, D1, false), -1);
    assertEquals(DateUtils.getDaysBetween(D1, true, D1, false), 0);
    assertEquals(DateUtils.getDaysBetween(D1, false, D1, true), 0);
    assertEquals(DateUtils.getDaysBetween(D1, true, D1, true), 1);
    assertEquals(DateUtils.getDaysBetween(D1, D1), 0);
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPrintNullDate1() {
    DateUtils.printYYYYMMDD(null);
  }

  /**
   * Tests date printing.
   */
  @Test
  public void testPrintYYYYMMDD() {
    final int year = 2009;
    final int month = 9;
    final int day = 1;
    final ZonedDateTime date = DateUtils.getUTCDate(year, month, day);
    assertEquals("20090901", DateUtils.printYYYYMMDD(date));
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPrintNullDate2() {
    DateUtils.printMMDD(null);
  }

  /**
   * Tests date printing.
   */
  public void testPrintMMDD() {
    final LocalDate test = LocalDate.of(2010, 1, 12);
    assertEquals("01-12", DateUtils.printMMDD(test));
  }

  /**
   * Tests the previous week day.
   */
  @Test
  public void testPreviousWeekDay() {
    final LocalDate sun = LocalDate.of(2009, 11, 8);
    final LocalDate sat = LocalDate.of(2009, 11, 7);
    final LocalDate fri = LocalDate.of(2009, 11, 6);
    final LocalDate thur = LocalDate.of(2009, 11, 5);
    final LocalDate wed = LocalDate.of(2009, 11, 4);
    final LocalDate tue = LocalDate.of(2009, 11, 3);
    final LocalDate mon = LocalDate.of(2009, 11, 2);
    final LocalDate lastFri = LocalDate.of(2009, 10, 30);

    assertEquals(fri, DateUtils.previousWeekDay(sun));
    assertEquals(fri, DateUtils.previousWeekDay(sat));
    assertEquals(thur, DateUtils.previousWeekDay(fri));
    assertEquals(wed, DateUtils.previousWeekDay(thur));
    assertEquals(tue, DateUtils.previousWeekDay(wed));
    assertEquals(mon, DateUtils.previousWeekDay(tue));
    assertEquals(lastFri, DateUtils.previousWeekDay(mon));
  }

  /**
   * Tests the next week day.
   */
  @Test
  public void testNextWeekDay() {
    final LocalDate sun = LocalDate.of(2009, 11, 8);
    final LocalDate sat = LocalDate.of(2009, 11, 7);
    final LocalDate fri = LocalDate.of(2009, 11, 6);
    final LocalDate thur = LocalDate.of(2009, 11, 5);
    final LocalDate wed = LocalDate.of(2009, 11, 4);
    final LocalDate tue = LocalDate.of(2009, 11, 3);
    final LocalDate mon = LocalDate.of(2009, 11, 2);
    final LocalDate nextMon = LocalDate.of(2009, 11, 9);

    assertEquals(nextMon, DateUtils.nextWeekDay(sun));
    assertEquals(nextMon, DateUtils.nextWeekDay(sat));
    assertEquals(nextMon, DateUtils.nextWeekDay(fri));
    assertEquals(fri, DateUtils.nextWeekDay(thur));
    assertEquals(thur, DateUtils.nextWeekDay(wed));
    assertEquals(wed, DateUtils.nextWeekDay(tue));
    assertEquals(tue, DateUtils.nextWeekDay(mon));
  }

  /**
   * Tests conversion to epoch millis.
   */
  @Test
  public void testUtcEpochMillis() {
    final long millis = DateUtils.getUTCEpochMilis(20100328);
    assertEquals(millis, 1269734400000L);
  }

  /**
   * Tests conversion to zone date time.
   */
  @Test
  public void testToZonedDateTime() {
    final ZonedDateTime date = DateUtils.toZonedDateTimeUTC(20100328);
    assertEquals(date, DateUtils.getUTCDate(2010, 3, 28));
  }

  /**
   * Tests conversion to local date time.
   */
  @Test
  public void testToLocalDate() {
    final LocalDate d20100328 = LocalDate.of(2010, MARCH, 28);
    final LocalDate localDate = DateUtils.toLocalDate(20100328);
    assertEquals(d20100328, localDate);
  }

  /**
   * Tests conversion from old Java dates.
   */
  @Test
  public void testConvertJavaUtilDate() {
    @SuppressWarnings("deprecation")
    final Date date = new Date(110, 0, 1);
    assertEquals(LocalDate.of(2010, 1, 1), DateUtils.fromDateFields(date));
  }

  /**
   * Tests conversion from old Java dates.
   */
  @Test
  public void testConvertJavaUtilCalendar() {
    final GregorianCalendar date = new GregorianCalendar(2010, 1, 1);
    assertEquals(ZonedDateTime.of(LocalDateTime.of(2010, 2, 1, 0, 0), ZoneId.of("UTC")), DateUtils.toZonedDateTime(date));
  }

  /**
   * Tests that a local date time cannot be converted.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionError() {
    DateUtils.toLocalDate(LocalDateTime.of(2018, 1, 1, 3, 0));
  }

  /**
   * Tests conversion of an object to a LocalDate.
   */
  @Test
  public void testToLocalDateFromObject() {
    assertEquals(LocalDate.of(2018, 1, 1), DateUtils.toLocalDate(LocalDate.of(2018, 1, 1)));
    assertEquals(LocalDate.of(2018, 1, 1), DateUtils.toLocalDate(FudgeDate.of(2018, 1, 1)));
  }

  /**
   * Creates a fixed clock.
   */
  @Test
  public void testCreateClock() {
    final Clock clock = DateUtils.fixedClockUTC(D1.toInstant());
    assertEquals(clock.getZone(), ZoneOffset.UTC);
    assertEquals(clock.instant(), D1.toInstant());
  }

  /**
   * Tests the estimated duration calculation.
   */
  @Test
  public void testEstimatedDuration() {
    final Period period = Period.of(0, 11, 4);
    assertEquals(DateUtils.estimatedDuration(period).getSeconds(), 29272806);
  }

  /**
   * Tests the toPeriod() method.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testPeriod() {
    assertEquals(DateUtils.toPeriod("PT0S"), Period.ZERO);
    assertEquals(DateUtils.toPeriod("P1D"), Period.ofDays(1));
  }
}
