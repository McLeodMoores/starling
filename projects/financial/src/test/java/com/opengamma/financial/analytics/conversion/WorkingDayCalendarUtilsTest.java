/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.conversion;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.impl.SimpleHoliday;
import com.opengamma.core.holiday.impl.SimpleHolidayWithWeekend;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.impl.InMemoryHolidayMaster;
import com.opengamma.master.holiday.impl.MasterHolidaySource;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link WorkingDayCalendarUtils}.
 */
public class WorkingDayCalendarUtilsTest {
  /** A region master */
  private static final InMemoryRegionMaster REGION_MASTER = new InMemoryRegionMaster();
  /** A region source */
  private static final RegionSource REGION_SOURCE = new MasterRegionSource(REGION_MASTER);
  /** A holiday master */
  private static final InMemoryHolidayMaster HOLIDAY_MASTER = new InMemoryHolidayMaster();
  /** A holiday source */
  private static final HolidaySource HOLIDAY_SOURCE = new MasterHolidaySource(HOLIDAY_MASTER);
  /** US holiday dates */
  private static final List<LocalDate> HOLIDAY_DATES = Arrays.asList(
      LocalDate.of(2015, 3, 3),
      LocalDate.of(2015, 3, 10),
      LocalDate.of(2015, 3, 17));
  /** US region with country and currency identifier */
  private static final SimpleRegion US = new SimpleRegion();
  /** GB region with currency identifier */
  private static final SimpleRegion GB = new SimpleRegion();

  static {
    US.setCountry(Country.US);
    US.setCurrency(Currency.USD);
    GB.setCurrency(Currency.GBP);
    REGION_MASTER.add(new RegionDocument(US));
    REGION_MASTER.add(new RegionDocument(GB));
    final SimpleHoliday usHoliday = new SimpleHolidayWithWeekend(HOLIDAY_DATES, WeekendType.SATURDAY_SUNDAY);
    final SimpleHoliday gbHoliday = new SimpleHolidayWithWeekend(Collections.<LocalDate>emptyList(), WeekendType.SATURDAY_SUNDAY);
    usHoliday.setRegionExternalId(ExternalSchemes.countryRegionId(Country.US));
    usHoliday.setType(HolidayType.BANK);
    gbHoliday.setCurrency(Currency.GBP);
    gbHoliday.setType(HolidayType.CURRENCY);
    final HolidayDocument usDocument = new HolidayDocument(usHoliday);
    usDocument.setName("US");
    final HolidayDocument gbDocument = new HolidayDocument(gbHoliday);
    gbDocument.setName("GB");
    HOLIDAY_MASTER.add(usDocument);
    HOLIDAY_MASTER.add(gbDocument);
  }

