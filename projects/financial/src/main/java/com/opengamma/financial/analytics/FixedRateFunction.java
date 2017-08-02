/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics;

import static com.opengamma.engine.value.ValueRequirementNames.FIXED_RATE;

import java.util.Collections;
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
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Function that returns the fixed rate for a trade, if available.
 */
public class FixedRateFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
    final double fixedRate = security.accept(FixedRateVisitor.INSTANCE);
    final ValueSpecification spec = new ValueSpecification(FIXED_RATE, target.toSpecification(), createValueProperties().get());
    return Collections.singleton(new ComputedValue(spec, fixedRate));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof SwapSecurity || security instanceof FRASecurity
        || security instanceof ForwardRateAgreementSecurity || security instanceof CashSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(FIXED_RATE, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

  private static final class FixedRateVisitor extends FinancialSecurityVisitorAdapter<Double> {
    public static final FixedRateVisitor INSTANCE = new FixedRateVisitor();

    private FixedRateVisitor() {
    }

    @Override
    public Double visitSwapSecurity(final SwapSecurity security) {
      final SwapLeg payLeg = security.getPayLeg();
      if (payLeg instanceof FixedInterestRateLeg) {
        return ((FixedInterestRateLeg) payLeg).getRate();
      }
      final SwapLeg receiveLeg = security.getReceiveLeg();
      if (receiveLeg instanceof FixedInterestRateLeg) {
        return ((FixedInterestRateLeg) receiveLeg).getRate();
      }
      return null;
    }

    @Override
    public Double visitFRASecurity(final FRASecurity security) {
      return security.getRate();
    }

    @Override
    public Double visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
      return security.getRate();
    }

    @Override
    public Double visitCashSecurity(final CashSecurity security) {
      return security.getRate();
    }
  }

}
