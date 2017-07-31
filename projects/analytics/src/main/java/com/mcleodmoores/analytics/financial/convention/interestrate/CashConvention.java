/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import java.util.Objects;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * A convention for cash rates (e.g. deposit, IBOR, etc.) that contains sufficient information to
 * build a {@link CashDefinition} when the fixing rate and start and end tenors are supplied:
 * <ul>
 *  <li> A business day convention to adjust dates for non-working days ({@link BusinessDayConvention}).
 *  <li> A working day calendar that contains information about weekends and holidays ({@link WorkingDayCalendar}).
 *  <li> The currency for which this convention is used.
 *  <li> A day count convention that calculates the times between dates ({@link DayCount}).
 *  <li> An end-of-month convention that determines the behaviour if the instrument payment date falls on the last day of a month.
 *  <li> The spot lag i.e. the number of days to adjust the start date of the instrument to move to the spot date.
 * </ul>
 */
public class CashConvention implements CurveDataConvention<CashDefinition> {

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
    private BusinessDayConvention _businessDayConvention;
    private WorkingDayCalendar _calendar;
    private Currency _currency;
    private DayCount _dayCount;
    private EndOfMonthConvention _endOfMonth;
    private int _spotLag;

    /**
     * Constructs the builder.
     */
    /* package */Builder() {
    }

    /**
     * Sets the business day convention.
     * @param businessDayConvention  the business day convention, not null
     * @return  the builder
     */
    public Builder withBusinessDayConvention(final BusinessDayConvention businessDayConvention) {
      _businessDayConvention = ArgumentChecker.notNull(businessDayConvention, "businessDayConvention");
      return this;
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
     * Sets the currency.
     * @param currency  the currency, not null
     * @return  the builder
     */
    public Builder withCurrency(final Currency currency) {
      _currency = ArgumentChecker.notNull(currency, "currency");
      return this;
    }

    /**
     * Sets the day count.
     * @param dayCount  the day count, not null
     * @return  the builder
     */
    public Builder withDayCount(final DayCount dayCount) {
      _dayCount = ArgumentChecker.notNull(dayCount, "dayCount");
      return this;
    }

    /**
     * Sets the end of month convention.
     * @param isEom  the end of month convention, not null
     * @return  the builder
     */
    public Builder withEndOfMonthConvention(final EndOfMonthConvention isEom) {
      _endOfMonth = ArgumentChecker.notNull(isEom, "isEom");
      return this;
    }

    /**
     * Sets the spot lag.
     * @param spotLag  the spot lag
     * @return  the builder
     */
    public Builder withSpotLag(final int spotLag) {
      _spotLag = spotLag;
      return this;
    }

    /**
     * Builds the convention.
     * @return  the convention
     */
    public CashConvention build() {
      if (_businessDayConvention == null) {
        throw new IllegalStateException("The business day convention must be supplied");
      }
      if (_calendar == null) {
        throw new IllegalStateException("The calendar must be supplied");
      }
      if (_currency == null) {
        throw new IllegalStateException("The currency must be supplied");
      }
      if (_dayCount == null) {
        throw new IllegalStateException("The day count must be supplied");
      }
      if (_endOfMonth == null) {
        throw new IllegalStateException("The end of month convention must be supplied");
      }
      return new CashConvention(this);
    }

    /**
     * Gets the business day convention.
     * @return  the business day convention
     */
    /* package */ BusinessDayConvention getBusinessDayConvention() {
      return _businessDayConvention;
    }

    /**
     * Gets the calendar.
     * @return  the calendar
     */
    /* package */ WorkingDayCalendar getCalendar() {
      return _calendar;
    }

    /**
     * Gets the currency.
     * @return  the currency
     */
    /* package */ Currency getCurrency() {
      return _currency;
    }

    /**
     * Gets the day count.
     * @return  the day count
     */
    /* package */ DayCount getDayCount() {
      return _dayCount;
    }

    /**
     * Gets the end of month convention.
     * @return  the end of month convention
     */
    /* package */ EndOfMonthConvention getEndOfMonthConvention() {
      return _endOfMonth;
    }

    /**
     * Gets the spot lag.
     * @return  the spot lag
     */
    /* package */ int getSpotLag() {
      return _spotLag;
    }
  }

