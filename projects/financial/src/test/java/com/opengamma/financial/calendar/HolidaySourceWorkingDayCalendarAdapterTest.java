/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.engine.InMemoryHolidaySource;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.holiday.ManageableHolidayWithWeekend;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link HolidaySourceWorkingDayCalendarAdapter}.
 */
public class HolidaySourceWorkingDayCalendarAdapterTest {
  /** EU region */
  private static final SimpleRegion EU = new SimpleRegion();
  /** BH region */
  private static final SimpleRegion BH = new SimpleRegion();
  /** EU holiday dates */
  private static final Collection<LocalDate> EU_DATES = Arrays.asList(LocalDate.of(2015, 1, 1), LocalDate.of(2015, 12, 25));
  /** BH holiday dates */
  private static final Collection<LocalDate> BH_DATES = Arrays.asList(LocalDate.of(2015, 1, 8), LocalDate.of(2015, 12, 24));
  /** USD holiday dates */
  private static final Collection<LocalDate> USD_DATES = Arrays.asList(LocalDate.of(2015, 1, 15), LocalDate.of(2015, 11, 25));
  /** SAR holiday dates */
  private static final Collection<LocalDate> SAR_DATES = Arrays.asList(LocalDate.of(2015, 1, 11), LocalDate.of(2015, 8, 12));
  /** EU bank holidays with no weekend information */
  private static final ManageableHoliday EU_HOLIDAY = new ManageableHoliday(HolidayType.BANK, ExternalSchemes.countryRegionId(Country.EU),
      EU_DATES);
  /** BH bank holidays with weekend information */
  private static final ManageableHoliday BH_HOLIDAY = new ManageableHolidayWithWeekend(HolidayType.BANK, ExternalSchemes.countryRegionId(Country.of("BH")),
      BH_DATES, WeekendType.FRIDAY_SATURDAY);
  /** USD holidays with no weekend information */
  private static final ManageableHoliday USD_HOLIDAY = new ManageableHoliday(Currency.USD, USD_DATES);
  /** SAR bank holidays with weekend information */
  private static final ManageableHoliday SAR_HOLIDAY = new ManageableHolidayWithWeekend(Currency.of("SAR"), SAR_DATES, WeekendType.THURSDAY_FRIDAY);
  /** The holiday source */
  private static final InMemoryHolidaySource HOLIDAY_SOURCE = new InMemoryHolidaySource(false);

  static {
    EU.setCountry(Country.EU);
    EU.setCurrency(Currency.EUR);
    EU.setName("Eurozone");
    BH.setCountry(Country.of("BH"));
    BH.setCurrency(Currency.of("BHD"));
    BH.setName("Bahrain");
    HOLIDAY_SOURCE.addHoliday(ExternalSchemes.countryRegionId(Country.EU), EU_HOLIDAY);
    HOLIDAY_SOURCE.addHoliday(ExternalSchemes.countryRegionId(Country.of("BH")), BH_HOLIDAY);
    HOLIDAY_SOURCE.addHoliday(Currency.USD, USD_HOLIDAY);
    HOLIDAY_SOURCE.addHoliday(Currency.of("SAR"), SAR_HOLIDAY);
  }

