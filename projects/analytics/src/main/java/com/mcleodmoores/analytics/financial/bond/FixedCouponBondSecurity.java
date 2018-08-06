/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import static com.mcleodmoores.analytics.financial.instruments.BuilderUtils.isSet;
import static com.mcleodmoores.analytics.financial.instruments.BuilderUtils.notEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.mcleodmoores.analytics.financial.annuity.PaymentComparator;
import com.mcleodmoores.analytics.financial.instruments.BondSecurityVisitor;
import com.mcleodmoores.analytics.financial.instruments.DataBondSecurityVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes a fixed coupon bond issue. The description can include variable notionals and coupons.
 */
public class FixedCouponBondSecurity implements BondInstrument {

  /**
   * Creates a fixed coupon bond builder.
   *
   * @return  a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A builder for fixed coupon bonds.
   */
  public static class Builder {
    private final List<PaymentFixed> _nominal;
    private final List<CouponFixed> _coupons;
    private LegalEntity _legalEntity;
    private BondConventionType _yieldConvention;
    private Double _settlementTime;
    private Double _accruedInterest;
    private Integer _couponsPerYear;
    private Double _accrualFactorToNextCoupon;

    private Builder() {
      _nominal = new ArrayList<>();
      _coupons = new ArrayList<>();
    }

    /**
     * Sets the legal entity.
     *
     * @param legalEntity  the legal entity, not null
     * @return  the builder
     */
    public Builder withLegalEntity(final LegalEntity legalEntity) {
      _legalEntity = ArgumentChecker.notNull(legalEntity, "legalEntity");
      return this;
    }

    /**
     * Sets the yield convention.
     *
     * @param yieldConvention  the yield convention, not null
     * @return  the builder
     */
    public Builder withYieldConvention(final BondConventionType yieldConvention) {
      _yieldConvention = ArgumentChecker.notNull(yieldConvention, "yieldConvention");
      return this;
    }

    /**
     * Sets the settlement time.
     *
     * @param settlementTime  the settlement time, not negative
     * @return  the builder
     */
    public Builder withSettlementTime(final double settlementTime) {
      _settlementTime = ArgumentChecker.notNegative(settlementTime, "settlementTime");
      return this;
    }

    /**
     * Sets the accrued interest.
     *
     * @param accruedInterest  the accrued interest, not negative
     * @return  the builder
     */
    public Builder withAccruedInterest(final double accruedInterest) {
      _accruedInterest = ArgumentChecker.notNegative(accruedInterest, "accruedInterest");
      return this;
    }

    /**
     * Sets the number of coupons in a year.
     *
     * @param couponsPerYear  the number of coupons in a year, greater than zero
     * @return  the builder
     */
    public Builder withCouponsPerYear(final int couponsPerYear) {
      _couponsPerYear = ArgumentChecker.notNegativeOrZero(couponsPerYear, "couponsPerYear");
      return this;
    }

    /**
     * Sets the accrual factor (as a year fraction according to convention).
     *
     * @param accrualFactorToNextCoupon  the accrual factor, not negative
     * @return  the builder
     */
    public Builder withAccrualFactorToNextCoupon(final double accrualFactorToNextCoupon) {
      _accrualFactorToNextCoupon = ArgumentChecker.notNegative(accrualFactorToNextCoupon, "accrualFactorToNextCoupon");
      return this;
    }

    /**
     * Adds a payment to the nominal schedule.
     *
     * @param payment  the payment, not null
     * @return  the builder
     */
    public Builder withNominal(final PaymentFixed payment) {
      _nominal.add(ArgumentChecker.notNull(payment, "payment"));
      return this;
    }

    /**
     * Adds payments to the nominal schedule.
     *
     * @param payments  the payments, not null
     * @return  the builder
     */
    public Builder withNominals(final PaymentFixed[] payments) {
      _nominal.addAll(Arrays.asList(ArgumentChecker.notNull(payments, "payments")));
      return this;
    }

    /**
     * Adds payments to the nominal schedule.
     *
     * @param payments  the payments, not null
     * @return  the builder
     */
    public Builder withNominals(final List<PaymentFixed> payments) {
      _nominal.addAll(ArgumentChecker.notNull(payments, "payments"));
      return this;
    }

