/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import java.util.Objects;

/**
 * Points up-front (PUF) is the current (as of April 2009) way of quoting CDSs. A CDS has a standardised coupon (premium) - which is either 100 or 500 bps in
 * North America (depending on the credit quality of the reference entity). An up front fee is then payable by the buyer of protection (i.e. the payer of the
 * premiums) - this fee can be negative (i.e. an amount is received by the protection buyer). PUF is quoted as a percentage of the notional. <br>
 * A zero hazard curve (or equivalent, e.g. the survival probability curve) can be implied from a set of PUF quotes (on the same name at different maturities)
 * by finding the curve that gives all the CDSs a clean present value equal to their PUF*Notional (the curve is not unique and will depend on other modeling
 * choices).
 */
public class PointsUpFront implements CDSQuoteConvention {

  private final double _coupon;
  private final double _puf;

  /**
   * @param coupon
   *          the CDS coupon.
   * @param puf
   *          the points-upfront quote
   */
  public PointsUpFront(final double coupon, final double puf) {
    _coupon = coupon;
    _puf = puf;
  }

  @Override
  public double getCoupon() {
    return _coupon;
  }

  /**
   * Gets the points-upfront.
   *
   * @return the points-upfront
   */
  public double getPointsUpFront() {
    return _puf;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_coupon, _puf);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PointsUpFront)) {
      return false;
    }
    final PointsUpFront other = (PointsUpFront) obj;
    return Double.doubleToLongBits(_coupon) == Double.doubleToLongBits(other._coupon) && Double.doubleToLongBits(_puf) == Double.doubleToLongBits(other._puf);
  }

  @Override
  public String toString() {
    return "PointsUpFront[coupon=" + _coupon + ", value=" + _puf + "]";
  }
}
