/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed coupon.
 */
public class CouponFixed extends Coupon {

  /**
   * The coupon fixed rate.
   */
  private final double _fixedRate;
  /**
   * The paid amount.
   */
  private final double _amount;
  /**
   * The start date of the coupon accrual period. Can be null if of no use.
   */
  private final ZonedDateTime _accrualStartDate;
  /**
   * The end date of the coupon accrual period. Can be null if of no use.
   */
  private final ZonedDateTime _accrualEndDate;

  /**
   * Constructor from all details but accrual dates.
   * @param currency  the payment currency, not null
   * @param paymentTime  time in years up to the payment
   * @param fundingCurveName  name of the funding curve, not null
   * @param paymentYearFraction  the year fraction (or accrual factor) for the coupon payment
   * @param notional  coupon notional
   * @param rate  the coupon fixed rate
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public CouponFixed(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional,
      final double rate) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    _fixedRate = rate;
    _accrualStartDate = null;
    _accrualEndDate = null;
    _amount = paymentYearFraction * notional * rate;
  }

  /**
   * Constructor from all details.
   * @param currency  the payment currency, not null
   * @param paymentTime  time in years up to the payment
   * @param fundingCurveName  name of the funding curve, not null
   * @param paymentYearFraction  the year fraction (or accrual factor) for the coupon payment
   * @param notional  coupon notional
   * @param rate  the coupon fixed rate
   * @param accrualStartDate  the start date of the coupon accrual period, not null
   * @param accrualEndDate  the end date of the coupon accrual period, not null
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public CouponFixed(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional,
      final double rate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    _fixedRate = rate;
    _amount = paymentYearFraction * notional * rate;
    _accrualStartDate = accrualStartDate;
    _accrualEndDate = accrualEndDate;
  }

  /**
   * Constructor from details with notional defaulted to 1.
   * @param currency  the payment currency, not null
   * @param paymentTime  time in years up to the payment
   * @param fundingCurveName  name of the funding curve, not null
   * @param paymentYearFraction  the year fraction (or accrual factor) for the coupon payment
   * @param rate  the coupon fixed rate
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public CouponFixed(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double rate) {
    this(currency, paymentTime, fundingCurveName, paymentYearFraction, 1.0, rate);
  }

  /**
   * Constructor from all details but accrual dates.
   * @param currency  the payment currency, not null
   * @param paymentTime  time in years up to the payment
   * @param paymentYearFraction  the year fraction (or accrual factor) for the coupon payment
   * @param notional  coupon notional
   * @param rate  the coupon fixed rate
   */
  public CouponFixed(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double rate) {
    super(currency, paymentTime, paymentYearFraction, notional);
    _fixedRate = rate;
    _accrualStartDate = null;
    _accrualEndDate = null;
    _amount = paymentYearFraction * notional * rate;
  }

  /**
   * Constructor from all details.
   * @param currency  the payment currency, not null
   * @param paymentTime  time in years up to the payment
   * @param paymentYearFraction  the year fraction (or accrual factor) for the coupon payment
   * @param notional  coupon notional
   * @param rate  the coupon fixed rate
   * @param accrualStartDate  the start date of the coupon accrual period, not null
   * @param accrualEndDate  the end date of the coupon accrual period, not null
   */
  public CouponFixed(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double rate,
      final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate) {
    super(currency, paymentTime, paymentYearFraction, notional);
    _fixedRate = rate;
    _amount = paymentYearFraction * notional * rate;
    _accrualStartDate = accrualStartDate;
    _accrualEndDate = accrualEndDate;
  }

  /**
   * Constructor from details with notional defaulted to 1.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param rate The coupon fixed rate.
   */
  public CouponFixed(final Currency currency, final double paymentTime, final double paymentYearFraction, final double rate) {
    this(currency, paymentTime, paymentYearFraction, 1.0, rate);
  }

  /**
   * Gets the coupon fixed rate.
   * @return  the fixed rate
   */
  public double getFixedRate() {
    return _fixedRate;
  }

  /**
   * Gets the start date of the coupon accrual period.
   * @return  the accrual start date
   */
  public ZonedDateTime getAccrualStartDate() {
    return _accrualStartDate;
  }

