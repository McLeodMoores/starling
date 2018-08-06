/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

/**
 * A visitor implementation used in unit tests.
 */
public class TestBondConventionVisitor implements YieldConventionTypeVisitor<String>, DataYieldConventionTypeVisitor<String, String> {

  /**
   * Static instance.
   */
  public static final TestBondConventionVisitor INSTANCE = new TestBondConventionVisitor();

  @Override
  public String visitUsStreet(final FixedCouponBondSecurity security, final String data) {
    return security.getYieldConventionType().name() + " " + data;
  }

  @Override
  public String visitUkDmo(final FixedCouponBondSecurity security, final String data) {
    return security.getYieldConventionType().name() + " " + data;
  }

  @Override
  public String visitFranceCompound(final FixedCouponBondSecurity security, final String data) {
    return security.getYieldConventionType().name() + " " + data;
  }

  @Override
  public String visitItalyTreasury(final FixedCouponBondSecurity security, final String data) {
    return security.getYieldConventionType().name() + " " + data;
  }

  @Override
  public String visitUsStreet(final FixedCouponBondSecurity security) {
    return security.getYieldConventionType().name();
  }

  @Override
  public String visitUkDmo(final FixedCouponBondSecurity security) {
    return security.getYieldConventionType().name();
  }

  @Override
  public String visitFranceCompound(final FixedCouponBondSecurity security) {
    return security.getYieldConventionType().name();
  }

  @Override
  public String visitItalyTreasury(final FixedCouponBondSecurity security) {
    return security.getYieldConventionType().name();
  }

  private TestBondConventionVisitor() {
  }
}
