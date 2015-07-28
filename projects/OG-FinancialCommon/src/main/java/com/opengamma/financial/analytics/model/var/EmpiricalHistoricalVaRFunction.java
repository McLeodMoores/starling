/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import static com.opengamma.financial.analytics.model.var.NormalHistoricalVaRFunction.DEFAULT_PNL_CONTRIBUTIONS;
import static com.opengamma.financial.analytics.model.var.NormalHistoricalVaRFunction.PROPERTY_VAR_DISTRIBUTION;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.var.EmpiricalDistributionVaRCalculator;
import com.opengamma.analytics.financial.var.EmpiricalDistributionVaRParameters;
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
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class EmpiricalHistoricalVaRFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * The name for the empirical historical VaR calculation method.
   */
  public static final String EMPIRICAL_VAR = "Empirical";

  private static final EmpiricalDistributionVaRCalculator CALCULATOR = new EmpiricalDistributionVaRCalculator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final String currency = getCurrency(inputs);
    final Object pnlSeriesObj = inputs.getValue(ValueRequirementNames.PNL_SERIES);
    if (pnlSeriesObj == null) {
      throw new OpenGammaRuntimeException("Could not get P&L series for " + target);
    }
    final DoubleTimeSeries<?> pnlSeries = (DoubleTimeSeries<?>) pnlSeriesObj;
    if (pnlSeries.isEmpty()) {
      throw new OpenGammaRuntimeException("P&L series for " + target + " was empty");
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> scheduleCalculatorNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Set<String> confidenceLevelNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CONFIDENCE_LEVEL);
    final Set<String> horizonNames = desiredValue.getConstraints().getValues(ValuePropertyNames.HORIZON);
    final EmpiricalDistributionVaRParameters parameters = getParameters(scheduleCalculatorNames, horizonNames, confidenceLevelNames);
    final double var = CALCULATOR.evaluate(parameters, pnlSeries).getVaRValue();
    final ValueProperties resultProperties = getResultProperties(currency, desiredValues.iterator().next());
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(ValueRequirementNames.HISTORICAL_VAR, target.toSpecification(), resultProperties), var));
  }

  private String getCurrency(final FunctionInputs inputs) {
    String currency = null;
    for (final ComputedValue value : inputs.getAllValues()) {
      currency = value.getSpecification().getProperty(ValuePropertyNames.CURRENCY);
      if (currency != null) {
        break;
      }
    }
    return currency;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.CONFIDENCE_LEVEL)
        .withAny(ValuePropertyNames.HORIZON)
        .withAny(ValuePropertyNames.AGGREGATION)
        .withAny(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS)
        .with(PROPERTY_VAR_DISTRIBUTION, EMPIRICAL_VAR).get();
    return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.HISTORICAL_VAR, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> samplingPeriodName = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriodName == null || samplingPeriodName.size() != 1) {
      return null;
    }
    final Set<String> scheduleCalculatorName = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleCalculatorName == null || scheduleCalculatorName.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctionName = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    if (samplingFunctionName == null || samplingFunctionName.size() != 1) {
      return null;
    }
    final Set<String> pnlContributionNames = constraints.getValues(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS);
    if (pnlContributionNames != null && pnlContributionNames.size() != 1) {
      return null;
    }
    String pnlContributionName = pnlContributionNames != null ? pnlContributionNames.iterator().next() : DEFAULT_PNL_CONTRIBUTIONS;
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName.iterator().next())
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next())
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, pnlContributionName); //TODO
    final Set<String> desiredCurrencyValues = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (desiredCurrencyValues == null || desiredCurrencyValues.isEmpty()) {
      properties.withAny(ValuePropertyNames.CURRENCY);
    } else {
      properties.with(ValuePropertyNames.CURRENCY, desiredCurrencyValues);
    }
    final Set<String> aggregationStyle = constraints.getValues(ValuePropertyNames.AGGREGATION);
    if (aggregationStyle != null) {
      if (aggregationStyle.isEmpty()) {
        properties.withOptional(ValuePropertyNames.AGGREGATION);
      } else {
        if (constraints.isOptional(ValuePropertyNames.AGGREGATION)) {
          properties.withOptional(ValuePropertyNames.AGGREGATION);
        }
        properties.with(ValuePropertyNames.AGGREGATION, aggregationStyle);
      }
    }
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    final String currency = input.getProperty(ValuePropertyNames.CURRENCY);
    if (currency == null) {
      return null;
    }
    final ValueProperties properties = getResultProperties(currency, input.getProperty(ValuePropertyNames.AGGREGATION), input.getProperty(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS));
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HISTORICAL_VAR, target.toSpecification(), properties));
  }

  private ValueProperties getResultProperties(final String currency, final String aggregationStyle, final String pnlContribution) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.CONFIDENCE_LEVEL)
        .withAny(ValuePropertyNames.HORIZON)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, pnlContribution)
        .with(PROPERTY_VAR_DISTRIBUTION, EMPIRICAL_VAR);
    if (aggregationStyle != null) {
      properties.with(ValuePropertyNames.AGGREGATION, aggregationStyle);
    }
    return properties.get();
  }

  private ValueProperties getResultProperties(final String currency, final ValueRequirement desiredValue) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
        .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
        .with(ValuePropertyNames.CONFIDENCE_LEVEL, desiredValue.getConstraint(ValuePropertyNames.CONFIDENCE_LEVEL))
        .with(ValuePropertyNames.HORIZON, desiredValue.getConstraint(ValuePropertyNames.HORIZON))
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, desiredValue.getConstraint(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS))
        .with(PROPERTY_VAR_DISTRIBUTION, EMPIRICAL_VAR);
    final String aggregationStyle = desiredValue.getConstraint(ValuePropertyNames.AGGREGATION);
    if (aggregationStyle != null) {
      properties.with(ValuePropertyNames.AGGREGATION, aggregationStyle);
    }
    return properties.get();
  }

  private EmpiricalDistributionVaRParameters getParameters(final Set<String> scheduleCalculatorNames, final Set<String> horizonNames, final Set<String> confidenceLevelNames) {
    if (scheduleCalculatorNames == null || scheduleCalculatorNames.isEmpty() || scheduleCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique schedule calculator name: " + scheduleCalculatorNames);
    }
    if (horizonNames == null || horizonNames.isEmpty() || horizonNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique horizon name: " + horizonNames);
    }
    if (confidenceLevelNames == null || confidenceLevelNames.isEmpty() || confidenceLevelNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique confidence level name: " + confidenceLevelNames);
    }
    return new EmpiricalDistributionVaRParameters(Double.valueOf(horizonNames.iterator().next()),
        VaRFunctionUtils.getBusinessDaysPerPeriod(scheduleCalculatorNames.iterator().next()), Double.valueOf(confidenceLevelNames.iterator().next()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION);
  }

}
