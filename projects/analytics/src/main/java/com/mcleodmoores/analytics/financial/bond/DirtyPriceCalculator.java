/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;

/**
 * Calculates the dirty price of a bond (i.e. the price of a bond plus the value of any accrued
 * interest) from the quoted yield of a bond, which depends on the yield convention type.
 */
public class DirtyPriceCalculator implements DataYieldConventionTypeVisitor<Double, Double> {

  @Override
  public Double visitUsStreet(final BondFixedSecurity bond, final Double yield) {
    final int nCoupons = bond.getNumberOfCoupons();
    final double nominal = bond.getNominal().getNthPayment(nCoupons - 1).getAmount();
    return nCoupons == 1 ? getFromMoneyMarketYield(bond, yield, nominal) : getFromRedemptionYield(bond, yield, nCoupons, nominal);
  }

  @Override
  public Double visitUkDmo(final BondFixedSecurity bond, final Double yield) {
    final int nCoupons = bond.getNumberOfCoupons();
    return getFromRedemptionYield(bond, yield, nCoupons, bond.getNominal().getNthPayment(nCoupons - 1).getAmount());
  }

  @Override
  public Double visitFranceCompound(final BondFixedSecurity bond, final Double yield) {
    final int nCoupons = bond.getNumberOfCoupons();
    final double nominal = bond.getNominal().getNthPayment(nCoupons - 1).getAmount();
    if (nCoupons == 1) {
      return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) * Math.pow(1 + yield / bond.getCouponPerYear(), -bond.getFactorToNextCoupon()) / nominal;
    }
    return getFromRedemptionYield(bond, yield, nCoupons, nominal);
  }

  @Override
  public Double visitItalyTreasury(final BondFixedSecurity bond, final Double yield) {
    final double semiAnnualYield = 2 * Math.sqrt(1 + yield - 1);
    final int nCoupons = bond.getNumberOfCoupons();
    return getFromRedemptionYield(bond, semiAnnualYield, nCoupons, bond.getNominal().getNthPayment(nCoupons - 1).getAmount());
  }

  private static double getFromMoneyMarketYield(final BondFixedSecurity bond, final double yield, final double nominal) {
    return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) / (1 + bond.getFactorToNextCoupon() * yield / bond.getCouponPerYear()) / nominal;
  }

  private static double getFromRedemptionYield(final BondFixedSecurity bond, final double yield, final int nCoupons, final double nominal) {
    final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
    double pvAtFirstCoupon = 0;
    for (int i = 0; i < nCoupons; i++) {
      pvAtFirstCoupon += bond.getCoupon().getNthPayment(i).getAmount() / Math.pow(factorOnPeriod, i);
    }
    pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nCoupons - 1);
    return pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon()) / nominal;
  }
}
