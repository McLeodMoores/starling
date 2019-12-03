/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * This function simply returns the market value for any cash equity security.
 * Produces two aliases - {@link ValueRequirementNames#PRESENT_VALUE} and {@link ValueRequirementNames#FAIR_VALUE},
 * both equal to the {@link MarketDataRequirementNames#MARKET_VALUE} requirement.
 */
public class StandardEquityModelFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final EquitySecurity equity = (EquitySecurity) target.getSecurity();
    final double price = (Double) inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    final Set<ComputedValue> result = new HashSet<>();
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURRENCY, equity.getCurrency().getCode()).get();
    result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.FAIR_VALUE, target.toSpecification(), properties), price));
    result.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties), price));
    return result;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final EquitySecurity equity = (EquitySecurity) target.getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, equity.getUniqueId()));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final EquitySecurity equity = (EquitySecurity) target.getSecurity();
    final Set<ValueSpecification> result = new HashSet<>();
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURRENCY, equity.getCurrency().getCode()).get();
    result.add(new ValueSpecification(ValueRequirementNames.FAIR_VALUE, target.toSpecification(), properties));
    result.add(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties));
    return result;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_SECURITY;
  }

}
