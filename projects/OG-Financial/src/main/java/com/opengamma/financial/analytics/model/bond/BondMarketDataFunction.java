/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 *
 */
public abstract class BondMarketDataFunction extends NonCompiledInvoker {

  private final String _requirementName;

  public BondMarketDataFunction(final String requirementName) {
    Validate.notNull(requirementName, "requirementName");
    _requirementName = requirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final BondSecurity security;
    if (target.getType() == ComputationTargetType.TRADE) {
      security = (BondSecurity) target.getTrade().getSecurity();
    } else if (target.getSecurity() instanceof BondSecurity) {
      security = (BondSecurity) target.getSecurity();
    } else {
      throw new OpenGammaRuntimeException("Unexpected target type " + target.getType());
    }
    final Object value = inputs.getValue(_requirementName);
    if (value == null) {
      throw new OpenGammaRuntimeException("Could not get " + _requirementName);
    }
    return getComputedValues(executionContext, (Double) value, security, target.toSpecification());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (target.getType() == ComputationTargetType.TRADE) {
      return Collections.singleton(new ValueRequirement(_requirementName, ComputationTargetType.SECURITY, target.getTrade().getSecurity().getUniqueId()));
    }
    return Collections.singleton(new ValueRequirement(_requirementName, target.toSpecification()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.BOND_SECURITY.or(ComputationTargetType.TRADE);
  }

  protected abstract Set<ComputedValue> getComputedValues(final FunctionExecutionContext context, final double value, final BondSecurity security, final ComputationTargetSpecification target);

}