  /**
   * Tests the behaviour when the holiday source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHolidaySource1() {
    new HolidaySourceWorkingDayCalendarAdapter(null, EU);
  }

  /**
   * Tests the behaviour when the holiday source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHolidaySource2() {
    new HolidaySourceWorkingDayCalendarAdapter(null, Currency.USD);
  }

  /**
   * Tests the behaviour when the region is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegion() {
    new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, (Region) null);
  }

  /**
   * Tests the behaviour when the currency is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, (Currency) null);
  }

  /**
   * Tests the behaviour when the holiday is not found in the source.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testMissingHolidayForRegion() {
    final SimpleRegion region = new SimpleRegion();
    region.setCountry(Country.AR);
    final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, region);
    calendar.isHoliday(LocalDate.of(2015, 1, 1));
  }

  /**
   * Tests the behaviour when the holiday is not found in the source.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testMissingHolidayForCurrency() {
    final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, Currency.AUD);
    calendar.isHoliday(LocalDate.of(2015, 1, 1));
  }

  /**
   * Tests the name.
   */
  @Test
  public void testName() {
    WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, EU);
    assertEquals(calendar.getName(), "Eurozone Bank");
    calendar = new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, Currency.USD);
    assertEquals(calendar.getName(), "USD Currency");
  }

  /**
   * Tests that the correct holidays are retrieved when the reference is a region and
   * default weekend days (Saturday and Sunday) are used.
   */
  @Test
  public void testRegionHolidayDefaultWeekends() {
    final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, EU);
    LocalDate date = LocalDate.of(2015, 1, 1);
    while (!date.isAfter(LocalDate.of(2016, 1, 1))) {
      if (EU_DATES.contains(date)) {
        assertTrue(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
        assertFalse(calendar.isWeekend(date));
      } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        assertFalse(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
        assertTrue(calendar.isWeekend(date));
      } else {
        assertFalse(calendar.isHoliday(date));
        assertTrue(calendar.isWorkingDay(date));
        assertFalse(calendar.isWeekend(date));
      }
      date = date.plusDays(1);
    }
  }

  /**
   * Tests that the correct holidays are retrieved when the reference is a region and
   * explicit weekend days are used.
   */
  @Test
  public void testRegionHolidayWithWeekends() {
    final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, BH);
    LocalDate date = LocalDate.of(2015, 1, 1);
    while (!date.isAfter(LocalDate.of(2016, 1, 1))) {
      if (BH_DATES.contains(date)) {
        assertTrue(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
        assertFalse(calendar.isWeekend(date));
      } else if (date.getDayOfWeek() == DayOfWeek.FRIDAY || date.getDayOfWeek() == DayOfWeek.SATURDAY) {
        assertFalse(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
        assertTrue(calendar.isWeekend(date));
      } else {
        assertFalse(calendar.isHoliday(date));
        assertTrue(calendar.isWorkingDay(date));
        assertFalse(calendar.isWeekend(date));
      }
      date = date.plusDays(1);
    }
  }

  /**
   * Tests that the correct holidays are retrieved when the reference is a currency and
   * default weekend days (Saturday and Sunday) are used.
   */
  @Test
  public void testCurrencyHolidayDefaultWeekends() {
    final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, Currency.USD);
    LocalDate date = LocalDate.of(2015, 1, 1);
    while (!date.isAfter(LocalDate.of(2016, 1, 1))) {
      if (USD_DATES.contains(date)) {
        assertTrue(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
        assertFalse(calendar.isWeekend(date));
      } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        assertFalse(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
        assertTrue(calendar.isWeekend(date));
      } else {
        assertFalse(calendar.isHoliday(date));
        assertTrue(calendar.isWorkingDay(date));
        assertFalse(calendar.isWeekend(date));
      }
      date = date.plusDays(1);
    }
  }

  /**
   * Tests that the correct holidays are retrieved when the reference is a currency and
   * explicit weekend days are used.
   */
  @Test
  public void testCurrencyHolidayWithWeekends() {
    final WorkingDayCalendar calendar = new HolidaySourceWorkingDayCalendarAdapter(HOLIDAY_SOURCE, Currency.of("SAR"));
    LocalDate date = LocalDate.of(2015, 1, 1);
    while (!date.isAfter(LocalDate.of(2016, 1, 1))) {
      if (SAR_DATES.contains(date)) {
        assertTrue(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
        assertFalse(calendar.isWeekend(date));
      } else if (date.getDayOfWeek() == DayOfWeek.THURSDAY || date.getDayOfWeek() == DayOfWeek.FRIDAY) {
        assertFalse(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
        assertTrue(calendar.isWeekend(date));
      } else {
        assertFalse(calendar.isHoliday(date));
        assertTrue(calendar.isWorkingDay(date));
        assertFalse(calendar.isWeekend(date));
      }
      date = date.plusDays(1);
    }
  }
}