    /**
     * Adds a coupon to the coupon schedule.
     *
     * @param coupon  the coupon, not null
     * @return  the builder
     */
    public Builder withCoupon(final CouponFixed coupon) {
      _coupons.add(ArgumentChecker.notNull(coupon, "coupon"));
      return this;
    }

    /**
     * Adds coupons to the coupon schedule.
     *
     * @param coupons  the coupons, not null
     * @return  the builder
     */
    public Builder withCoupons(final CouponFixed[] coupons) {
      _coupons.addAll(Arrays.asList(ArgumentChecker.notNull(coupons, "coupons")));
      return this;
    }

    /**
     * Adds coupons to the coupon schedule.
     *
     * @param coupons  the coupons, not null
     * @return  the builder
     */
    public Builder withCoupons(final List<CouponFixed> coupons) {
      _coupons.addAll(ArgumentChecker.notNull(coupons, "coupons"));
      return this;
    }

    /**
     * Builds the bond. If a field has not been set, an exception is thrown.
     *
     * @return  the bond
     */
    public FixedCouponBondSecurity build() {
      Collections.sort(_nominal, PaymentComparator.INSTANCE);
      Collections.sort(_coupons, PaymentComparator.INSTANCE);
      final AnnuityPaymentFixed nominals = new AnnuityPaymentFixed(notEmpty(_nominal, "nominals").toArray(new PaymentFixed[0]));
      final AnnuityCouponFixed coupons = new AnnuityCouponFixed(notEmpty(_coupons, "coupons").toArray(new CouponFixed[0]));
      return new FixedCouponBondSecurity(nominals, coupons, isSet(_legalEntity, "legalEntity"),
          isSet(_yieldConvention, "yieldConvention"), isSet(_settlementTime, "settlementTime"), isSet(_accruedInterest, "accruedInterest"),
          isSet(_couponsPerYear, "couponsPerYear"), isSet(_accrualFactorToNextCoupon, "accrualFactorToNextCoupon"));
    }
  }

  private final AnnuityPaymentFixed _nominal;
  private final AnnuityCouponFixed _coupons;
  private final LegalEntity _legalEntity;
  private final BondConventionType _yieldConvention;
  private final double _settlementTime;
  private final double _accruedInterest;
  private final int _couponsPerYear;
  private final double _accrualFactorToNextCoupon;

  private FixedCouponBondSecurity(final AnnuityPaymentFixed nominal, final AnnuityCouponFixed coupon, final LegalEntity legalEntity,
      final BondConventionType yieldConvention, final double settlementTime, final double accruedInterest, final int couponsPerYear,
      final double accrualFactorToNextCoupon) {
    final PaymentFixed[] nominalPayments = nominal.getPayments();
    final CouponFixed[] couponPayments = coupon.getPayments();
    final PaymentFixed[] nominalCopy = new PaymentFixed[nominalPayments.length];
    final CouponFixed[] couponCopy = new CouponFixed[couponPayments.length];
    System.arraycopy(nominalPayments, 0, nominalCopy, 0, nominalPayments.length);
    System.arraycopy(couponPayments, 0, couponCopy, 0, couponPayments.length);
    _nominal = new AnnuityPaymentFixed(nominalCopy);
    _coupons = new AnnuityCouponFixed(couponCopy);
    _legalEntity = legalEntity;
    _yieldConvention = yieldConvention;
    _settlementTime = settlementTime;
    _accruedInterest = accruedInterest;
    _couponsPerYear = couponsPerYear;
    _accrualFactorToNextCoupon = accrualFactorToNextCoupon;
  }

  @Override
  public <RESULT_TYPE> RESULT_TYPE accept(final BondSecurityVisitor<RESULT_TYPE> visitor) {
    return visitor.visitFixedCouponBond(this);
  }

