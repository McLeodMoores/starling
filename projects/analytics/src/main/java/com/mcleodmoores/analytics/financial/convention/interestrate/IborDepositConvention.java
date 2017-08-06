/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import java.util.Objects;

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
 * A convention for IBOR rates that contains sufficient information to build a {@link DepositIborDefinition}
 * when the fixing rate and start and end tenors are supplied:
 * <ul>
 *   <li> A working day calendar that contains information about weekends and holidays ({@link WorkingDayCalendar}).
 *   <li> An underlying index ({@link IborTypeIndex}).
 * </ul>
 */
public class IborDepositConvention implements CurveDataConvention<DepositIborDefinition> {

  /**
   * Gets the convention builder.
   * @return  the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A builder for this convention.
   */
  public static class Builder {
    private WorkingDayCalendar _calendar;
    private IborTypeIndex _index;

    /**
     * Constructs the builder.
     */
    /* package */Builder() {
    }

    /**
     * Sets the calendar.
     * @param calendar  the calendar, not null
     * @return  the builder
     */
    public Builder withCalendar(final WorkingDayCalendar calendar) {
      _calendar = ArgumentChecker.notNull(calendar, "calendar");
      return this;
    }

    /**
     * Sets the index.
     * @param index  the index, not null
     * @return  the builder
     */
    public Builder withIborIndex(final IborTypeIndex index) {
      _index = ArgumentChecker.notNull(index, "index");
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

  /**
   * Constructs the convention.
   * @param index  the index
   * @param calendar  the calendar
   */
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_calendar == null ? 0 : _calendar.hashCode());
    result = prime * result + (_index == null ? 0 : _index.hashCode());
    result = prime * result + (_tenor == null ? 0 : _tenor.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final IborDepositConvention other = (IborDepositConvention) obj;
    if (!Objects.equals(_tenor, other._tenor)) {
      return false;
    }
    if (!Objects.equals(_index, other._index)) {
      return false;
    }
    if (!Objects.equals(_calendar, other._calendar)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("IborDepositConvention [index=");
    builder.append(_index);
    builder.append(", tenor=");
    builder.append(_tenor);
    builder.append(", calendar=");
    builder.append(_calendar);
    builder.append("]");
    return builder.toString();
  }


}