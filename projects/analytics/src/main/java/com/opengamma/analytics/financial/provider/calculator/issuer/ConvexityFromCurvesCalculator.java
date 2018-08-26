/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculate convexity from the curves.
 */
public final class ConvexityFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, Double> {

  /**
   * The calculator instance.
   */
  private static final ConvexityFromCurvesCalculator INSTANCE = new ConvexityFromCurvesCalculator();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static ConvexityFromCurvesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ConvexityFromCurvesCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final IssuerProviderInterface marketData) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(marketData, "marketData");
    return BondSecurityDiscountingMethod.getInstance().convexityFromCurves(bond, marketData) / 100;
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final IssuerProviderInterface marketData) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(marketData, "marketData");
    return BondSecurityDiscountingMethod.getInstance().convexityFromCurves(bond.getBondTransaction(), marketData) / 100;
  }

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final IssuerProviderInterface marketData) {
    ArgumentChecker.notNull(bill, "bill");
    ArgumentChecker.notNull(marketData, "marketData");
    return BillSecurityDiscountingMethod.getInstance().convexityFromCurves(bill, marketData) / 100;
  }

  @Override
  public Double visitBillTransaction(final BillTransaction bill, final IssuerProviderInterface marketData) {
    ArgumentChecker.notNull(bill, "bill");
    ArgumentChecker.notNull(marketData, "marketData");
    return BillSecurityDiscountingMethod.getInstance().convexityFromCurves(bill.getBillPurchased(), marketData) / 100;
  }
}
