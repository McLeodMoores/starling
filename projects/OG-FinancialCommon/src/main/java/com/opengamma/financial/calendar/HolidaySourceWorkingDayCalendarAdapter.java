/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.calendar;

import java.util.Collection;

import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.WeekendTypeProvider;
import com.opengamma.core.region.Region;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * An adapter that allows holiday information from a {@link HolidaySource} to be used in a {@link WorkingDayCalendar}.
 * <p>
 * This class currently only supports {@link Holiday}s of the types:
 * <ul>
 *  <li> {@link HolidayType#BANK} for a {@link Region}
 *  <li> {@link HolidayType#CURRENCY} for a {@link Currency}
 * </ul>
 * If a holiday is requested that is not available from the source, then an exception is thrown.
 */
public class HolidaySourceWorkingDayCalendarAdapter implements WorkingDayCalendar {
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The holiday region */
  private final Region _region;
  /** The holiday currency */
  private final Currency _currency;
  /** The holiday type */
  private final HolidayType _type;

  /**
   * Creates an adapter for bank holidays referenced by a region.
   * @param holidaySource  the holiday source, not null
   * @param region  the region, not null
   */
  public HolidaySourceWorkingDayCalendarAdapter(final HolidaySource holidaySource, final Region region) {
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _region = ArgumentChecker.notNull(region, "region");
    _currency = null;
    _type = HolidayType.BANK;
  }

  /**
   * Creates an adapter for currency holidays referenced by a currency.
   * @param holidaySource  the holiday source, not null
   * @param currency  the currency, not null
   */
  public HolidaySourceWorkingDayCalendarAdapter(final HolidaySource holidaySource, final Currency currency) {
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _currency = ArgumentChecker.notNull(currency, "currency");
    _region = null;
    _type = HolidayType.CURRENCY;
  }

  @Override
  public boolean isWorkingDay(final LocalDate date) {
    switch (_type) {
      case BANK: {
        Collection<Holiday> holidays = _holidaySource.get(HolidayType.BANK, _region.getExternalIdBundle());
        if (holidays.isEmpty()) {
          // see if there is a holiday for the currency
          final Currency currency = _region.getCurrency();
          if (currency != null) {
            holidays = _holidaySource.get(currency);
          }
          if (holidays.isEmpty()) {
            throw new DataNotFoundException("Could not get holiday for " + _region.getExternalIdBundle());
          }
        }
        for (final Holiday holiday : holidays) {
          if (holiday instanceof WeekendTypeProvider) {
            if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(date)) {
              return false;
            }
            if (holiday.getHolidayDates().contains(date)) {
              return false;
            }
          } else {
            // backwards compatibility for source, where weekends were hard-coded and not stored in the Holiday
            // object
            if (WeekendType.SATURDAY_SUNDAY.isWeekend(date)) {
              return false;
            }
            if (holiday.getHolidayDates().contains(date)) {
              return false;
            }
          }
        }
        return true;
      }
      case CURRENCY: {
        final Collection<Holiday> holidays = _holidaySource.get(_currency);
        if (holidays.isEmpty()) {
          throw new DataNotFoundException("Could not get holiday for " + _currency);
        }
        for (final Holiday holiday : holidays) {
          if (holiday instanceof WeekendTypeProvider) {
            if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(date)) {
              return false;
            }
            if (holiday.getHolidayDates().contains(date)) {
              return false;
            }
          } else {
            // backwards compatibility for source, where weekends were hard-coded and not stored in the Holiday
            // object
            if (WeekendType.SATURDAY_SUNDAY.isWeekend(date)) {
              return false;
            }
            if (holiday.getHolidayDates().contains(date)) {
              return false;
            }
          }
        }
        return true;
      }
      default:
        throw new OpenGammaRuntimeException("Unhandled holiday type " + _type);
    }
  }

  @Override
  public boolean isHoliday(final LocalDate date) {
    switch (_type) {
      case BANK: {
        Collection<Holiday> holidays = _holidaySource.get(HolidayType.BANK, _region.getExternalIdBundle());
        if (holidays.isEmpty()) {
          // see if there is a holiday for the currency
          final Currency currency = _region.getCurrency();
          if (currency != null) {
            holidays = _holidaySource.get(currency);
          }
          if (holidays.isEmpty()) {
            throw new DataNotFoundException("Could not get holiday for " + _region.getExternalIdBundle());
          }
        }
        for (final Holiday holiday : holidays) {
          if (holiday instanceof WeekendTypeProvider) {
            if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(date)) {
              return false;
            }
            if (holiday.getHolidayDates().contains(date)) {
              return true;
            }
          } else {
            // backwards compatibility for source, where weekends were hard-coded and not stored in the Holiday
            // object
            if (WeekendType.SATURDAY_SUNDAY.isWeekend(date)) {
              return false;
            }
            if (holiday.getHolidayDates().contains(date)) {
              return true;
            }
          }
        }
        return false;
      }
      case CURRENCY: {
        final Collection<Holiday> holidays = _holidaySource.get(_currency);
        if (holidays.isEmpty()) {
          throw new DataNotFoundException("Could not get holiday for " + _currency);
        }
        for (final Holiday holiday : holidays) {
          if (holiday instanceof WeekendTypeProvider) {
            if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(date)) {
              return false;
            }
            if (holiday.getHolidayDates().contains(date)) {
              return true;
            }
          } else {
            // backwards compatibility for source, where weekends were hard-coded and not stored in the Holiday
            // object
            if (WeekendType.SATURDAY_SUNDAY.isWeekend(date)) {
              return false;
            }
            if (holiday.getHolidayDates().contains(date)) {
              return true;
            }
          }
        }
        return false;
      }
      default:
        throw new OpenGammaRuntimeException("Unhandled holiday type " + _type);
    }
  }

  @Override
  public boolean isWeekend(final LocalDate date) {
    switch (_type) {
      case BANK: {
          Collection<Holiday> holidays = _holidaySource.get(HolidayType.BANK, _region.getExternalIdBundle());
          if (holidays.isEmpty()) {
            // see if there is a holiday for the currency
            final Currency currency = _region.getCurrency();
            if (currency != null) {
              holidays = _holidaySource.get(currency);
            }
            if (holidays.isEmpty()) {
              throw new DataNotFoundException("Could not get holiday for " + _region.getExternalIdBundle());
            }
          }
          for (final Holiday holiday : holidays) {
            if (holiday instanceof WeekendTypeProvider) {
              if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(date)) {
                return true;
              }
            } else {
              // backwards compatibility for source, where weekends were hard-coded and not stored in the Holiday
              // object
              if (WeekendType.SATURDAY_SUNDAY.isWeekend(date)) {
                return true;
              }
            }
          }
          return false;
        }
      case CURRENCY: {
        final Collection<Holiday> holidays = _holidaySource.get(_currency);
        if (holidays.isEmpty()) {
          throw new DataNotFoundException("Could not get holiday for " + _currency);
        }
        for (final Holiday holiday : holidays) {
          if (holiday instanceof WeekendTypeProvider) {
            if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(date)) {
              return true;
            }
          } else {
            // backwards compatibility for source, where weekends were hard-coded and not stored in the Holiday
            // object
            if (WeekendType.SATURDAY_SUNDAY.isWeekend(date)) {
              return true;
            }
          }
        }
        return false;
      }
      default:
        throw new OpenGammaRuntimeException("Unhandled holiday type " + _type);
    }
  }

  @Override
  public String getName() {
    switch (_type) {
      case BANK: {
        final StringBuilder regionName = new StringBuilder(_region.getName());
        regionName.append(" Bank");
        return regionName.toString();
      }
      case CURRENCY: {
        final StringBuilder ccyName = new StringBuilder(_currency.getName());
        ccyName.append(" Currency");
        return ccyName.toString();
      }
      default: {
        throw new OpenGammaRuntimeException("Unhandled holiday type " + _type);
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("HolidaySourceWorkingDayCalendarAdapter[");
    sb.append(getName());
    sb.append("]");
    return sb.toString();
  }


}
