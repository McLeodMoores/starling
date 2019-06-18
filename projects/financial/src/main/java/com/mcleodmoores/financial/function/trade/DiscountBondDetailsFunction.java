/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.trade;

import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.financial.analytics.model.bondcurves.BondAndBondFutureFromCurvesFunction;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class DiscountBondDetailsFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, FixedCouponBondCashFlows> {

  public DiscountBondDetailsFunction() {
    super(BOND_DETAILS, null);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!super.canApplyTo(context, target)) {
      return false;
    }
    return target.getTrade().getSecurity() instanceof BillSecurity;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final BillTransactionDefinition definition = (BillTransactionDefinition) BondAndBondFutureFunctionUtils.getDefinition(executionContext, target, now);
    final BillTransaction derivative = (BillTransaction) BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(executionContext, target, now, inputs);
    final IssuerProvider curves = (IssuerProvider) inputs.getValue(CURVE_BUNDLE);
    final DiscountBondDetailsProvider provider = new DiscountBondDetailsProvider(curves, now, definition);
    final ValueSpecification spec = new ValueSpecification(BOND_DETAILS, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, derivative.accept(DiscountBondDetailsCalculator.INSTANCE, provider)));
  }

}
