/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
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
 * Calculate Macaulay duration from the curves.
 */
public final class MacaulayDurationFromCurvesCalculator extends InstrumentDerivativeVisitorAdapter<IssuerProviderInterface, Double> {

  /**
   * The calculator instance.
   */
  private static final MacaulayDurationFromCurvesCalculator INSTANCE = new MacaulayDurationFromCurvesCalculator();

  /**
   * Return the calculator instance.
   *
   * @return The instance.
   */
  public static MacaulayDurationFromCurvesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private MacaulayDurationFromCurvesCalculator() {
  }

  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final IssuerProviderInterface marketData) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(marketData, "marketData");
    return BondSecurityDiscountingMethod.getInstance().macaulayDurationFromCurves(bond, marketData);
  }

  @Override
  public Double visitBondFixedTransaction(final BondFixedTransaction bond, final IssuerProviderInterface marketData) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(marketData, "marketData");
    return BondSecurityDiscountingMethod.getInstance().macaulayDurationFromCurves(bond.getBondTransaction(), marketData);
  }

  @Override
  public Double visitBillSecurity(final BillSecurity bill, final IssuerProviderInterface marketData) {
    ArgumentChecker.notNull(bill, "bill");
    ArgumentChecker.notNull(marketData, "marketData");
    return BillSecurityDiscountingMethod.getInstance().macaulayDurationFromCurves(bill, marketData);
  }

  @Override
  public Double visitBillTransaction(final BillTransaction bill, final IssuerProviderInterface marketData) {
    ArgumentChecker.notNull(bill, "bill");
    ArgumentChecker.notNull(marketData, "marketData");
    return BillSecurityDiscountingMethod.getInstance().macaulayDurationFromCurves(bill.getBillPurchased(), marketData);
  }
}
