/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.convention.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * An extension of the cash convention that represents an overnight deposit convention. The required
 * fields are:
 * <ul>
 *  <li> A working day calendar that contains information about weekends and holidays ({@link WorkingDayCalendar}).
 *  <li> The currency for which the convention is used.
 *  <li> A day count convention that calculates the times between dates ({@link DayCount}).
 * </ul>
 */
public class OvernightDepositConvention extends CashConvention {

  /**
   * Gets the convention builder.
   * @return  the builder
   */
  public static OvernightDepositConvention.Builder builder() {
    return new Builder();
  }

  /**
   * A builder for this convention with methods overridden for covariant returns.
   */
  public static class Builder extends CashConvention.Builder {

    @Override
    public Builder withCurrency(final Currency currency) {
      super.withCurrency(currency);
      return this;
    }

    @Override
    public Builder withCalendar(final WorkingDayCalendar calendar) {
      super.withCalendar(calendar);
      return this;
    }

    @Override
    public Builder withDayCount(final DayCount dayCount) {
      super.withDayCount(dayCount);
      return this;
    }

    @Override
    public OvernightDepositConvention build() {
      if (getCurrency() == null) {
        throw new IllegalStateException("The currency must be supplied");
      }
      if (getCalendar() == null) {
        throw new IllegalStateException("The calendar must be supplied");
      }
      if (getDayCount() == null) {
        throw new IllegalStateException("The day count must be supplied");
      }
      return new OvernightDepositConvention(this);
    }

  }

  /**
   * Constructs the convention.
   * @param builder  the builder
   */
  /* package */OvernightDepositConvention(final Builder builder) {
    super(builder);
  }

  @Override
  public CashDefinition toCurveInstrument(final ZonedDateTime date, final Tenor startTenor, final Tenor endTenor, final double notional,
      final double fixedRate) {
    ArgumentChecker.notNull(date, "valuationDate");
    ArgumentChecker.notNull(startTenor, "startTenor");
    ArgumentChecker.notNull(endTenor, "endTenor");
    final ZonedDateTime startDate = TenorUtils.adjustDateByTenor(date, startTenor, getCalendar(), 0);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, getCalendar());
    final double accrualFactor = getDayCount().getDayCountFraction(startDate, endDate, new CalendarAdapter(getCalendar()));
    return new CashDefinition(getCurrency(), startDate, endDate, notional, fixedRate, accrualFactor);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("OvernightDepositConvention [currency=");
    builder.append(getCurrency().getCode());
    builder.append(", calendar=");
    builder.append(getCalendar().getName());
    builder.append(", dayCount=");
    builder.append(getDayCount().getName());
    builder.append("]");
    return builder.toString();
  }

}