  @Override
  public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final DataBondSecurityVisitor<DATA_TYPE, RESULT_TYPE> visitor, final DATA_TYPE data) {
    return visitor.visitFixedCouponBond(this, data);
  }

  @Override
  public <RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<RESULT_TYPE> visitor) {
    return _yieldConvention.accept(visitor, this);
  }

  @Override
  public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final DataYieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final DATA_TYPE data) {
    return _yieldConvention.accept(visitor, this, data);
  }

  /**
   * Gets the nominal payments as an annuity.
   *
   * @return  the nominal payments
   */
  public AnnuityPaymentFixed getNominalPayments() {
    return _nominal;
  }

  /**
   * Gets the coupons as an annuity.
   *
   * @return  the coupons
   */
  public AnnuityCouponFixed getCoupons() {
    return _coupons;
  }

  /**
   * Gets the legal entity.
   *
   * @return the legal entity
   */
  public LegalEntity getLegalEntity() {
    return _legalEntity;
  }

  /**
   * Gets the yield convention.
   *
   * @return  the yield convention
   */
  public BondConventionType getYieldConventionType() {
    return _yieldConvention;
  }

  /**
   * Gets the time to settlement in years.
   *
   * @return  the time to settlement
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * Gets the accrued interest.
   *
   * @return  the accrued interest
   */
  public double getAccruedInterest() {
    return _accruedInterest;
  }

  /**
   * Gets the number of coupons in a year.
   *
   * @return  the number of coupons in a year
   */
  public int getCouponsPerYear() {
    return _couponsPerYear;
  }

  /**
   * Gets the accrual factor (the year fraction as defined by the bond market convention) to the payment of the next coupon.
   *
   * @return  the accrual factor
   */
  public double getAccrualFactorToNextCoupon() {
    return _accrualFactorToNextCoupon;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_accrualFactorToNextCoupon);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_accruedInterest);
    result = prime * result + (int) (temp ^ temp >>> 32);
    result = prime * result + (_coupons == null ? 0 : _coupons.hashCode());
    result = prime * result + _couponsPerYear;
    result = prime * result + (_legalEntity == null ? 0 : _legalEntity.hashCode());
    result = prime * result + (_nominal == null ? 0 : _nominal.hashCode());
    temp = Double.doubleToLongBits(_settlementTime);
    result = prime * result + (int) (temp ^ temp >>> 32);
    result = prime * result + (_yieldConvention == null ? 0 : _yieldConvention.hashCode());
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
    if (!(obj instanceof FixedCouponBondSecurity)) {
      return false;
    }
    final FixedCouponBondSecurity other = (FixedCouponBondSecurity) obj;
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
      return false;
    }
    if (_yieldConvention != other._yieldConvention) {
      return false;
    }
    if (Double.doubleToLongBits(_accrualFactorToNextCoupon) != Double.doubleToLongBits(other._accrualFactorToNextCoupon)) {
      return false;
    }
    if (Double.doubleToLongBits(_accruedInterest) != Double.doubleToLongBits(other._accruedInterest)) {
      return false;
    }
    if (_couponsPerYear != other._couponsPerYear) {
      return false;
    }
    if (!ObjectUtils.equals(_legalEntity, other._legalEntity)) {
      return false;
    }
    if (!ObjectUtils.equals(_nominal, other._nominal)) {
      return false;
    }
    if (!ObjectUtils.equals(_coupons, other._coupons)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FixedCouponBondSecurity[settlementTime=");
    sb.append(_settlementTime);
    sb.append(", couponsPerYear=");
    sb.append(_couponsPerYear);
    sb.append(", accruedInterest=");
    sb.append(_accruedInterest);
    sb.append(", accrualFactorToNextCoupon=");
    sb.append(_accrualFactorToNextCoupon);
    sb.append(", yieldConvention=");
    sb.append(_yieldConvention.name());
    sb.append(", legalEntity=");
    sb.append(_legalEntity);
    sb.append("\nnotionalPayments=[");
    int i = 0;
    for (final PaymentFixed p : _nominal.getPayments()) {
      sb.append(p);
      if (i < _nominal.getNumberOfPayments() - 1) {
        sb.append(", ");
      }
      i++;
    }
    i = 0;
    sb.append("]\ncouponPayments=[");
    for (final CouponFixed p : _coupons.getPayments()) {
      sb.append(p);
      if (i < _coupons.getNumberOfPayments() - 1) {
        sb.append("\n\t\t\t\t");
      }
      i++;
    }
    sb.append("]]");
    return sb.toString();
  }
}
