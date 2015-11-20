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

import com.opengamma.analytics.date.CalendarAdapter;
import com.opengamma.analytics.date.WorkingDayCalendar;
import com.opengamma.analytics.date.WorkingDayCalendarAdapter;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.bond.calculator.PriceFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes a (Treasury) bill transaction, which includes information about the date on which the transaction settles
 * and the amount paid or received when entered.
 */
public class BillTransactionDefinition implements InstrumentDefinition<BillTransaction> {

  /**
   * The bill security underlying the transaction.
   */
  private final BillSecurityDefinition _underlying;
  /**
   * The bill quantity.
   */
  private final double _quantity;
  /**
   * The date at which the bill transaction is settled.
   */
  private final ZonedDateTime _settlementDate;
  /**
   * The amount paid or received at settlement date for the bill transaction.
   * The amount is negative for a purchase and positive for a sale.
   */
  private final double _settlementAmount;

  /**
   * Creates a transaction.
   * @param underlying  the bill security underlying the transaction, not null
   * @param quantity  the quantity of bills, not null
   * @param settlementDate  the date on which the bill transaction is settled, not null
   * @param settlementAmount  the amount paid at settlement date for the bill transaction, negative for a purchase and positive for a sale
   */
  public BillTransactionDefinition(final BillSecurityDefinition underlying, final double quantity, final ZonedDateTime settlementDate,
      final double settlementAmount) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
    _settlementDate = ArgumentChecker.notNull(settlementDate, "settlementDate");
    ArgumentChecker.isTrue(quantity * settlementAmount <= 0, "Quantity and settlement amount should have opposite signs");
    _quantity = quantity;
    _settlementAmount = settlementAmount;
  }

  /**
   * Creates a transaction using the yield and yield convention of the underlying to calculate the settlement amount.
   * @param underlying  the bill security underlying the transaction, not null
   * @param quantity  the bill quantity, not null
   * @param settlementDate  the date at which the bill transaction is settled, not null
   * @param yield  the transaction yield, should be consistent with the yield convention of the underlying security
   * @param calendar  the holiday calendar, not null
   * @return  a bill transaction
   * @deprecated  Use {@link #fromYield(BillSecurityDefinition, double, ZonedDateTime, double, WorkingDayCalendar)}, which
   * takes the non-deprecated version of a calendar.
   */
  @Deprecated
  public static BillTransactionDefinition fromYield(final BillSecurityDefinition underlying, final double quantity, final ZonedDateTime settlementDate,
      final double yield, final Calendar calendar) {
    return fromYield(underlying, quantity, settlementDate, yield,
        (WorkingDayCalendar) new WorkingDayCalendarAdapter(calendar, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
  }

  /**
   * Creates a transaction using the yield and yield convention of the underlying to calculate the settlement amount.
   * @param underlying  the bill security underlying the transaction, not null
   * @param quantity  the bill quantity, not null
   * @param settlementDate  the date at which the bill transaction is settled, not null
   * @param yield  the transaction yield, should be consistent with the yield convention of the underlying security
   * @param workingDayCalendar  the holiday calendar, not null
   * @return  a bill transaction
   */
  public static BillTransactionDefinition fromYield(final BillSecurityDefinition underlying, final double quantity, final ZonedDateTime settlementDate,
      final double yield, final WorkingDayCalendar workingDayCalendar) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(settlementDate, "settlementDate");
    final Calendar calendar = new CalendarAdapter(workingDayCalendar);
    final double accrualFactor = underlying.getDayCount().getDayCountFraction(settlementDate, underlying.getEndDate(), calendar);
    final double settlementAmount = -quantity * underlying.getNotional() * PriceFromYieldCalculator.priceFromYield(underlying.getYieldConvention(),
        yield, accrualFactor);
    return new BillTransactionDefinition(underlying, quantity, settlementDate, settlementAmount);
  }

  /**
   * Gets the bill security underlying the transaction.
   * @return  the bill
   */
  public BillSecurityDefinition getUnderlying() {
    return _underlying;
  }

  /**
   * Gets quantity of bills in the transaction.
   * @return  the quantity
   */
  public double getQuantity() {
    return _quantity;
  }

  /**
   * Gets the date on which the bill transaction is settled.
   * @return  the date
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the amount paid on the settlement date for the bill transaction.
   * @return  the amount
   */
  public double getSettlementAmount() {
    return _settlementAmount;
  }

  @Override
  public String toString() {
    return "Transaction: " + _quantity + " of " + _underlying.toString();
  }

  /**
   * {@inheritDoc}
   * @deprecated  Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BillTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yieldCurveNames");
    final BillSecurity purchased = _underlying.toDerivative(date, _settlementDate, yieldCurveNames);
    final BillSecurity standard = _underlying.toDerivative(date, yieldCurveNames);
    final double amount = _settlementDate.isBefore(date) ? 0.0 : _settlementAmount;
    return new BillTransaction(purchased, _quantity, amount, standard);
  }

  @Override
  public BillTransaction toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final BillSecurity purchased = _underlying.toDerivative(date, _settlementDate);
    final BillSecurity standard = _underlying.toDerivative(date);
    final double amount = _settlementDate.isBefore(date) ? 0.0 : _settlementAmount;
    return new BillTransaction(purchased, _quantity, amount, standard);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillTransactionDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_quantity);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_settlementAmount);
    result = prime * result + (int) (temp ^ temp >>> 32);
    result = prime * result + _settlementDate.hashCode();
    result = prime * result + _underlying.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BillTransactionDefinition)) {
      return false;
    }
    final BillTransactionDefinition other = (BillTransactionDefinition) obj;
    if (Double.doubleToLongBits(_quantity) != Double.doubleToLongBits(other._quantity)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementAmount) != Double.doubleToLongBits(other._settlementAmount)) {
      return false;
    }
    if (!Objects.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    if (!Objects.equals(_underlying, other._underlying)) {
      return false;
    }
    return true;
  }

}