  /**
   * Gets the end date of the coupon accrual period.
   * @return  the accrual end date
   */
  public ZonedDateTime getAccrualEndDate() {
    return _accrualEndDate;
  }

  /**
   * Gets the paid amount.
   * @return  the amount
   */
  public double getAmount() {
    return _amount;
  }

  /**
   * Creates a new coupon with the same characteristics, except the rate which is 1.0.
   * @return  the new coupon
   */
  @SuppressWarnings("deprecation")
  public CouponFixed withUnitCoupon() {
    try {
      return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), 1);
    } catch (final IllegalStateException e) {
      return new CouponFixed(getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(), 1);
    }
  }

  /**
   * Create a new fixed coupon with all the details unchanged except that the rate is the one provided.
   * @param rate  the new rate
   * @return  the coupon
   */
  @SuppressWarnings("deprecation")
  public CouponFixed withRate(final double rate) {
    try {
      return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(),
          rate, getAccrualStartDate(), getAccrualEndDate());
    } catch (final IllegalStateException e) {
      return new CouponFixed(getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(), rate, getAccrualStartDate(), getAccrualEndDate());
    }
  }

  /**
   * Create a new fixed coupon with all the details unchanged except that the rate is shifted by the spread.
   * @param spread  the rate spread
   * @return  the coupon
   */
  @SuppressWarnings("deprecation")
  public CouponFixed withRateShifted(final double spread) {
    try {
      return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(),
          getFixedRate() + spread, getAccrualStartDate(), getAccrualEndDate());
    } catch (final IllegalStateException e) {
      return new CouponFixed(getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(), getFixedRate() + spread,
          getAccrualStartDate(), getAccrualEndDate());
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public CouponFixed withNotional(final double notional) {
    try {
      return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getFixedRate(),
          getAccrualStartDate(), getAccrualEndDate());
    } catch (final IllegalStateException e) {
      return new CouponFixed(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixedRate(),
          getAccrualStartDate(), getAccrualEndDate());
    }
  }

  /**
   * Returns a fixed payment with the same features (currency, payment time, amount) as the fixed coupon.
   * @return A fixed payment.
   */
  @SuppressWarnings("deprecation")
  public PaymentFixed toPaymentFixed() {
    try {
      return new PaymentFixed(getCurrency(), getPaymentTime(), _amount, getFundingCurveName());
    } catch (final IllegalStateException e) {
      return new PaymentFixed(getCurrency(), getPaymentTime(), _amount);
    }
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixed(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixed(this);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append(", getCurrency()=");
    builder.append(getCurrency());
    builder.append("CouponFixed [_fixedRate=");
    builder.append(_fixedRate);
    builder.append(", _amount=");
    builder.append(_amount);
    builder.append(", _accrualStartDate=");
    builder.append(_accrualStartDate);
    builder.append(", _accrualEndDate=");
    builder.append(_accrualEndDate);
    builder.append(", getPaymentYearFraction()=");
    builder.append(getPaymentYearFraction());
    builder.append(", getNotional()=");
    builder.append(getNotional());
    builder.append(", getReferenceAmount()=");
    builder.append(getReferenceAmount());
    builder.append(", getPaymentTime()=");
    builder.append(getPaymentTime());
    String fundingCurveName = null;
    try {
      fundingCurveName = getFundingCurveName();
    } catch (final IllegalStateException e) {
      // deprecated form was not used
    }
    if (fundingCurveName != null) {
      builder.append(", getFundingCurveName()=");
      builder.append(getFundingCurveName());
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_accrualEndDate == null ? 0 : _accrualEndDate.hashCode());
    result = prime * result + (_accrualStartDate == null ? 0 : _accrualStartDate.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_fixedRate);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof CouponFixed)) {
      return false;
    }
    final CouponFixed other = (CouponFixed) obj;
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixedRate) != Double.doubleToLongBits(other._fixedRate)) {
      return false;
    }
    if (!ObjectUtils.equals(_accrualEndDate, other._accrualEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_accrualStartDate, other._accrualStartDate)) {
      return false;
    }
    return true;
  }

}
