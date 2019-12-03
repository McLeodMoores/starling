/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondCapitalIndexedSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.util.ArgumentChecker;

/**
 * Bond accrued interest from the conventional yield-to-maturity function.
 */
public final class AccruedInterestFromYieldCalculator extends InstrumentDerivativeVisitorAdapter<Double, Double> {
  /** A singleton instance */
  private static final AccruedInterestFromYieldCalculator INSTANCE = new AccruedInterestFromYieldCalculator();
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_INFLATION_BOND_SECURITY = BondCapitalIndexedSecurityDiscountingMethod.getInstance();

  /**
   * Gets a static instance.
   * @return The instance
   */
  public static AccruedInterestFromYieldCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private AccruedInterestFromYieldCalculator() {
  }

  @Override
  public Double visitBillTransaction(final BillTransaction bill, final Double yield) {
    ArgumentChecker.notNull(bill, "bill");
    ArgumentChecker.notNull(yield, "yield");
    return 0.;
  }

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final Double yield) {
    ArgumentChecker.notNull(bill, "bill");
    ArgumentChecker.notNull(yield, "yield");
    return 0.;
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    return BondSecurityDiscountingMethod.getInstance().accruedInterestFromYield(bond, yield);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");
    return BondSecurityDiscountingMethod.getInstance().accruedInterestFromYield(bond.getBondTransaction(), yield);
  }

  @Override
  public Double visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final Double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(yield, "yield");

    final BondCapitalIndexedSecurity<?> bondSecurity = bond.getBondStandard();
    return METHOD_INFLATION_BOND_SECURITY.accruedInterestFromCleanYield(bondSecurity, yield);
  }
}
