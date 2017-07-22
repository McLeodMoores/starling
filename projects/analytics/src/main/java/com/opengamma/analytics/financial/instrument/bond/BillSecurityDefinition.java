/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.bond;

import java.util.Objects;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.mcleodmoores.date.WorkingDayCalendarAdapter;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Describes a (Treasury) bill security. A bill pays a fixed amount (notional) at a given date. There are no coupon or interest payments.
 */
public class BillSecurityDefinition implements InstrumentDefinition<BillSecurity> {

  /**
   * The bill currency.
   */
  private final Currency _currency;
  /**
   * The bill end or maturity date.
   */
  private final ZonedDateTime _endDate;
  /**
   * The bill nominal.
   */
  private final double _notional;
  /**
   * The standard number of days between trade date and trade settlement. Used for price and yield computation.
   */
  private final int _settlementDays;
  /**
   * The calendar used to compute the standard settlement date.
   */
  private final WorkingDayCalendar _calendar;
  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;
  /**
   * The yield day count convention.
   */
  private final DayCount _dayCount;
  /**
   * The issuer.
   */
  private final LegalEntity _issuer;

  /**
   * Creates a bill with a legal entity that contains only the issuer name.
   * @param currency  the bill currency, not null
   * @param endDate  the bill end or maturity date, not null
   * @param notional  the nominal amount
   * @param settlementDays  the standard number of days between trade date and trade settlement
   * @param calendar  the calendar used to compute the standard settlement date, not null
   * @param yieldConvention  the yield (to maturity) computation convention, not null
   * @param dayCount  the yield day count convention, not null
   * @param issuer  the bill issuer name, not null
   * @deprecated  Use {@link #BillSecurityDefinition(Currency, ZonedDateTime, double, int, WorkingDayCalendar, YieldConvention, DayCount, String)}.
   */
  @Deprecated
  public BillSecurityDefinition(final Currency currency, final ZonedDateTime endDate, final double notional, final int settlementDays,
      final Calendar calendar, final YieldConvention yieldConvention, final DayCount dayCount, final String issuer) {
    this(currency, endDate, notional, settlementDays, calendar, yieldConvention, dayCount, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Creates a bill.
   * @param currency  the bill currency, not null
   * @param endDate  the bill end or maturity date, not null
   * @param notional  the nominal amount
   * @param settlementDays  the standard number of days between trade date and trade settlement
   * @param calendar  the calendar used to compute the standard settlement date, not null
   * @param yieldConvention  the yield (to maturity) computation convention, not null
   * @param dayCount  the yield day count convention, not null
   * @param issuer  the bill issuer, not null
   * @deprecated  Use {@link #BillSecurityDefinition(Currency, ZonedDateTime, double, int, WorkingDayCalendar, YieldConvention, DayCount, LegalEntity)}.
   */
  @Deprecated
  public BillSecurityDefinition(final Currency currency, final ZonedDateTime endDate, final double notional, final int settlementDays, final Calendar calendar,
      final YieldConvention yieldConvention, final DayCount dayCount, final LegalEntity issuer) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(endDate, "End date");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    ArgumentChecker.notNull(dayCount, "Day count");
    ArgumentChecker.notNull(issuer, "Issuer");
    ArgumentChecker.isTrue(notional > 0.0, "Notional should be positive");
    _currency = currency;
    _endDate = endDate;
    _notional = notional;
    _settlementDays = settlementDays;
    _calendar = new WorkingDayCalendarAdapter(calendar, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    _issuer = issuer;
    _yieldConvention = yieldConvention;
    _dayCount = dayCount;
  }

  /**
   * Creates a bill with a legal entity that contains only the issuer name.
   * @param currency  the bill currency, not null
   * @param endDate  the bill end or maturity date, not null
   * @param notional  the nominal amount
   * @param settlementDays  the standard number of days between trade date and trade settlement
   * @param calendar  the calendar used to compute the standard settlement date, not null
   * @param yieldConvention  the yield (to maturity) computation convention, not null
   * @param dayCount  the yield day count convention, not null
   * @param issuer  the bill issuer name, not null
   */
  public BillSecurityDefinition(final Currency currency, final ZonedDateTime endDate, final double notional, final int settlementDays,
      final WorkingDayCalendar calendar, final YieldConvention yieldConvention, final DayCount dayCount, final String issuer) {
    this(currency, endDate, notional, settlementDays, calendar, yieldConvention, dayCount, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Creates a bill.
   * @param currency  the bill currency, not null
   * @param endDate  the bill end or maturity date, not null
   * @param notional  the nominal amount
   * @param settlementDays  the standard number of days between trade date and trade settlement
   * @param calendar  the calendar used to compute the standard settlement date, not null
   * @param yieldConvention  the yield (to maturity) computation convention, not null
   * @param dayCount  the yield day count convention, not null
   * @param issuer  the bill issuer, not null
   */
  public BillSecurityDefinition(final Currency currency, final ZonedDateTime endDate, final double notional, final int settlementDays,
      final WorkingDayCalendar calendar, final YieldConvention yieldConvention, final DayCount dayCount, final LegalEntity issuer) {
    _currency = ArgumentChecker.notNull(currency, "currency");
    _endDate = ArgumentChecker.notNull(endDate, "endDate");
    _notional = notional;
    _settlementDays = settlementDays;
    _calendar = ArgumentChecker.notNull(calendar, "calendar");
    _yieldConvention = ArgumentChecker.notNull(yieldConvention, "yieldConvention");
    _dayCount = ArgumentChecker.notNull(dayCount, "dayCount");
    _issuer = ArgumentChecker.notNull(issuer, "issuer");
    ArgumentChecker.isTrue(notional > 0.0, "Notional should be positive");
  }

  /**
   * Get the bill currency.
   * @return  the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the bill end or maturity date.
   * @return  the date
   */
  public ZonedDateTime getEndDate() {
    return _endDate;
  }

  /**
   * Gets the notional amount.
   * @return  the notional
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the standard number of days between trade date and trade settlement. Used for price and yield computation.
   * @return  the number of days between trade date and trade settlement.
   */
  public int getSettlementDays() {
    return _settlementDays;
  }

  /**
   * Gets the working day calendar used to compute the standard settlement date.
   * @return  the working day calendar
   */
  public WorkingDayCalendar getWorkingDayCalendar() {
    return _calendar;
  }

  /**
   * Gets the calendar used to compute the standard settlement date.
   * @return  the calendar
   * @deprecated  the calendar type is deprecated
   */
  @Deprecated
  public Calendar getCalendar() {
    return new CalendarAdapter(_calendar);
  }

  /**
   * Gets the yield (to maturity) computation convention.
   * @return  the convention
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  /**
   * Gets the yield day count convention.
   * @return  the convention
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the bill issuer name.
   * @return  the name
   */
  public String getIssuer() {
    return _issuer.getShortName();
  }

  /**
   * Gets the bill issuer.
   * @return  the issuer
   */
  public LegalEntity getIssuerEntity() {
    return _issuer;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Bill ");
    sb.append(_issuer);
    sb.append(" ");
    sb.append(_currency);
    sb.append(": maturity ");
    sb.append(_endDate);
    sb.append(" - notional ");
    sb.append(_notional);
    return sb.toString();
  }

  /**
   * Convert the "Definition" version to the "Derivative" version.
   * @param date The reference date.
   * @param settlementDate The bill settlement date.
   * @param yieldCurveNames The yield curves names. [0] discounting curve, [1] credit curve.
   * @return The bill security.
   * @deprecated Use the version without yield curve names
   */
  @Deprecated
  public BillSecurity toDerivative(final ZonedDateTime date, final ZonedDateTime settlementDate, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(settlementDate, "settlementDate");
    ArgumentChecker.notNull(yieldCurveNames, "yieldCurveNames");
    ArgumentChecker.isTrue(!date.isAfter(_endDate), "Reference date {} is after end date {}", date, _endDate);
    double settlementTime = TimeCalculator.getTimeBetween(date, settlementDate);
    settlementTime = Math.max(settlementTime, 0.0);
    final double endTime = TimeCalculator.getTimeBetween(date, _endDate);
    final double accrualFactor = _dayCount.getDayCountFraction(settlementDate, _endDate, new CalendarAdapter(_calendar));
    return new BillSecurity(_currency, settlementTime, endTime, _notional, _yieldConvention, accrualFactor, _issuer, yieldCurveNames[1], yieldCurveNames[0]);
  }

  /**
   * Convert the "Definition" version to the "Derivative" version.
   * @param date The reference date.
   * @param settlementDate The bill settlement date.
   * @return The bill security.
   */
  public BillSecurity toDerivative(final ZonedDateTime date, final ZonedDateTime settlementDate) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(settlementDate, "settlementDate");
    ArgumentChecker.isTrue(!date.isAfter(_endDate), "Reference date {} is after end date {}", date, _endDate);
    double settlementTime = TimeCalculator.getTimeBetween(date, settlementDate);
    settlementTime = Math.max(settlementTime, 0.0);
    final double endTime = TimeCalculator.getTimeBetween(date, _endDate);
    final double accrualFactor = _dayCount.getDayCountFraction(settlementDate, _endDate, new CalendarAdapter(_calendar));
    return new BillSecurity(_currency, settlementTime, endTime, _notional, _yieldConvention, accrualFactor, _issuer);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BillSecurity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yieldCurveNames");
    ArgumentChecker.isTrue(!date.isAfter(_endDate), "Reference date {} is after end date {}", date, _endDate);
    ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(date, _settlementDays, new CalendarAdapter(_calendar));
    settlementDate = settlementDate.isAfter(_endDate) ? _endDate : settlementDate;
    return toDerivative(date, settlementDate, yieldCurveNames);
  }

  @Override
  public BillSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(_endDate), "Reference date {} is after end date {}", date, _endDate);
    ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(date, _settlementDays, new CalendarAdapter(_calendar));
    settlementDate = settlementDate.isAfter(_endDate) ? _endDate : settlementDate;
    return toDerivative(date, settlementDate);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillSecurityDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calendar.hashCode();
    result = prime * result + _currency.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + _endDate.hashCode();
    result = prime * result + _issuer.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ temp >>> 32);
    result = prime * result + _settlementDays;
    result = prime * result + _yieldConvention.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BillSecurityDefinition)) {
      return false;
    }
    final BillSecurityDefinition other = (BillSecurityDefinition) obj;
    if (!Objects.equals(_endDate, other._endDate)) {
      return false;
    }
    if (!Objects.equals(_issuer, other._issuer)) {
      return false;
    }
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    if (!Objects.equals(_dayCount, other._dayCount)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (_settlementDays != other._settlementDays) {
      return false;
    }
    if (!Objects.equals(_yieldConvention, other._yieldConvention)) {
      return false;
    }
    if (!Objects.equals(_calendar, other._calendar)) {
      return false;
    }
    return true;
  }

}
