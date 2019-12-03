/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon.constantspread;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.horizon.HorizonCalculator;
import com.opengamma.analytics.financial.horizon.rolldown.CurveProviderConstantSpreadRolldown;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class SwapConstantSpreadHorizonCalculator
    extends HorizonCalculator<SwapDefinition, MulticurveProviderInterface, ZonedDateTimeDoubleTimeSeries[]> {
  /** Empty series */
  private static final ImmutableZonedDateTimeDoubleTimeSeries EMPTY_SERIES = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  /**
   * A static instance.
   */
  public static final HorizonCalculator<SwapDefinition, MulticurveProviderInterface, ZonedDateTimeDoubleTimeSeries[]> INSTANCE = new SwapConstantSpreadHorizonCalculator();

  private SwapConstantSpreadHorizonCalculator() {
  }

  @Override
  public MultipleCurrencyAmount getTheta(final SwapDefinition definition, final ZonedDateTime date, final MulticurveProviderInterface data,
      final int daysForward, final Calendar calendar) {
    return getTheta(definition, date, data, daysForward, calendar,
        new ZonedDateTimeDoubleTimeSeries[] { EMPTY_SERIES, EMPTY_SERIES, EMPTY_SERIES });
  }

  @Override
  public MultipleCurrencyAmount getTheta(final SwapDefinition definition, final ZonedDateTime date, final MulticurveProviderInterface data,
      final int daysForward, final Calendar calendar, final ZonedDateTimeDoubleTimeSeries[] fixingSeries) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(fixingSeries, "fixing series");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday;
    final InstrumentDerivative instrumentTomorrow;
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    if (calendar.isWorkingDay(date.toLocalDate())) {
      instrumentToday = definition.toDerivative(date, fixingSeries);
      final ZonedDateTimeDoubleTimeSeries[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, horizonDate);
      instrumentTomorrow = definition.toDerivative(horizonDate, shiftedFixingSeries);
    } else {
      final ZonedDateTime nextWorkingDay = ScheduleCalculator.getAdjustedDate(date, 1, calendar);
      final ZonedDateTime nextHorizonDate = nextWorkingDay.plusDays(daysForward);
      final ZonedDateTimeDoubleTimeSeries[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, nextHorizonDate);
      instrumentToday = definition.toDerivative(nextWorkingDay, fixingSeries);
      instrumentTomorrow = definition.toDerivative(nextHorizonDate, shiftedFixingSeries);
    }
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final MulticurveProviderInterface tomorrowData = (MulticurveProviderInterface) CurveProviderConstantSpreadRolldown.INSTANCE
        .rollDown(data, shiftTime);
    return subtract(instrumentTomorrow.accept(PresentValueDiscountingCalculator.getInstance(), tomorrowData),
        instrumentToday.accept(PresentValueDiscountingCalculator.getInstance(), data));
  }

  /**
   * Create a new time series with the same data up to tomorrow (tomorrow excluded) and with an extra data for tomorrow equal to the last
   * value in the time series.
   *
   * @param fixingSeries
   *          The time series.
   * @param tomorrow
   *          Tomorrow date.
   * @return The time series with added data.
   */
  private ZonedDateTimeDoubleTimeSeries[] getDateShiftedTimeSeries(final ZonedDateTimeDoubleTimeSeries[] fixingSeries,
      final ZonedDateTime tomorrow) {
    final int n = fixingSeries.length;
    final ZonedDateTimeDoubleTimeSeries[] laggedFixingSeries = new ZonedDateTimeDoubleTimeSeries[n];
    for (int i = 0; i < n; i++) {
      if (fixingSeries[i].isEmpty()) {
        laggedFixingSeries[i] = ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(tomorrow.getZone());
      } else {
        final ZonedDateTimeDoubleTimeSeries ts = fixingSeries[i].subSeries(fixingSeries[i].getEarliestTime(), tomorrow);
        if (ts == null || ts.isEmpty()) {
          laggedFixingSeries[i] = ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(tomorrow.getZone());
        } else {
          final ZonedDateTimeDoubleTimeSeries subSeries = fixingSeries[i].subSeries(fixingSeries[i].getEarliestTime(), tomorrow);
          final List<ZonedDateTime> times = new ArrayList<>(subSeries.times());
          final List<Double> values = new ArrayList<>(subSeries.values());
          times.add(tomorrow);
          values.add(ts.getLatestValue());
          laggedFixingSeries[i] = ImmutableZonedDateTimeDoubleTimeSeries.of(times, values, tomorrow.getZone());
        }
      }
    }
    return laggedFixingSeries;
  }
}
