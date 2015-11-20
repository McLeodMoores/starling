/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate Macaulay duration from yield.
 */
public final class MacaulayDurationFromYieldCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final MacaulayDurationFromYieldCalculator s_instance = new MacaulayDurationFromYieldCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static MacaulayDurationFromYieldCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private MacaulayDurationFromYieldCalculator() {
  }

  /**
   * The method used for different instruments.
   */
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  @Override
  public Double visitBillTransaction(final BillTransaction bill, final Double yield) {
    ArgumentChecker.notNull(bill, "bill");
    return bill.getBillPurchased().getEndTime();
  }

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final Double yield) {
    ArgumentChecker.notNull(bill, "bill");
    return bill.getEndTime();
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    return METHOD_BOND_SECURITY.macaulayDurationFromYield(bond, yield);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final Double yield) {
    return METHOD_BOND_SECURITY.macaulayDurationFromYield(bond.getBondTransaction(), yield);
  }
}