  /**
   * Sets up the service context.
   */
  @BeforeSuite
  public static void setUp() {
    final VersionCorrectionProvider versionCorrectionProvider = new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    };
    final ServiceContext serviceContext = ServiceContext.of(VersionCorrectionProvider.class, versionCorrectionProvider)
        .with(HolidaySource.class, HOLIDAY_SOURCE);
    ThreadLocalServiceContext.init(serviceContext);
  }

  /**
   * Tests the behaviour when the region source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegionSource1() {
    WorkingDayCalendarUtils.getCalendarForRegion(null, HOLIDAY_SOURCE, ExternalSchemes.countryRegionId(Country.US));
  }

  /**
   * Tests the behaviour when the holiday source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHolidaySource1() {
    WorkingDayCalendarUtils.getCalendarForRegion(REGION_SOURCE, null, ExternalSchemes.countryRegionId(Country.GB));
  }

  /**
   * Tests the behaviour when the region id is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegionId1() {
    WorkingDayCalendarUtils.getCalendarForRegion(REGION_SOURCE, HOLIDAY_SOURCE, null);
  }

  /**
   * Tests the behaviour when the region identifier contains multiple region names separated by a +.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMultipleRegionIds1() {
    WorkingDayCalendarUtils.getCalendarForRegion(REGION_SOURCE, HOLIDAY_SOURCE, ExternalSchemes.financialRegionId("US+GB"));
  }

  /**
   * Tests the behaviour when the region identifier contains multiple region names separated by a ,.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMultipleRegionIds2() {
    WorkingDayCalendarUtils.getCalendarForRegion(REGION_SOURCE, HOLIDAY_SOURCE, ExternalSchemes.financialRegionId("US, GB"));
  }

  /**
   * Tests the behaviour when the region source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegionSource2() {
    WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(null, HOLIDAY_SOURCE, ExternalSchemes.countryRegionId(Country.US), Currency.USD);
  }

  /**
   * Tests the behaviour when the holiday source is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullHolidaySource2() {
    WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(REGION_SOURCE, null, ExternalSchemes.countryRegionId(Country.GB), Currency.GBP);
  }

  /**
   * Tests the behaviour when the region id is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegionId2() {
    WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(REGION_SOURCE, HOLIDAY_SOURCE, null, Currency.USD);
  }

  /**
   * Tests the behaviour when the currency is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(REGION_SOURCE, HOLIDAY_SOURCE, ExternalSchemes.countryRegionId(Country.US), null);
  }

  /**
   * Tests the behaviour when there is no holiday for a region.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoRegionHoliday() {
    WorkingDayCalendarUtils.getCalendarForRegion(REGION_SOURCE, HOLIDAY_SOURCE, ExternalSchemes.countryRegionId(Country.AR));
  }

  /**
   * Tests the behaviour when there is no holiday for a region or currency.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoRegionOrCurrencyHoliday() {
    WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(REGION_SOURCE, HOLIDAY_SOURCE, ExternalSchemes.countryRegionId(Country.EU), Currency.EUR);
  }

  /**
   * Tests that the region id separation logic.
   */
  @Test
  public void testRegionSeparator() {
    assertNull(WorkingDayCalendarUtils.getMultipleRegionSeparator(ExternalSchemes.countryRegionId(Country.US)));
    assertEquals(WorkingDayCalendarUtils.getMultipleRegionSeparator(ExternalSchemes.financialRegionId("US+GB")), "\\+");
    assertEquals(WorkingDayCalendarUtils.getMultipleRegionSeparator(ExternalId.of(ExternalSchemes.FINANCIAL, "US,GB")), ",");
    assertNull(WorkingDayCalendarUtils.getMultipleRegionSeparator(ExternalSchemes.isdaHoliday("US")));
  }

  /**
   * Tests that the correct calendar is returned for a region.
   */
  @Test
  public void testCalendarForRegion() {
    final WorkingDayCalendar calendar =
        WorkingDayCalendarUtils.getCalendarForRegion(REGION_SOURCE, HOLIDAY_SOURCE, ExternalSchemes.countryRegionId(Country.US));
    LocalDate date = LocalDate.of(2015, 1, 1);
    while (date.isBefore(LocalDate.of(2016, 1, 1))) {
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        assertTrue(calendar.isWeekend(date));
        assertFalse(calendar.isWorkingDay(date));
      } else if (HOLIDAY_DATES.contains(date)) {
        assertTrue(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
      }
      date = date.plusDays(1);
    }
  }

  /**
   * Tests that the correct calendar is returned for a region or currency if there is no holiday for that region id.
   */
  @Test
  public void testCalendarForRegionOrCurrency() {
    WorkingDayCalendar calendar =
        WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(REGION_SOURCE, HOLIDAY_SOURCE, ExternalSchemes.countryRegionId(Country.US), Currency.USD);
    LocalDate date = LocalDate.of(2015, 1, 1);
    while (date.isBefore(LocalDate.of(2016, 1, 1))) {
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        assertTrue(calendar.isWeekend(date));
        assertFalse(calendar.isWorkingDay(date));
      } else if (HOLIDAY_DATES.contains(date)) {
        assertTrue(calendar.isHoliday(date));
        assertFalse(calendar.isWorkingDay(date));
      } else {
        assertTrue(calendar.isWorkingDay(date));
      }
      date = date.plusDays(1);
    }
    calendar = WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(REGION_SOURCE, HOLIDAY_SOURCE, ExternalSchemes.countryRegionId(Country.GB), Currency.GBP);
    date = LocalDate.of(2015, 1, 1);
    while (date.isBefore(LocalDate.of(2016, 1, 1))) {
      if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
        assertTrue(calendar.isWeekend(date));
        assertFalse(calendar.isWorkingDay(date));
      } else {
        assertTrue(calendar.isWorkingDay(date));
      }
      date = date.plusDays(1);
    }
  }
}
