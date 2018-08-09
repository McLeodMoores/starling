/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;

/**
 *
 */
public class LastCouponConvexityCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    return getSimpleYield(bond, yield);
  }

  private static double getSimpleYield(final BondFixedSecurity bond, final Double yield) {
    final double timeToPay = bond.getFactorToNextCoupon() / bond.getCouponPerYear();
    final double discount = 1 + timeToPay * yield;
    return 2 * timeToPay * timeToPay / discount / discount;
  }

  private static double getFranceCompounding(final BondFixedSecurity bond, final Double yield) {
    final double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) / nominal * Math.pow(1.0 + yield / bond.getCouponPerYear(), -bond.getFactorToNextCoupon());
  }
}
