/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Describes a generic single currency bond issue. This base class contains a schedule of notional amounts, which may vary, a schedule of
 * coupon payments, information about the issuer, and the settlement time. The coupons can be any time (e.g. fixed, referenced to an index).
 *
 * @param <N>
 *          the notional type
 * @param <C>
 *          the coupon type
 */
public abstract class BondSecurity<N extends Payment, C extends Coupon> implements InstrumentDerivative {
  private final Annuity<N> _nominal;
  private final Annuity<C> _coupon;
  private final double _settlementTime;
  private final LegalEntity _issuer;
  private final String _discountingCurveName;

  /**
   * Constructs a generic bond with a legal entity that contains only issuer name information. This constructor hard-codes a bond curve name
   * and should not be used.
   *
   * @param nominal
   *          the notional payments, not null
   * @param coupon
   *          the bond coupons, not null
   * @param settlementTime
   *          the time (in years) to settlement date, not negative
   * @param discountingCurveName
   *          the name of the curve used for settlement amount discounting
   * @param issuer
   *          the bond issuer name, not null
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public BondSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final String discountingCurveName,
      final String issuer) {
    this(nominal, coupon, settlementTime, discountingCurveName, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Bond constructor from the bond nominal and coupon.
   *
   * @param nominal
   *          The notional payments.
   * @param coupon
   *          The bond coupons.
   * @param settlementTime
   *          The time (in years) to settlement date.
   * @param discountingCurveName
   *          The name of the curve used for settlement amount discounting.
   * @param issuer
   *          The bond issuer name.
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public BondSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final String discountingCurveName,
      final LegalEntity issuer) {
    ArgumentChecker.notNull(nominal, "Nominal");
    ArgumentChecker.notNull(coupon, "Coupon");
    ArgumentChecker.notNull(discountingCurveName, "Repo curve name");
    ArgumentChecker.notNull(issuer, "Issuer");
    _nominal = nominal;
    _coupon = coupon;
    _settlementTime = settlementTime;
    _discountingCurveName = discountingCurveName;
    _issuer = issuer;
  }

  /**
   * Bond constructor from the bond nominal and coupon.
   *
   * @param nominal
   *          The notional payments.
   * @param coupon
   *          The bond coupons.
   * @param settlementTime
   *          The time (in years) to settlement date.
   * @param issuer
   *          The bond issuer name.
   * @deprecated The issuer should be provided. Use {@link #BondSecurity(Annuity, Annuity, double, String, LegalEntity)}.
   */
  @Deprecated
  public BondSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final String issuer) {
    this(nominal, coupon, settlementTime, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Bond constructor from the bond nominal and coupon.
   *
   * @param nominal
   *          The notional payments.
   * @param coupon
   *          The bond coupons.
   * @param settlementTime
   *          The time (in years) to settlement date.
   * @param issuer
   *          The bond issuer.
   */
  public BondSecurity(final Annuity<N> nominal, final Annuity<C> coupon, final double settlementTime, final LegalEntity issuer) {
    _nominal = ArgumentChecker.notNull(nominal, "nominal");
    _coupon = ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.isTrue(nominal.getCurrency().equals(coupon.getCurrency()),
        "The nominal currency {} and coupon currency {} were different", nominal.getCurrency(), coupon.getCurrency());
    _issuer = ArgumentChecker.notNull(issuer, "issuer");
    _settlementTime = settlementTime;
    _discountingCurveName = null;
  }

  /**
   * Gets the nominal payments.
   *
   * @return The nominal payments.
   */
  public Annuity<N> getNominal() {
    return _nominal;
  }

  /**
   * Gets the coupons.
   *
   * @return The coupons.
   */
  public Annuity<C> getCoupon() {
    return _coupon;
  }

  /**
   * Gets the settlement time.
   *
   * @return The settlement time.
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * Gets the bond currency.
   *
   * @return The bond currency.
   */
  public Currency getCurrency() {
    return _nominal.getCurrency();
  }

  /**
   * Gets the name of the curve used for settlement amount discounting.
   *
   * @return The curve name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDerivative}s
   */
  @Deprecated
  public String getRepoCurveName() {
    if (_discountingCurveName == null) {
      throw new IllegalStateException("Repo curve name was not set");
    }
    return _discountingCurveName;
  }

  /**
   * Gets the issuer name.
   *
   * @return The issuer name.
   */
  public String getIssuer() {
    return _issuer.getShortName();
  }

  /**
   * Gets the issuer.
   *
   * @return The issuer
   */
  public LegalEntity getIssuerEntity() {
    return _issuer;
  }

  /**
   * Gets the bond issuer name and currency.
   *
   * @return The name/currency.
   * @deprecated This information is no longer used in the curve providers.
   */
  @Deprecated
  public Pair<String, Currency> getIssuerCcy() {
    return Pairs.of(getIssuer(), _nominal.getCurrency());
  }

  /**
   * Gets the name of the curve used for discounting.
   *
   * @return The curve name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDerivative}s
   */
  @Deprecated
  public String getDiscountingCurveName() {
    return getNominal().getDiscountCurve();
  }

  @Override
  public String toString() {
    String result = "Bond Security:";
    result += "\nNominal: " + _nominal.toString();
    result += "\nCoupon: " + _coupon.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_coupon == null ? 0 : _coupon.hashCode());
    result = prime * result + (_discountingCurveName == null ? 0 : _discountingCurveName.hashCode());
    result = prime * result + (_issuer == null ? 0 : _issuer.hashCode());
    result = prime * result + (_nominal == null ? 0 : _nominal.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_settlementTime);
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
    if (!(obj instanceof BondSecurity)) {
      return false;
    }
    final BondSecurity<?, ?> other = (BondSecurity<?, ?>) obj;
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_coupon, other._coupon)) {
      return false;
    }
    if (!ObjectUtils.equals(_nominal, other._nominal)) {
      return false;
    }
    if (!ObjectUtils.equals(_issuer, other._issuer)) {
      return false;
    }
    if (!ObjectUtils.equals(_discountingCurveName, other._discountingCurveName)) {
      return false;
    }
    return true;
  }

}
