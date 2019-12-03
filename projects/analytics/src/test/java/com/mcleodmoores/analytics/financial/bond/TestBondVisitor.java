/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import com.mcleodmoores.analytics.financial.instruments.BondSecurityVisitor;
import com.mcleodmoores.analytics.financial.instruments.DataBondSecurityVisitor;

/**
 * A visitor implementation used in unit tests.
 */
public class TestBondVisitor implements BondSecurityVisitor<Double>, DataBondSecurityVisitor<Integer, Double> {

  /**
   * Static instance.
   */
  public static final TestBondVisitor INSTANCE = new TestBondVisitor();

  @Override
  public Double visitFixedCouponBond(final FixedCouponBondSecurity bond, final Integer data) {
    return bond.getAccruedInterest() / data;
  }

  @Override
  public Double visitFixedCouponBond(final FixedCouponBondSecurity bond) {
    return -bond.getAccruedInterest();
  }

  private TestBondVisitor() {
  }
}
