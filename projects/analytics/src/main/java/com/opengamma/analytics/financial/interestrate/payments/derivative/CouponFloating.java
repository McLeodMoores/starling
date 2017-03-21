/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a generic floating coupon with a unique fixing date.
 */
public abstract class CouponFloating extends Coupon {

  /**
   * The floating coupon fixing time.
   */
  private final double _fixingTime;

  /**
   * Constructor from all the details.
   * @param currency  the payment currency, not null
   * @param paymentTime  time in years up to the payment
   * @param fundingCurveName  name of the funding curve, not null
   * @param paymentYearFraction  the year fraction (or accrual factor) for the coupon payment
   * @param notional  coupon notional
   * @param fixingTime  time in years up to fixing
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public CouponFloating(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction,
      final double notional, final double fixingTime) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    ArgumentChecker.isTrue(fixingTime >= 0.0, "fixing time < 0");
    _fixingTime = fixingTime;
  }

  /**
   * Constructor from all the details.
   * @param currency  the payment currency, not null
   * @param paymentTime  time in years up to the payment
   * @param paymentYearFraction  the year fraction (or accrual factor) for the coupon payment
   * @param notional  coupon notional
   * @param fixingTime  time in years up to fixing
   */
  public CouponFloating(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime) {
    super(currency, paymentTime, paymentYearFraction, notional);
    ArgumentChecker.isTrue(fixingTime >= 0.0, "fixing time < 0");
    _fixingTime = fixingTime;
  }

  /**
   * Gets the floating coupon fixing time.
   * @return The fixing time.
   */
  public double getFixingTime() {
    return _fixingTime;
  }

  @SuppressWarnings("deprecation")
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("CouponFloating[_currency=");
    sb.append(getCurrency());
    sb.append(", _notional=");
    sb.append(getNotional());
    sb.append(", _fixingTime=");
    sb.append(getFixingTime());
    sb.append(", _paymentTime=");
    sb.append(getPaymentTime());
    sb.append(", _paymentYearFraction=");
    sb.append(getPaymentYearFraction());
    sb.append(", _referenceAmount=");
    sb.append(getReferenceAmount());
    String fundingCurveName = null;
    try {
      fundingCurveName = getFundingCurveName();
    } catch (final IllegalStateException e) {
      // wasn't set
    }
    if (fundingCurveName != null) {
      sb.append(", _fundingCurveName=");
      sb.append(getFundingCurveName());
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingTime);
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
    if (!(obj instanceof CouponFloating)) {
      return false;
    }
    final CouponFloating other = (CouponFloating) obj;
    if (Double.doubleToLongBits(_fixingTime) != Double.doubleToLongBits(other._fixingTime)) {
      return false;
    }
    return true;
  }

}
