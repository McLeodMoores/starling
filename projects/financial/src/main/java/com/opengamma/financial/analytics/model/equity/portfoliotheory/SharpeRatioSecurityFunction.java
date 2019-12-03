/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * 
 */
public class SharpeRatioSecurityFunction extends SharpeRatioFunction {

  public SharpeRatioSecurityFunction(final String resolutionKey) {
    super(resolutionKey);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getSecurity() instanceof EquitySecurity;
  }
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    return super.execute(executionContext, inputs, target, desiredValues);
  }
}
