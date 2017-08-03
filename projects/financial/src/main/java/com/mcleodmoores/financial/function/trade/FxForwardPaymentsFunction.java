/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.trade;

import static com.opengamma.engine.value.ValueRequirementNames.PAY_AMOUNT;
import static com.opengamma.engine.value.ValueRequirementNames.RECEIVE_AMOUNT;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Returns the pay and receive amounts for FX forward and NDFs.
 */
public class FxForwardPaymentsFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
    final CurrencyAmount pay =
        CurrencyAmount.of(security.accept(ForexVisitors.getPayCurrencyVisitor()), security.accept(ForexVisitors.getPayAmountVisitor()));
    final CurrencyAmount receive =
        CurrencyAmount.of(security.accept(ForexVisitors.getReceiveCurrencyVisitor()), security.accept(ForexVisitors.getReceiveAmountVisitor()));
    final ValueSpecification paySpec = new ValueSpecification(PAY_AMOUNT, target.toSpecification(), createValueProperties().get());
    final ValueSpecification receiveSpec = new ValueSpecification(RECEIVE_AMOUNT, target.toSpecification(), createValueProperties().get());
    final Set<ComputedValue> results = new HashSet<>();
    results.add(new ComputedValue(paySpec, pay));
    results.add(new ComputedValue(receiveSpec, receive));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<>();
    results.add(new ValueSpecification(PAY_AMOUNT, target.toSpecification(), createValueProperties().get()));
    results.add(new ValueSpecification(RECEIVE_AMOUNT, target.toSpecification(), createValueProperties().get()));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }


}