  private final Currency _currency;
  private final WorkingDayCalendar _calendar;
  private final int _spotLag;
  private final DayCount _dayCount;
  private final BusinessDayConvention _businessDayConvention;
  private final boolean _endOfMonth;

  /**
   * Constructs the convention.
   * @param builder  the builder
   */
  /* package */CashConvention(final Builder builder) {
    _currency = builder.getCurrency();
    _calendar = builder.getCalendar();
    _spotLag = builder.getSpotLag();
    _dayCount = builder.getDayCount();
    _businessDayConvention = builder.getBusinessDayConvention();
    if (builder.getEndOfMonthConvention() != null) {
      switch (builder.getEndOfMonthConvention()) {
        case ADJUST_FOR_END_OF_MONTH:
          _endOfMonth = true;
          break;
        case IGNORE_END_OF_MONTH:
          _endOfMonth = false;
          break;
        default:
          throw new IllegalArgumentException("Unsupported end of month convention " + builder.getEndOfMonthConvention());
      }
    } else {
      _endOfMonth = true;
    }
  }

  @Override
  public CashDefinition toCurveInstrument(final ZonedDateTime date, final Tenor startTenor, final Tenor endTenor, final double notional,
      final double fixedRate) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(startTenor, "startTenor");
    ArgumentChecker.notNull(endTenor, "endTenor");
    final ZonedDateTime startDate, endDate;
    if (endTenor.isBusinessDayTenor()) {
      startDate = TenorUtils.adjustDateByTenor(date, startTenor, _calendar, 0);
      endDate = TenorUtils.adjustDateByTenor(startDate, endTenor, _calendar, _spotLag);
    } else {
      final ZonedDateTime spot = TenorUtils.adjustDateByTenor(date, startTenor, _calendar, _spotLag);
      startDate = ScheduleCalculator.getAdjustedDate(spot, startTenor, _businessDayConvention, _calendar, 0, _endOfMonth);
      endDate = ScheduleCalculator.getAdjustedDate(startDate, endTenor, _businessDayConvention, _calendar, 0, _endOfMonth);
    }
    final double accrualFactor = _dayCount.getDayCountFraction(startDate, endDate, new CalendarAdapter(_calendar));
    return new CashDefinition(_currency, startDate, endDate, notional, fixedRate, accrualFactor);
  }

  /**
   * Gets the business day convention.
   * @return  the business day convention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the calendar.
   * @return  the calendar
   */
  public WorkingDayCalendar getCalendar() {
    return _calendar;
  }

  /**
   * Gets the currency.
   * @return  the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the day count.
   * @return  the day count
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Returns true if the end of month is considered.
   * @return  true if the end of month is considered
   */
  public boolean isEndOfMonthConvention() {
    return _endOfMonth;
  }

  /**
   * Gets the spot lag.
   * @return  the spot lag
   */
  public int getSpotLag() {
    return _spotLag;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_businessDayConvention == null ? 0 : _businessDayConvention.hashCode());
    result = prime * result + (_calendar == null ? 0 : _calendar.hashCode());
    result = prime * result + (_currency == null ? 0 : _currency.hashCode());
    result = prime * result + (_dayCount == null ? 0 : _dayCount.hashCode());
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _spotLag;
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
    final CashConvention other = (CashConvention) obj;
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    if (!Objects.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    if (!Objects.equals(_dayCount, other._dayCount)) {
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
    builder.append("CashConvention [currency=");
    builder.append(_currency.getCode());
    builder.append(", calendar=");
    builder.append(_calendar.getName());
    builder.append(", spotLag=");
    builder.append(_spotLag);
    builder.append(", dayCount=");
    builder.append(_dayCount.getName());
    builder.append(", businessDayConvention=");
    builder.append(_businessDayConvention.getName());
    builder.append(", endOfMonth=");
    builder.append(_endOfMonth);
    builder.append("]");
    return builder.toString();
  }

}
