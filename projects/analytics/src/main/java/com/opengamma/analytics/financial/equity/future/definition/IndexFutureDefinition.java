/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.equity.future.derivative.IndexFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Generic index future definition. An IndexFuture is always cash-settled.
 */
public class IndexFutureDefinition implements InstrumentDefinitionWithData<IndexFuture, Double> {
  /** ZonedDateTime on which settlement value of index is fixed */
  private final ZonedDateTime _expiryDate;
  /** Date on which payment is made */
  private final ZonedDateTime _settlementDate;
  /** Identifier of the underlying commodity */
  private final ExternalId _underlying;
  /** reference price. Typically the price at which the trade was last margined */
  private final double _referencePrice;
  private final Currency _currency;
  /** Notional of a single contract */
  private final double _unitAmount;

  /**
   * Basic setup for an index Future.
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
   * @param underlying
   *          ExtenalId of the underlying index
   */
  public IndexFutureDefinition(final ZonedDateTime expiryDate, final ZonedDateTime settlementDate, final double strikePrice, final Currency currency,
      final double unitValue, final ExternalId underlying) {
    _expiryDate = ArgumentChecker.notNull(expiryDate, "expiry");
    _settlementDate = ArgumentChecker.notNull(settlementDate, "settlement date");
    _currency = ArgumentChecker.notNull(currency, "currency");
    _referencePrice = strikePrice;
    _unitAmount = unitValue;
    _underlying = underlying;
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
   * Gets the reference price.
   *
   * @return the reference
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  /**
   * Gets the strike price.
   *
   * @return the strike price
   */
  public double getStrikePrice() {
    return getReferencePrice();
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
   * Gets the unit amount.
   *
   * @return the unit amount
   */
  public double getUnitAmount() {
    return _unitAmount;
  }

  /**
   * Gets the underlying.
   *
   * @return the underlying
   */
  public ExternalId getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the settlementType.
   *
   * @return {@link SettlementType.CASH}
   */
  public SettlementType getSettlementType() {
    return SettlementType.CASH;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _expiryDate.hashCode();
    result = prime * result + _settlementDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_referencePrice);
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
    final IndexFutureDefinition other = (IndexFutureDefinition) obj;
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
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
    return visitor.visitIndexFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitIndexFutureDefinition(this);
  }

  @Override
  public IndexFuture toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date, getReferencePrice());
  }

  @Override
  public IndexFuture toDerivative(final ZonedDateTime date, final Double referencePrice, final String... yieldCurveNames) {
    return toDerivative(date, referencePrice);
  }

  @Override
  public IndexFuture toDerivative(final ZonedDateTime date) {
    return toDerivative(date, getReferencePrice());
  }

  @Override
  public IndexFuture toDerivative(final ZonedDateTime date, final Double referencePrice) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(_expiryDate), "Valuation date is after expiry date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, getExpiryDate());
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, getSettlementDate());
    final IndexFuture newDeriv = new IndexFuture(timeToFixing, timeToDelivery, referencePrice, getCurrency(), getUnitAmount());
    return newDeriv;
  }

}
