/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.interestrate.payments.derivative;

import com.mcleodmoores.analytics.util.BuilderChecker;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.util.money.Currency;

/**
 * Builds a {@link CapFloorIbor}.
 */
public final class IborCapletFloorletBuilder {

  /**
   * Gets a cap/floor builder.
   * @return  the builder
   */
  public static IborCapletFloorletBuilder builder() {
    return new IborCapletFloorletBuilder();
  }

  private Currency _currency;
  private Double _paymentTime;
  private Double _paymentYearFraction;
  private Double _notional;
  private Double _fixingTime;
  private Double _fixingPeriodStartTime;
  private Double _fixingPeriodEndTime;
  private Double _fixingYearFraction;
  private IborIndex _index;
  private Double _strike;

  /**
   * Restricted constructor.
   */
  private IborCapletFloorletBuilder() {
  }

  /**
   * Sets the currency.
   * @param currency  the currency, not null
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withCurrency(final Currency currency) {
    _currency = currency;
    return this;
  }

  /**
   * Sets the payment time.
   * @param paymentTime  the payment time
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withPaymentTime(final double paymentTime) {
    _paymentTime = paymentTime;
    return this;
  }

  /**
   * Sets the payment year fraction.
   * @param paymentYearFraction  the payment year fraction
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withPaymentYearFraction(final double paymentYearFraction) {
    _paymentYearFraction = paymentYearFraction;
    return this;
  }

  /**
   * Sets the notional.
   * @param notional  the notional
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withNotional(final double notional) {
    _notional = notional;
    return this;
  }

  /**
   * Sets the fixing time, which cannot be after the fixing period start time.
   * @param fixingTime  the fixing time
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withFixingTime(final double fixingTime) {
    _fixingTime = fixingTime;
    return this;
  }

  /**
   * Sets the fixing period start time, which cannot be before the fixing time.
   * @param fixingPeriodStartTime  the fixing period start time
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withFixingPeriodStartTime(final double fixingPeriodStartTime) {
    _fixingPeriodStartTime = fixingPeriodStartTime;
    return this;
  }

  /**
   * Sets the fixing period end time, which cannot be before the fixing period start time.
   * @param fixingPeriodEndTime  the fixing period end time
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withFixingPeriodEndTime(final double fixingPeriodEndTime) {
    _fixingPeriodEndTime = fixingPeriodEndTime;
    return this;
  }

  /**
   * Sets the fixing year fraction.
   * @param fixingYearFraction  the fixing year fraction, cannot be negative
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withFixingYearFraction(final double fixingYearFraction) {
    _fixingYearFraction = fixingYearFraction;
    return this;
  }

  /**
   * Sets the ibor index.
   * @param index  the index, not null
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withIndex(final IborIndex index) {
    _index = index;
    return this;
  }

  /**
   * Sets the strike.
   * @param strike  the strike
   * @return  the CapFloorIborBuilder
   */
  public IborCapletFloorletBuilder withStrike(final double strike) {
    _strike = strike;
    return this;
  }

  /**
   * Builds a cap.
   * @return  the cap
   */
  public CapFloorIbor buildCap() {
    return build(true);
  }

  /**
   * Builds a floor.
   * @return  the floor
   */
  public CapFloorIbor buildFloor() {
    return build(false);
  }

  /**
   * Builds a cap or floor.
   * @param isCap  true to build a cap
   * @return  the cap or floor
   */
  public CapFloorIbor build(final boolean isCap) {
    BuilderChecker.notNull(_currency, "currency");
    BuilderChecker.notNull(_paymentTime, "payment time");
    BuilderChecker.notNull(_paymentYearFraction, "payment year fraction");
    BuilderChecker.notNull(_notional, "notional");
    BuilderChecker.notNull(_fixingTime, "fixing time");
    BuilderChecker.notNull(_index, "index");
    BuilderChecker.notNull(_fixingPeriodStartTime, "fixing period start time");
    BuilderChecker.notNull(_fixingPeriodEndTime, "fixing period end time");
    BuilderChecker.notNull(_fixingYearFraction, "fixing year fraction");
    BuilderChecker.notNull(_strike, "strike");
    return new CapFloorIbor(_currency, _paymentTime, _paymentYearFraction, _notional, _fixingTime, _index, _fixingPeriodStartTime, _fixingPeriodEndTime,
        _fixingYearFraction, _strike, isCap);
  }
}

