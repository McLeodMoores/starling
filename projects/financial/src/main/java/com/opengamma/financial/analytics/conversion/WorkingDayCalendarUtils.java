/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Collection;

import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.calendar.HolidaySourceWorkingDayCalendarAdapter;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Utility methods for retrieving {@link WorkingDayCalendar}s from a source.
 * <p>
 * There are methods that are analogous to those in {@link CalendarUtils}, <b>without</b> support for region identifiers
 * of the form "GB+US", as the simple union of holiday dates in often incorrect in calculating settlement or reset dates,
 * for example.
 */
public final class WorkingDayCalendarUtils {

  /**
   * Restricted constructor.
   */
  private WorkingDayCalendarUtils() {
  }

  /**
   * Gets a working day calendar for the highest-level region. This method does not support regions that have identifiers
   * formed from multiple regions (e.g. US+GB).
   * @param regionSource  the region source, not null
   * @param holidaySource  the holiday source, not null
   * @param regionId  the region identifier, not null
   * @return  a working day calendar that wraps the holiday source or throws an exception if no holidays are found
   */
  public static WorkingDayCalendar getCalendarForRegion(final RegionSource regionSource, final HolidaySource holidaySource,
      final ExternalId regionId) {
    ArgumentChecker.notNull(regionSource, "regionSource");
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(regionId, "regionId");
    final String separator = getMultipleRegionSeparator(regionId);
    if (separator != null) {
      throw new IllegalArgumentException("Cannot create a working day calendar from multiple regions: " + regionId);
    }
    final Region region = regionSource.getHighestLevelRegion(regionId);
    if (region != null) {
      final Collection<Holiday> holidays = holidaySource.get(HolidayType.BANK, regionId.toBundle());
      if (holidays != null && !holidays.isEmpty()) {
        return new HolidaySourceWorkingDayCalendarAdapter(holidaySource, region);
      }
    }
    throw new OpenGammaRuntimeException("Could not get " + regionId + " holidays from source");
  }

  /**
   * Returns the escaped separator character for parsing multiple regions.
   * @param regionId  the region id to parse.
   * @return  the escaped separator character
   */
  public static String getMultipleRegionSeparator(final ExternalId regionId) {
    if (!(regionId.isScheme(ExternalSchemes.FINANCIAL) || regionId.isScheme(ExternalSchemes.ISDA_HOLIDAY))) {
      return null;
    }
    final String regions = regionId.getValue();
    if (regions.contains("+")) {
      return "\\+";
    } else if (regions.contains(",")) {
      return ",";
    }
    return null;
  }

  /**
   * Gets the calendar by checking the source first for a holiday for the appropriate highest-level region, then the currency.
   * @param regionSource  the region source, not null
   * @param holidaySource  the holiday source, not null
   * @param regionId  the region identifier, not null
   * @param currency  the currency, not null
   * @return  a working day calendar that wraps the holiday source, or throws an exception if holidays are found
   */
  //TODO this method is a bit awkward - would be nice to be able to get a currency for a region and then split this method into two
  public static WorkingDayCalendar getCalendarForRegionOrCurrency(final RegionSource regionSource, final HolidaySource holidaySource, final ExternalId regionId,
      final Currency currency) {
    ArgumentChecker.notNull(regionSource, "regionSource");
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(regionId, "regionId");
    ArgumentChecker.notNull(currency, "currency");
    WorkingDayCalendar calendar = null;
    try {
      calendar = getCalendarForRegion(regionSource, holidaySource, regionId);
    } catch (final Exception e) {
      // source might throw exception rather than returning null or empty collection of region or holiday
      final Collection<Holiday> holidays = holidaySource.get(currency);
      if (holidays == null || holidays.isEmpty()) {
        throw new OpenGammaRuntimeException("Could not get " + regionId + " or " + currency + " holidays from source");
      }
      calendar = new HolidaySourceWorkingDayCalendarAdapter(holidaySource, currency);
    }
    if (calendar == null) {
      final Collection<Holiday> holidays = holidaySource.get(currency);
      if (holidays == null || holidays.isEmpty()) {
        throw new OpenGammaRuntimeException("Could not get " + regionId + " or " + currency + " holidays from source");
      }
      calendar = new HolidaySourceWorkingDayCalendarAdapter(holidaySource, currency);
    }
    return calendar;
  }

}
