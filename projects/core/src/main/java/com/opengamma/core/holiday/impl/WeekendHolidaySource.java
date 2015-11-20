/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday.impl;

import java.util.Collection;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.AbstractSource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * Simple implementation of {@code HolidaySource} where the only holidays are weekends.
 * <p>
 * This is designed for testing.
 * @deprecated This source hard-codes weekend days to be Saturday and Sunday. This information
 * should be supplied from a {@link com.opengamma.core.holiday.WeekendTypeProvider}.
 */
@Deprecated
public class WeekendHolidaySource extends AbstractSource<Holiday> implements HolidaySource {

  @Override
  public Holiday get(final UniqueId uniqueId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Holiday> get(final HolidayType holidayType,
                                 final ExternalIdBundle regionOrExchangeIds) {
    return ImmutableSet.of();
  }

  @Override
  public Collection<Holiday> get(final Currency currency) {
    return ImmutableSet.of();
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    return isWeekend(dateToCheck);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
      final ExternalIdBundle regionOrExchangeIds) {
    return isWeekend(dateToCheck);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType,
      final ExternalId regionOrExchangeId) {
    return isWeekend(dateToCheck);
  }

  private boolean isWeekend(final LocalDate dateToCheck) {
    return dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;
  }

}
