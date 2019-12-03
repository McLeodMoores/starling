/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Produces a value in terms of a {@link ValuePropertyNames#SAMPLING_PERIOD} from a value in terms of
 * {@link HistoricalTimeSeriesFunctionUtils#START_DATE_PROPERTY} and {@link HistoricalTimeSeriesFunctionUtils#END_DATE_PROPERTY}.
 */
public class PnLPeriodTranslationFunction extends AbstractFunction.NonCompiledInvoker {

  private static final ComputationTargetType TYPE = ComputationTargetType.POSITION.or(ComputationTargetType.PORTFOLIO_NODE);

  private final String _valueRequirementName;

  public PnLPeriodTranslationFunction(final String valueRequirementName) {
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return TYPE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(_valueRequirementName, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> samplingPeriods = desiredValue.getConstraints().getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriods == null || samplingPeriods.size() != 1) {
      return null;
    }
    final String samplingPeriod = Iterables.getOnlyElement(samplingPeriods);
    final DateConstraint start = DateConstraint.VALUATION_TIME.minus(samplingPeriod);
    final ValueProperties inputConstraints = desiredValue.getConstraints().copy()
        .withOptional(ValuePropertyNames.SAMPLING_PERIOD)
        .with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, start.toString())
        .with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, DateConstraint.VALUATION_TIME.toString())
        .get();
    return ImmutableSet.of(new ValueRequirement(_valueRequirementName, target.toSpecification(), inputConstraints));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    final Map.Entry<ValueSpecification, ValueRequirement> input = Iterables.getOnlyElement(inputs.entrySet());
    final String samplingPeriod = input.getValue().getConstraint(ValuePropertyNames.SAMPLING_PERIOD);
    final ValueProperties outputProperties = input.getKey().getProperties().copy()
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriod)
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    return ImmutableSet.of(new ValueSpecification(_valueRequirementName, target.toSpecification(), outputProperties));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Object result = inputs.getValue(_valueRequirementName);
    return ImmutableSet.of(new ComputedValue(new ValueSpecification(_valueRequirementName, target.toSpecification(), desiredValue.getConstraints()), result));
  }

}
