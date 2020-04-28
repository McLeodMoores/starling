/**
 * Copyright (C) 2020 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.generator.interestrate;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class FraGenerator implements CurveInstrumentGenerator<ForwardRateAgreementDefinition> {

  /**
   * Gets the generator builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A builder for this generator.
   */
  public static class Builder {
    private WorkingDayCalendar _calendar;
    private IborTypeIndex _index;

    /**
     * Constructs the builder.
     */
    Builder() {
    }

    /**
     * Sets the calendar.
     *
     * @param calendar
     *          the calendar, not null
     * @return the builder
     */
    public Builder withCalendar(final WorkingDayCalendar calendar) {
      _calendar = ArgumentChecker.notNull(calendar, "calendar");
      return this;
    }

    /**
     * Sets the index.
     *
     * @param index
     *          the index, not null
     * @return the builder
     */
    public Builder withIborIndex(final IborTypeIndex index) {
      _index = ArgumentChecker.notNull(index, "index");
      return this;
    }

    /**
     * Builds the generator.
     *
     * @return the generator
     */
    public FraGenerator build() {
      if (_index == null) {
        throw new IllegalStateException("The ibor index must be supplied");
      }
      if (_calendar == null) {
        throw new IllegalStateException("The calendar must be supplied");
      }
      return new FraGenerator(_index, _calendar);
    }
  }

  private final IborTypeIndex _index;
  private final WorkingDayCalendar _calendar;

  /**
   * Constructs the generator.
   *
   * @param index
   *          the index
   * @param calendar
   *          the calendar
   */
  FraGenerator(final IborTypeIndex index, final WorkingDayCalendar calendar) {
    _index = index;
    _calendar = calendar;
  }

  @Override
  public ForwardRateAgreementDefinition toCurveInstrument(final ZonedDateTime date, final Tenor startTenor, final Tenor endTenor, final double notional,
      final double fixedRate) {
    @SuppressWarnings("deprecation")
    final Calendar calendar = CalendarAdapter.of(_calendar);
    final Period startPeriod = TenorUtils.minus(endTenor, _index.getTenor()).getPeriod();
    return ForwardRateAgreementDefinition.fromTrade(date, startPeriod, notional, IndexConverter.toIborIndex(_index), fixedRate, calendar);
  }

}
