/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday;

import java.util.Collection;

import org.threeten.bp.LocalDate;

import com.opengamma.core.Source;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * A source of holiday information as accessed by the main application.
 * <p>
 * This interface provides a simple view of holidays as used by most parts of the application.
 * This may be backed by a full-featured holiday master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 * <p>
 * The original implementation has methods that are used to determine whether a date is a holiday,
 * with some of the implementations hard-coding the weekend days to be Saturday or Sunday. Although
 * these implementations have been retained, the correct way to determine whether a date is a holiday
 * or weekend is to use implementations of {@link Holiday} that are {@link WeekendTypeProvider}s and
 * use a {@link WorkingDayCalculator}.
 */
@PublicSPI
public interface HolidaySource extends Source<Holiday> {

  /**
   * Returns the collection of holiday records for a particular
   * holiday type and region/exchange. Note that when attempting
   * to determine whether a particular date is a holiday for a
   * region/exchange, a weekend check should be done before
   * calling this method.
   *
   * @param holidayType  the type of holiday, must not be CURRENCY, not null
   * @param regionOrExchangeIds  the regions or exchanges to check, not null
   * @return the collection of holiday records, not null
   */
  Collection<Holiday> get(HolidayType holidayType,
                          ExternalIdBundle regionOrExchangeIds);

  /**
   * Returns the collection of holiday records for a particular
   * currency. Note that when attempting to determine whether a
   * particular date is a holiday for a currency, a weekend check
   * should be done before calling this method.
   *
   * @param currency  the currency to check, not null
   * @return the collection of holiday records, not null
   */
  Collection<Holiday> get(Currency currency);

  /**
   * Checks if a date is a holiday for a CURRENCY type.
   *
   * @param dateToCheck the date to check, not null
   * @param currency  the currency to check, not null
   * @return true if it is a holiday
   * @deprecated  Use a {@link WorkingDayCalculator}
   */
  @Deprecated
  boolean isHoliday(LocalDate dateToCheck, Currency currency);

  /**
   * Checks if a date is a holiday for a BANK, SETTLEMENT or TRADING type.
   *
   * @param dateToCheck the date to check, not null
   * @param holidayType  the type of holiday, must not be CURRENCY, not null
   * @param regionOrExchangeIds  the regions or exchanges to check, not null
   * @return true if it is a holiday
   * @deprecated  Use a {@link WorkingDayCalculator}
   */
  @Deprecated
  boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds);

  /**
   * Checks if a date is a holiday for a BANK, SETTLEMENT or TRADING type.
   *
   * @param dateToCheck the date to check, not null
   * @param holidayType  the type of holiday, must not be CURRENCY, not null
   * @param regionOrExchangeId  the region or exchange to check, not null
   * @return true if it is a holiday
   * @deprecated Use a {@link WorkingDayCalculator}
   */
  @Deprecated
  boolean isHoliday(LocalDate dateToCheck, HolidayType holidayType, ExternalId regionOrExchangeId);

}
