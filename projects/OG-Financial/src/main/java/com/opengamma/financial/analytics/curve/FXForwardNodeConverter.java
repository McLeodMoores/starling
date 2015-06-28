/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.date.FxSettlementDayCalculator;
import com.opengamma.analytics.date.FxWorkingDayCalendar;
import com.opengamma.analytics.date.LatAmFxSettlementDayCalculator;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.calendar.HolidaySourceWorkingDayCalendarAdapter;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Convert a {@link FXForwardNode} node into a {@link ForexDefinition}. This converter has been changed to
 * use FX settlement rules, although backwards compatibility has been maintained.
 * If {@link FXSpotConvention#getUseIntermediateUsHolidays()} is not null, it is assumed that the FX
 * settlement rules are to be used to calculate the delivery date. Otherwise, the previous calculation
 * method is used.
 * <p>
 * The FX settlement rule calculation proceeds as follows:
 * <ul>
 *  <li> The tenor of the node is added to the valuation time.
 *  <li> T+n settlement must include n good business days, where good business days are defined as dates that are neither
 *       holidays nor weekends in either of the currencies in the pair, unless one of those currencies is USD, in which case
 *       USD holidays are ignored, unless one of the currencies is a special Latin American currency, in which case USD holidays
 *       are considered.
 *  <li> The delivery date cannot be a holiday in either of the currencies or USD. The date is moved forwards until this
 *       is true.
 * </ul>
 * <p>
 * Otherwise, the delivery date of the forward is calculated by:
 * <ul>
 *   <li> The spot date is computed from the valuation date by adding the number of settlement days
 *   <li> The delivery date is computed from the spot date adding the maturity tenor of the node and using the business-day-convention,
 *        calendar and EOM of the convention to calculate the delivery date
 * </ul>
 * In both cases, the forward amount in the pay currency is 1 and in the receive currency -quote (e.g. - (spot+forward points)).
 */
public class FXForwardNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;
  /** The forward value */
  private final Double _forward;

  /**
   * @param conventionSource  the convention source, not null
   * @param holidaySource  the holiday source, not null
   * @param regionSource  the region source, not null
   * @param marketData  the market data, not null
   * @param dataId  the id of the market data, not null
   * @param valuationTime  the valuation time, not null
   */
  public FXForwardNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(regionSource, "regionSource");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.notNull(dataId, "dataId");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _valuationTime = valuationTime;
    _forward = marketData.getDataPoint(dataId);
    ArgumentChecker.notNull(_forward, "forward");
  }

  @SuppressWarnings("deprecation")
  @Override
  public InstrumentDefinition<?> visitFXForwardNode(final FXForwardNode fxForward) {
    ArgumentChecker.notNull(fxForward, "fxForward");
    final ExternalId conventionId = fxForward.getFxForwardConvention();
    final FXForwardAndSwapConvention forwardConvention = _conventionSource.getSingle(conventionId, FXForwardAndSwapConvention.class);
    final ExternalId underlyingConventionId = forwardConvention.getSpotConvention();
    final FXSpotConvention spotConvention = _conventionSource.getSingle(underlyingConventionId, FXSpotConvention.class);
    final Currency payCurrency = fxForward.getPayCurrency();
    final Currency receiveCurrency = fxForward.getReceiveCurrency();
    final double payAmount = 1;
    final int settlementDays = spotConvention.getSettlementDays();
    final ZonedDateTime exchangeDate;
    // TODO start tenor isn't needed in the node
    if (spotConvention.getUseIntermediateUsHolidays() != null) {
      final WorkingDayCalendar usCalendar = getCalendarForRegionOrCurrency();
      final Map<Currency, WorkingDayCalendar> calendars = new HashMap<>();
      // TODO see comment on getCalendarForRegionOrCurrency
      calendars.put(payCurrency, new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, payCurrency));
      calendars.put(receiveCurrency, new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, receiveCurrency));
      calendars.put(Currency.USD, usCalendar);
      final FxWorkingDayCalendar perCurrencyCalendars = new FxWorkingDayCalendar(payCurrency.toString() + "/" + receiveCurrency.toString(), calendars);
      // find forward date
      final ZonedDateTime forwardDate = TenorUtils.adjustDateByTenor(_valuationTime, fxForward.getMaturityTenor());
      final LocalDate localSettlementDate;
      if (spotConvention.getUseIntermediateUsHolidays()) {
        localSettlementDate = LatAmFxSettlementDayCalculator.getInstance().getSettlementDate(forwardDate.toLocalDate(), settlementDays, perCurrencyCalendars);
      } else {
        localSettlementDate = FxSettlementDayCalculator.getInstance().getSettlementDate(forwardDate.toLocalDate(), settlementDays, perCurrencyCalendars);
      }
      exchangeDate = ZonedDateTime.of(LocalDateTime.of(localSettlementDate, forwardDate.toLocalTime()), forwardDate.getZone());
    } else {
      // backwards compatibility - ignore any US holidays on the settlement date and use the settlement region only
      if (spotConvention.getSettlementRegion() == null) {
        throw new OpenGammaRuntimeException("Could not get settlement region from " + spotConvention.getSettlementRegion());
      }
      final Calendar settlementCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, forwardConvention.getSettlementRegion());
      final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(_valuationTime, settlementDays, settlementCalendar);
      exchangeDate = ScheduleCalculator.getAdjustedDate(spotDate, fxForward.getMaturityTenor(), forwardConvention.getBusinessDayConvention(),
          settlementCalendar, forwardConvention.isIsEOM());
    }
    return ForexDefinition.fromAmounts(payCurrency, receiveCurrency, exchangeDate, payAmount, -_forward);
  }

  /**
   * Gets the calendar by checking the source first for a holiday for the appropriate region, then the currency.
   * @return  the calendar
   */
  //TODO this method should be used for both FX currencies as well as US, but need to find how to get a region for a currency
  private WorkingDayCalendar getCalendarForRegionOrCurrency() {
    WorkingDayCalendar calendar = null;
    try {
      final Region region = _regionSource.getHighestLevelRegion(ExternalSchemes.countryRegionId(Country.US));
      if (region != null) {
        final Collection<Holiday> usHolidays = _holidaySource.get(HolidayType.BANK, ExternalSchemes.countryRegionId(Country.US).toBundle());
        if (usHolidays != null && !usHolidays.isEmpty()) {
          calendar = new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, region);
        }
      }
    } catch (final Exception e) {
      // source might throw exception rather than returning null or empty collection of region or holiday
      final Collection<Holiday> usHolidays = _holidaySource.get(Currency.USD);
      if (usHolidays == null || usHolidays.isEmpty()) {
        throw new OpenGammaRuntimeException("Could not get US holidays from source");
      }
      calendar = new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, Currency.USD);
    }
    if (calendar == null) {
      final Collection<Holiday> usHolidays = _holidaySource.get(Currency.USD);
      if (usHolidays == null || usHolidays.isEmpty()) {
        throw new OpenGammaRuntimeException("Could not get US holidays from source");
      }
      calendar = new HolidaySourceWorkingDayCalendarAdapter(_holidaySource, Currency.USD);
    }
    return calendar;
  }

}
