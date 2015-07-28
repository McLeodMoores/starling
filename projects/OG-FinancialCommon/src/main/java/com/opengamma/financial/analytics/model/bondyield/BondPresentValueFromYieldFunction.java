/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondyield;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProvider;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present of a bond from the clean price and a curve bundle.
 */
public class BondPresentValueFromYieldFunction extends BondFromYieldAndCurvesFunction {
  /** Prices bonds */
  private static final BondTransactionDiscountingMethod BOND_CALCULATOR = BondTransactionDiscountingMethod.getInstance();
  /** Prices bills */
  private static final BillTransactionDiscountingMethod BILL_CALCULATOR = BillTransactionDiscountingMethod.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PRESENT_VALUE}.
   */
  public BondPresentValueFromYieldFunction() {
    super(PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final FunctionInputs inputs, final InstrumentDerivative derivative, final IssuerProvider issuerCurves,
      final double yield, final ValueSpecification spec) {
    final String expectedCurrency = spec.getProperty(CURRENCY);
    final MultipleCurrencyAmount pv;
    if (derivative instanceof BondFixedTransaction) {
      pv = BOND_CALCULATOR.presentValueFromYield((BondFixedTransaction) derivative, issuerCurves, yield);
    } else if (derivative instanceof BillTransaction) {
      pv = BILL_CALCULATOR.presentValueFromYield((BillTransaction) derivative, yield, issuerCurves);
    } else {
      throw new OpenGammaRuntimeException("Unhandled instrument type " + derivative.getClass());
    }
    if (pv.size() != 1 || !expectedCurrency.equals(pv.getCurrencyAmounts()[0].getCurrency().getCode())) {
      throw new OpenGammaRuntimeException("Expecting a single result in " + expectedCurrency);
    }
    return Collections.singleton(new ComputedValue(spec, pv.getCurrencyAmounts()[0].getAmount()));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    return super.getResultProperties(target)
        .with(CURRENCY, currency);
  }

}
