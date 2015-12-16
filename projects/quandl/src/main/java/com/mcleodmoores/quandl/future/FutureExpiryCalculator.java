/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import java.util.Map;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Month;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.google.common.collect.ImmutableMap;
import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.mcleodmoores.quandl.convention.QuandlFinancialConventionVisitorAdapter;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Calculates the expiry of a future given a month code, the day of the week and the nth day.
 */
//TODO #11
public class FutureExpiryCalculator extends QuandlFinancialConventionVisitorAdapter<Function2<Character, Integer, Expiry>> {
  /** Weekends */
  private static final Calendar WEEKENDS = new MondayToFridayCalendar("Weekend");
  /** A map from month codes to months. */
  /* package */ static final Map<Character, Month> MONTH_CODES = ImmutableMap.<Character, Month>builder()
      .put('F', Month.JANUARY)
      .put('G', Month.FEBRUARY)
      .put('H', Month.MARCH)
      .put('J', Month.APRIL)
      .put('K', Month.MAY)
      .put('M', Month.JUNE)
      .put('N', Month.JULY)
      .put('Q', Month.AUGUST)
      .put('U', Month.SEPTEMBER)
      .put('V', Month.OCTOBER)
      .put('X', Month.NOVEMBER)
      .put('Z', Month.DECEMBER)
      .build();
  /** A holiday source */
  private final HolidaySource _holidaySource;
  /** A region source */
  private final RegionSource _regionSource;

  /**
   * Creates an instance that does not use a holiday source. In this case, the only holidays that will be taken into
   * account are weekends.
   */
  public FutureExpiryCalculator() {
    _holidaySource = null;
    _regionSource = null;
  }
  /**
   * Creates an instance.
   * @param holidaySource  the holiday source, not null
   * @param regionSource  the region source, not null
   */
  public FutureExpiryCalculator(final HolidaySource holidaySource, final RegionSource regionSource) {
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
  }

  @Override
  public Function2<Character, Integer, Expiry> visitQuandlStirFutureConvention(final QuandlStirFutureConvention convention) {
    ArgumentChecker.notNull(convention, "convention");
    final int nthDay = convention.getNthDay();
    final DayOfWeek dayOfWeek = DayOfWeek.valueOf(convention.getDayOfWeek());
    final TemporalAdjuster dateAdjuster = TemporalAdjusters.dayOfWeekInMonth(nthDay, dayOfWeek);
    final LocalTime expiryTime = LocalTime.parse(convention.getLastTradeTime());
    final Calendar calendar;
    if (_holidaySource == null) {
      calendar = WEEKENDS;
    } else {
      final Region exchangeRegion = _regionSource.getHighestLevelRegion(convention.getTradingExchangeCalendarId());
      calendar = _holidaySource == null ? WEEKENDS : new HolidaySourceCalendarAdapter(_holidaySource, exchangeRegion);
    }
    final ZoneId timeZone = ZoneId.of(convention.getZoneOffsetId());
    return new Function2<Character, Integer, Expiry>() {

      @Override
      public Expiry apply(final Character monthCode, final Integer year) {
        ArgumentChecker.notNull(year, "year");
        final Month month = MONTH_CODES.get(monthCode);
        ArgumentChecker.notNull(month, "month");
        final LocalDate firstDateOfMonth = LocalDate.of(year, month, 1);
        LocalDate nthDayOfMonth = firstDateOfMonth.with(dateAdjuster);
        while (!calendar.isWorkingDay(nthDayOfMonth)) {
          nthDayOfMonth = nthDayOfMonth.plusDays(1);
        }
        return new Expiry(ZonedDateTime.of(LocalDateTime.of(nthDayOfMonth, expiryTime), timeZone), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
      }

    };
  }

  @Override
  public final Function2<Character, Integer, Expiry> visitQuandlFedFundsFutureConvention(final QuandlFedFundsFutureConvention convention) {
    ArgumentChecker.notNull(convention, "convention");
    final TemporalAdjuster dateAdjuster = TemporalAdjusters.lastDayOfMonth();
    final LocalTime expiryTime = LocalTime.parse(convention.getLastTradeTime());
    final ZoneId timeZone = ZoneId.of(convention.getZoneOffsetId());
    final Calendar calendar;
    if (_holidaySource == null) {
      calendar = WEEKENDS;
    } else {
      final Region exchangeRegion = _regionSource.getHighestLevelRegion(convention.getTradingExchangeCalendarId());
      calendar = _holidaySource == null ? WEEKENDS : new HolidaySourceCalendarAdapter(_holidaySource, exchangeRegion);
    }
    return new Function2<Character, Integer, Expiry>() {

      @Override
      public Expiry apply(final Character monthCode, final Integer year) {
        ArgumentChecker.notNull(year, "year");
        final Month month = MONTH_CODES.get(monthCode);
        ArgumentChecker.notNull(month, "month");
        final LocalDate firstDateOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDateOfMonth.with(dateAdjuster);
        while (!calendar.isWorkingDay(lastDayOfMonth)) {
          lastDayOfMonth = lastDayOfMonth.minusDays(1);
        }
        return new Expiry(ZonedDateTime.of(LocalDateTime.of(lastDayOfMonth, expiryTime), timeZone), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
      }

    };
  }

}
