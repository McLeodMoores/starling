/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IborTypeIndex;
import com.opengamma.analytics.financial.instrument.index.IndexConverter;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class IborDepositConvention implements CurveDataConvention<DepositIborDefinition> {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private WorkingDayCalendar _calendar;
    private IborTypeIndex _index;

    /**
     * Constructs the builder.
     */
    /* package */Builder() {
    }

    public Builder withIborIndex(final IborTypeIndex index) {
      _index = index;
      return this;
    }

    public Builder withCalendar(final WorkingDayCalendar calendar) {
      _calendar = calendar;
      return this;
    }

    /**
     * Builds the convention.
     * @return  the convention
     */
    public IborDepositConvention build() {
      if (_index == null) {
        throw new IllegalStateException("The ibor index must be supplied");
      }
      if (_calendar == null) {
        throw new IllegalStateException("The calendar must be supplied");
      }
      return new IborDepositConvention(_index, _calendar);
    }
  }

  private final IborIndex _index;
  private final Tenor _tenor;
  private final WorkingDayCalendar _calendar;

  /* package */ IborDepositConvention(final IborTypeIndex index, final WorkingDayCalendar calendar) {
    _index = IndexConverter.toIborIndex(index);
    _calendar = calendar;
    _tenor = index.getTenor();
  }

  @Override
  public DepositIborDefinition toCurveInstrument(final ZonedDateTime date, final Tenor startTenor, final Tenor endTenor, final double notional,
      final double fixedRate) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(startTenor, "startTenor");
    ArgumentChecker.notNull(endTenor, "endTenor");
    final Currency currency = _index.getCurrency();
    final int spotLag = _index.getSpotLag();
    final DayCount dayCount = _index.getDayCount();
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(date, spotLag, _calendar);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, _tenor, _calendar);
    final double accrualFactor = dayCount.getDayCountFraction(startDate, endDate, new CalendarAdapter(_calendar));
    return new DepositIborDefinition(currency, startDate, endDate, notional, fixedRate, accrualFactor, _index);
  }
}