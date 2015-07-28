/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.holiday.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendTypeProvider;
import com.opengamma.core.holiday.impl.SimpleHoliday;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * A {@code HolidaySource} implemented using an underlying {@code HolidayMaster}.
 * <p>
 * The {@link HolidaySource} interface provides holidays to the application via a narrow API. This class provides the source on
 * top of a standard {@link HolidayMaster}.
 * <p>
 * Previously, this source hard-coded the weekend to be Saturday and Sunday. Now the holiday is checked and if it is a
 * {@link WeekendTypeProvider}, the provider is used to determine if a day is a weekend. If this information is not available
 * from a holiday, the previous hard-coding is used.
 */
@PublicSPI
public class MasterHolidaySource extends AbstractMasterSource<Holiday, HolidayDocument, HolidayMaster> implements HolidaySource {
  /** True if the holiday calendars should be cached */
  private final boolean _cacheHolidayCalendars;
  /** The cached holiday objects */
  private final ConcurrentMap<HolidaySearchRequest, Holiday> _cachedHolidays = new ConcurrentHashMap<>();

  /**
   * Creates an instance with an underlying master.
   *
   * @param master the master, not null
   */
  public MasterHolidaySource(final HolidayMaster master) {
    this(master, false);
  }

  /**
   * Creates an instance with an underlying master.
   *
   * @param master the master, not null
   * @param cacheCalendars whether all calendars should be cached
   */
  public MasterHolidaySource(final HolidayMaster master, final boolean cacheCalendars) {
    super(master);
    _cacheHolidayCalendars = cacheCalendars;
  }

  @Override
  public Collection<Holiday> get(final HolidayType holidayType,
                                 final ExternalIdBundle regionOrExchangeIds) {
    final HolidaySearchRequest request = createNonCurrencySearchRequest(holidayType, regionOrExchangeIds);
    return processDocuments(getMaster().search(request));
  }

  @Override
  public Collection<Holiday> get(final Currency currency) {
    final HolidaySearchRequest request = createCurrencySearchRequest(currency);
    return processDocuments(getMaster().search(request));
  }

  private Collection<Holiday> processDocuments(final HolidaySearchResult search) {
    return ImmutableList.<Holiday>copyOf(search.getHolidays());
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    final HolidaySearchRequest request = createCurrencySearchRequest(currency);
    return isHoliday(request, dateToCheck);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    final HolidaySearchRequest request = createNonCurrencySearchRequest(holidayType, regionOrExchangeIds);
    return isHoliday(request, dateToCheck);
  }

  private VersionCorrection getVersionCorrection() {
    final ServiceContext serviceContext = ThreadLocalServiceContext.getInstance();
    return serviceContext.get(VersionCorrectionProvider.class).getConfigVersionCorrection();
  }

  private HolidaySearchRequest createCurrencySearchRequest(final Currency currency) {
    return createdVersionCorrectedSearchRequest(new HolidaySearchRequest(currency));
  }

  private HolidaySearchRequest createNonCurrencySearchRequest(final HolidayType holidayType,
                                                              final ExternalIdBundle regionOrExchangeIds) {
    return createdVersionCorrectedSearchRequest(new HolidaySearchRequest(holidayType, regionOrExchangeIds));
  }

  private HolidaySearchRequest createdVersionCorrectedSearchRequest(final HolidaySearchRequest searchRequest) {
    searchRequest.setVersionCorrection(getVersionCorrection());
    return searchRequest;
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    final HolidaySearchRequest request = new HolidaySearchRequest(holidayType, ExternalIdBundle.of(regionOrExchangeId));
    return isHoliday(request, dateToCheck);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the specified date is a holiday.
   *
   * @param request the request to search base on, not null
   * @param dateToCheck the date to check, not null
   * @return true if the date is a holiday
   */
  protected boolean isHoliday(final HolidaySearchRequest request, final LocalDate dateToCheck) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    final HolidaySearchRequest cacheKey = request.clone();
    if (_cacheHolidayCalendars) {
      final Holiday cachedHoliday = _cachedHolidays.get(cacheKey);
      if (cachedHoliday != null) {
        // note that the weekend check has been moved into this section rather than being short-circuited
        // above, as different countries can have different weekend days
        if (cachedHoliday instanceof WeekendTypeProvider) {
          if (((WeekendTypeProvider) cachedHoliday).getWeekendType().isWeekend(dateToCheck)) {
            return true;
          }
        } else {
          // backwards compatibility
          if (isWeekend(dateToCheck)) {
            return true;
          }
        }
        final List<LocalDate> cachedDates = cachedHoliday.getHolidayDates();
        if (cachedDates == null || cachedDates.isEmpty()) {
          // Sign that we couldn't find anything.
          return false;
        }
        return isHoliday(cachedDates, dateToCheck);
      }
    }
    final HolidayDocument doc;
    if (_cacheHolidayCalendars) {
      // get all holidays and cache
      final HolidaySearchResult result = getMaster().search(cacheKey);
      if (result == null) {
        doc = null;
      } else {
        doc = result.getFirstDocument();
      }
      if (doc != null) {
        _cachedHolidays.put(cacheKey, doc.getHoliday());
      } else {
        // preserves old behaviour, which cached an empty list of dates
        _cachedHolidays.put(cacheKey, new SimpleHoliday());
        return false;
      }
    } else {
      // Not caching, search for this date only.
      request.setDateToCheck(dateToCheck);
      final HolidaySearchResult result = getMaster().search(request);
      if (result != null) {
        doc = result.getFirstDocument();
      } else {
        doc = null;
      }
    }
    if (doc != null && doc.getHoliday() instanceof WeekendTypeProvider) {
      if (((WeekendTypeProvider) doc.getHoliday()).getWeekendType().isWeekend(dateToCheck)) {
        return true;
      }
    } else {
      // backwards compatibility
      if (isWeekend(dateToCheck)) {
        return true;
      }
    }
    return isHoliday(doc, dateToCheck);
  }

  /**
   * Checks if the specified date is a holiday.
   *
   * @param doc document retrieved from underlying holiday master, may be null
   * @param dateToCheck the date to check, not null
   * @return false if nothing was retrieved from underlying holiday master. Otherwise, true if and only if
   * the date is a holiday based on the underlying holiday master
   */
  protected boolean isHoliday(final HolidayDocument doc, final LocalDate dateToCheck) {
    if (doc == null) {
      return false;
    }
    return Collections.binarySearch(doc.getHoliday().getHolidayDates(), dateToCheck) >= 0;
  }

  /**
   * Returns true if a date is present in a list of holiday dates. Note that this method
   * does not include weekends.
   * @param dates  the dates, not null
   * @param dateToCheck  the date to check, not null
   * @return true if the date is present in the list of holiday dates
   */
  protected boolean isHoliday(final List<LocalDate> dates, final LocalDate dateToCheck) {
    return Collections.binarySearch(dates, dateToCheck) >= 0;
  }

  /**
   * Checks if the date is at the weekend, defined as a Saturday or Sunday.
   *
   * @param date the date to check, not null
   * @return true if it is a weekend
   * @deprecated The weekend dates should be returned from a {@link WeekendTypeProvider} object.
   */
  @Deprecated
  protected boolean isWeekend(final LocalDate date) {
    return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
  }

}
