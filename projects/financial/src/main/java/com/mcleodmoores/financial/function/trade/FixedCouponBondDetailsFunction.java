/**
 *
 */
package com.mcleodmoores.financial.function.trade;

import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
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
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class FixedCouponBondDetailsFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, FixedCouponBondCashFlows> {

  public FixedCouponBondDetailsFunction() {
    super(BOND_DETAILS, null);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!super.canApplyTo(context, target)) {
      return false;
    }
    return target.getTrade().getSecurity() instanceof BondSecurity;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final BondFixedTransactionDefinition definition =
        (BondFixedTransactionDefinition) BondAndBondFutureFunctionUtils.getDefinition(executionContext, target, now);
    final BondFixedTransaction derivative =
        (BondFixedTransaction) BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(executionContext, target, now, inputs);
    final IssuerProvider curves = (IssuerProvider) inputs.getValue(CURVE_BUNDLE);
    final FixedCouponBondDetailsProvider provider = new FixedCouponBondDetailsProvider(curves, now, definition);
    final ValueSpecification spec = new ValueSpecification(BOND_DETAILS, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, derivative.accept(FixedCouponBondDetailsCalculator.INSTANCE, provider)));
  }


}
