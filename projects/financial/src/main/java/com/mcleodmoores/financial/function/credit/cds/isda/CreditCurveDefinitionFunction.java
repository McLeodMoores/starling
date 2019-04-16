/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda;

import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;

import java.util.Collections;
import java.util.Set;

import com.mcleodmoores.financial.function.credit.configs.CreditCurveDefinition;
import com.mcleodmoores.financial.function.credit.source.ConfigDbCreditCurveDefinitionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.credit.CreditCurveIdentifier;

/**
 * A function that retrieves {@link CreditCurveDefinition}s from a config source. The function has a {@link CreditCurveIdentifier} target type, so in general
 * there will be one curve per RED code / seniority / currency / restructuring type (if available).
 */
public class CreditCurveDefinitionFunction extends AbstractFunction.NonCompiledInvoker {
  private ConfigDbCreditCurveDefinitionSource _source;

  @Override
  public void init(final FunctionCompilationContext context) {
    _source = ConfigDbCreditCurveDefinitionSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final CreditCurveDefinition definition = _source.getDefinition((CreditCurveIdentifier) target.getValue());
    return Collections
        .singleton(new ComputedValue(new ValueSpecification(CURVE_DEFINITION, target.toSpecification(), createValueProperties().get()), definition));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CREDIT_CURVE_IDENTIFIER;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(CURVE_DEFINITION, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

}
