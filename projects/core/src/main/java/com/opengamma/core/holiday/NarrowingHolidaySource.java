/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday;

import static org.threeten.bp.DayOfWeek.SATURDAY;
import static org.threeten.bp.DayOfWeek.SUNDAY;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A holiday source that delegates to an another source, but which ensures that
 * it only calls the get methods on the delegate. This is intended to allow
 * the use of proxy classes as the delegates which allows different
 * behaviours e.g. capturing the data returned from sources.
 * <p>
 * Previously, this source hard-coded the weekend to be Saturday and Sunday. Now the holiday is checked and if it is a
 * {@link WeekendTypeProvider}, the provider is used to determine if a day is a weekend. If this information is not available
 * from a holiday, the previous hard-coding is used.
 */
public class NarrowingHolidaySource implements HolidaySource {
  /** The delegate source */
  private final HolidaySource _delegate;

  /**
   * Create a narrowing source, wrapping the provided source.
   *
   * @param delegate the source to delegate to, not null
   */
  public NarrowingHolidaySource(final HolidaySource delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public Collection<Holiday> get(final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    return _delegate.get(holidayType, regionOrExchangeIds);
  }

  @Override
  public Collection<Holiday> get(final Currency currency) {
    return _delegate.get(currency);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    return isHoliday(dateToCheck, get(currency));
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    return isHoliday(dateToCheck, get(holidayType, regionOrExchangeIds));
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    return isHoliday(dateToCheck, holidayType, regionOrExchangeId.toBundle());
  }

  @Override
  public Holiday get(final UniqueId uniqueId) {
    return _delegate.get(uniqueId);
  }

  @Override
  public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return _delegate.get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, Holiday> get(final Collection<UniqueId> uniqueIds) {
    return _delegate.get(uniqueIds);
  }

  @Override
  public Map<ObjectId, Holiday> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    return _delegate.get(objectIds, versionCorrection);
  }

  /**
   * Checks that a date is not a weekend or holiday.
   * @param dateToCheck  the date to check
   * @param holidays  the holidays from the delegate source
   * @return true if the date is a holiday or weekend
   */
  private static boolean isHoliday(final LocalDate dateToCheck, final Collection<Holiday> holidays) {
    for (final Holiday holiday : holidays) {
      // if the holiday contains information about the weekend, check that first
      if (holiday instanceof WeekendTypeProvider) {
        if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(dateToCheck)) {
          return true;
        }
        if (Collections.binarySearch(holiday.getHolidayDates(), dateToCheck) >= 0) {
          return true;
        }
      }
      // otherwise, keep old behaviour of assuming isWeekend() provides the correct days, performing the quicker check first
      if (isWeekend(dateToCheck)) {
        return true;
      }
      if (Collections.binarySearch(holiday.getHolidayDates(), dateToCheck) >= 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the day is a Saturday or Sunday.
   * @param date  the date
   * @return  true if the day is a Saturday or Sunday
   * @deprecated This method is only used to account for the cases where the Holiday is not a WeekendProvider
   */
  @Deprecated
  private static boolean isWeekend(final LocalDate date) {
    return date.getDayOfWeek() == SATURDAY || date.getDayOfWeek() == SUNDAY;
  }

}
