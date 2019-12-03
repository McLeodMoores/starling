/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import java.util.Objects;

/**
 * Par spread is the old (i.e. pre-April 2009) way of quoting CDSs. A CDS would be constructed to have an initial fair value of zero; the par-spread is the
 * value of the coupon (premium) on the premium leg that makes this so. <br>
 * A zero hazard curve (or equivalent, e.g. the survival probability curve) can be implied from a set of par spread quotes (on the same name at different
 * maturities) by finding the curve that gives all the CDSs a PV of zero (the curve is not unique and will depend on other modeling choices).
 */
public class ParSpread implements CDSQuoteConvention {

  private final double _parSpread;

  /**
   * @param parSpread
   *          the par spread
   */
  public ParSpread(final double parSpread) {
    _parSpread = parSpread;
  }

  @Override
  public double getCoupon() {
    return _parSpread;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_parSpread);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ParSpread)) {
      return false;
    }
    final ParSpread other = (ParSpread) obj;
    return Double.doubleToLongBits(_parSpread) == Double.doubleToLongBits(other._parSpread);
  }

  @Override
  public String toString() {
    return "ParSpread[value=" + _parSpread + "]";
  }
}
