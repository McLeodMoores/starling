/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * Utilities and constants for {@code Calendar}.
 * <p>
 * This is a thread-safe static utility class.
 * @deprecated  {@link Calendar} is deprecated in the analytics library, as it is not possible to distinguish between
 * weekends and holidays.
 */
@Deprecated
public class CalendarUtils {

  /**
   * Restricted constructor.
   */
  protected CalendarUtils() {
    super();
  }

  //-------------------------------------------------------------------------
  public static Calendar getCalendar(final RegionSource regionSource, final HolidaySource holidaySource,
      final ExternalId regionId) {
    final String separator = getMultipleRegionSeparator(regionId);
    if (separator != null) {
      final String[] regions = regionId.getValue().split(separator);
      final Set<Region> resultRegions = new HashSet<>();
      for (final String region : regions) {
        if (regionId.isScheme(ExternalSchemes.FINANCIAL)) {
          resultRegions.add(regionSource.getHighestLevelRegion(ExternalSchemes.financialRegionId(region)));
        } else if (regionId.isScheme(ExternalSchemes.ISDA_HOLIDAY)) {
          resultRegions.add(regionSource.getHighestLevelRegion(ExternalSchemes.isdaHoliday(region)));
        }
      }
      return new HolidaySourceCalendarAdapter(holidaySource, resultRegions.toArray(new Region[resultRegions.size()]));
    }
    final Region region = regionSource.getHighestLevelRegion(regionId); // we've checked that they are the same.
    return new HolidaySourceCalendarAdapter(holidaySource, region);
  }

  /**
   * Returns the escaped separator character for parsing multiple regions
   *
   * @param regionId the region id to parse.
   * @return the escaped separator charactor.
   */
  private static String getMultipleRegionSeparator(final ExternalId regionId) {
    if (!(regionId.isScheme(ExternalSchemes.FINANCIAL) || regionId.isScheme(ExternalSchemes.ISDA_HOLIDAY))) {
      return null;
    }

    final String regions = regionId.getValue();
    if (regions.contains("+")) {
      return "\\+";
    } else if (regions.contains(",")) {
      return ",";
    } else {
      return null;
    }
  }

  public static Calendar getCalendar(final HolidaySource holidaySource, final Currency... currencies) {
    return new HolidaySourceCalendarAdapter(holidaySource, currencies);
  }

}
