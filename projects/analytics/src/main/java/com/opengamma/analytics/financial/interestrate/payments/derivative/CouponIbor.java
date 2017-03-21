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

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a single currency Ibor-like coupon.
 */
@SuppressWarnings("deprecation")
public class CouponIbor extends CouponFloating implements DepositIndexCoupon<IborIndex> {

  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  private final IborIndex _index;
  /**
   * The fixing period start time (in years).
   */
  private final double _fixingPeriodStartTime;
  /**
   * The fixing period end time (in years).
   */
  private final double _fixingPeriodEndTime;
  /**
   * The fixing period year fraction (or accrual factor) in the fixing convention.
   */
  private final double _fixingAccrualFactor;
  /**
   * The forward curve name used in to estimate the fixing index.
   */
  private final String _forwardCurveName;

  /**
   * Constructor from all details.
   * @param currency  the payment currency, not null
   * @param paymentTime  time in years up to the payment
   * @param fundingCurveName  name of the funding curve, not null
   * @param paymentYearFraction  the year fraction (or accrual factor) for the coupon payment
   * @param notional  coupon notional
   * @param fixingTime  time (in years) up to fixing
   * @param index  the Ibor-like index on which the coupon fixes, not null
   * @param fixingPeriodStartTime  the fixing period start time in years
   * @param fixingPeriodEndTime  the fixing period end time in years
   * @param fixingYearFraction  the year fraction (or accrual factor) for the fixing period
   * @param forwardCurveName Name of the forward (or estimation) curve.
   * @deprecated Use the constructor that does not take yield curve names
   */
  @Deprecated
  public CouponIbor(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction,
      final double notional, final double fixingTime, final IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime,
      final double fixingYearFraction, final String forwardCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    ArgumentChecker.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    ArgumentChecker.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    _index = ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Index currency incompatible with coupon currency");
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingAccrualFactor = fixingYearFraction;
    _forwardCurveName = ArgumentChecker.notNull(forwardCurveName, "forwardCurveName");
  }

  /**
   * Constructor from all details.
   * @param currency  the payment currency, not null
   * @param paymentTime  time in years up to the payment
   * @param paymentYearFraction  the year fraction (or accrual factor) for the coupon payment
   * @param notional  coupon notional
   * @param fixingTime  time (in years) up to fixing
   * @param index  the Ibor-like index on which the coupon fixes
   * @param fixingPeriodStartTime  the fixing period start time in years
   * @param fixingPeriodEndTime  the fixing period end time in years
   * @param fixingYearFraction  the year fraction (or accrual factor) for the fixing period
   */
  public CouponIbor(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime,
      final IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    ArgumentChecker.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    ArgumentChecker.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Index currency incompatible with coupon currency");
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingAccrualFactor = fixingYearFraction;
    _forwardCurveName = null;
    _index = ArgumentChecker.notNull(index, "index");
  }

  /**
   * Gets the fixing period start time (in years).
   * @return The fixing period start time.
   */
  public double getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixing period end time (in years).
   * @return The fixing period end time.
   */
  public double getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the accrual factor for the fixing period.
   * @return The accrual factor.
   */
  public double getFixingAccrualFactor() {
    return _fixingAccrualFactor;
  }

  /**
   * Gets the forward curve name.
   * @return The name.
   * @deprecated Curve names should no longer be set in {@link com.opengamma.analytics.financial.instrument.InstrumentDefinition}s
   */
  @Deprecated
  public String getForwardCurveName() {
    if (_forwardCurveName == null) {
      throw new IllegalStateException("Forward curve name was not set");
    }
    return _forwardCurveName;
  }

  /**
   * Gets the Ibor-like index.
   * @return The index.
   */
  @Override
  public IborIndex getIndex() {
    return _index;
  }

  @Override
  public CouponIbor withNotional(final double notional) {
    if (_forwardCurveName == null) {
      return new CouponIbor(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(), _index,
          getFixingPeriodStartTime(), getFixingPeriodEndTime(), getFixingAccrualFactor());
    }
    return new CouponIbor(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getFixingTime(),
        _index, getFixingPeriodStartTime(), getFixingPeriodEndTime(), getFixingAccrualFactor(), getForwardCurveName());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("CouponIbor[_index=");
    sb.append(_index);
    sb.append(", _currency=");
    sb.append(getCurrency());
    sb.append(", _notional=");
    sb.append(getNotional());
    sb.append(", _fixingTime=");
    sb.append(getFixingTime());
    sb.append(", _fixingPeriodStartTime=");
    sb.append(_fixingPeriodStartTime);
    sb.append(", _fixingPeriodEndTime=");
    sb.append(_fixingPeriodEndTime);
    sb.append(", _fixingAccrualFactor=");
    sb.append(_fixingAccrualFactor);
    sb.append(", _paymentTime=");
    sb.append(getPaymentTime());
    sb.append(", _paymentYearFraction=");
    sb.append(getPaymentYearFraction());
    sb.append(", _referenceAmount=");
    sb.append(getReferenceAmount());
    if (_forwardCurveName != null) {
      sb.append(", _fundingCurveName=");
      sb.append(getFundingCurveName());
      sb.append(", _forwardCurveName=");
      sb.append(_forwardCurveName);
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodEndTime);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_fixingPeriodStartTime);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_fixingAccrualFactor);
    result = prime * result + (int) (temp ^ temp >>> 32);
    result = prime * result + (_forwardCurveName == null ? 0 : _forwardCurveName.hashCode());
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
    if (!(obj instanceof CouponIbor)) {
      return false;
    }
    final CouponIbor other = (CouponIbor) obj;
    if (Double.doubleToLongBits(_fixingPeriodEndTime) != Double.doubleToLongBits(other._fixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime) != Double.doubleToLongBits(other._fixingPeriodStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingAccrualFactor) != Double.doubleToLongBits(other._fixingAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardCurveName, other._forwardCurveName)) {
      return false;
    }
    return true;
  }

  /**
   * Creates a coupon with the same payment terms but with unit notional.
   * @return  the coupon with unit notional
   */
  public CouponIbor withUnitCoupon() {
    if (_forwardCurveName == null) {
      return new CouponIbor(getCurrency(), getPaymentTime(), getPaymentYearFraction(), 1, getFixingTime(), getIndex(), getFixingPeriodStartTime(),
          getFixingPeriodEndTime(), getFixingAccrualFactor());
    }
    return new CouponIbor(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), 1, getFixingTime(), getIndex(),
        getFixingPeriodStartTime(), getFixingPeriodEndTime(), getFixingAccrualFactor(), getForwardCurveName());
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIbor(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIbor(this);
  }

}
