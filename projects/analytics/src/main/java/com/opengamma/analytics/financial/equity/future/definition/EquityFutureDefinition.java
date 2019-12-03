/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class EquityFutureDefinition implements InstrumentDefinitionWithData<EquityFuture, Double> {

  private final ZonedDateTime _expiryDate;
  private final ZonedDateTime _settlementDate;
  private final double _strikePrice;
  private final Currency _currency;
  private final double _unitAmount;

  /**
   * Basic setup for an Equity Future. TODO resolve conventions; complete param set
   * 
   * @param expiryDate
   *          The date-time at which the reference rate is fixed and the future is cash settled
   * @param settlementDate
   *          The date on which exchange is made, whether physical asset or cash equivalent
   * @param strikePrice
   *          The reference price at which the future will be settled
   * @param currency
   *          The reporting currency of the future
   * @param unitValue
   *          The currency value that the price of one contract will move by when the asset's price moves by one point
   */
  public EquityFutureDefinition(final ZonedDateTime expiryDate, final ZonedDateTime settlementDate, final double strikePrice, final Currency currency,
      final double unitValue) {
    _expiryDate = ArgumentChecker.notNull(expiryDate, "expiry");
    _settlementDate = ArgumentChecker.notNull(settlementDate, "settlement date");
    _currency = ArgumentChecker.notNull(currency, "currency");
    _strikePrice = strikePrice;
    _unitAmount = unitValue;
  }

  /**
   * Gets the expiry date: the date on which the reference index level is fixed and the future is cash settled.
   *
   * @return the expiry date
   */
  public ZonedDateTime getExpiryDate() {
    return _expiryDate;
  }

  /**
   * Gets the settlement date: the date on which the cash exchange is made.
   *
   * @return the settlement date
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the strike price.
   *
   * @return the strike price
   */
  public double getStrikePrice() {
    return _strikePrice;
  }

  /**
   * Gets the currency.
   *
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the unit amount. This represents the PNL of a single long contract if its price increases by 1.0. Also known as the 'Point Value'.
   * 
   * @return the unit amount
   */
  public double getUnitAmount() {
    return _unitAmount;
  }

  @Override
  public EquityFuture toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date);
  }

  @Override
  public EquityFuture toDerivative(final ZonedDateTime date, final Double referencePrice, final String... yieldCurveNames) {
    return toDerivative(date, referencePrice);
  }

  @Override
  public EquityFuture toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(_expiryDate), "Valuation date is after expiry date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, _expiryDate);
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, _settlementDate);
    final EquityFuture newDeriv = new EquityFuture(timeToFixing, timeToDelivery, _strikePrice, _currency, _unitAmount);
    return newDeriv;
  }

  @Override
  public EquityFuture toDerivative(final ZonedDateTime date, final Double referencePrice) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(_expiryDate), "Valuation date is after expiry date");
    if (referencePrice == null) {
      return toDerivative(date);
    }
    final double timeToFixing = TimeCalculator.getTimeBetween(date, _expiryDate);
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, _settlementDate);
    final EquityFuture newDeriv = new EquityFuture(timeToFixing, timeToDelivery, referencePrice, _currency, _unitAmount);
    return newDeriv;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _expiryDate.hashCode();
    result = prime * result + _settlementDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_strikePrice);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ temp >>> 32);
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
    final EquityFutureDefinition other = (EquityFutureDefinition) obj;
    if (Double.doubleToLongBits(_strikePrice) != Double.doubleToLongBits(other._strikePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (Double.doubleToLongBits(_unitAmount) != Double.doubleToLongBits(other._unitAmount)) {
      return false;
    }
    return true;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityFutureDefinition(this);
  }

}
