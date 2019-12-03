/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
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
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalValuationFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Calculates a PnL series by performing a full historical valuation over the required period.
 */
public class HistoricalValuationPnLFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties outputConstraints = desiredValue.getConstraints();
    final Set<String> startDates = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (startDates == null || startDates.size() != 1) {
      return null;
    }
    final Set<String> endDates = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if (endDates == null || endDates.size() != 1) {
      return null;
    }
    Set<String> desiredCurrencies = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (desiredCurrencies == null || desiredCurrencies.isEmpty()) {
      final Collection<Currency> targetCurrencies = FinancialSecurityUtils.getCurrencies(target.getPosition().getSecurity(), context.getSecuritySource());
      // REVIEW jonathan 2013-03-12 -- should we pass through all the currencies and see what it wants to produce?
      desiredCurrencies = ImmutableSet.of(Iterables.get(targetCurrencies, 0).getCode());
    }
    final String pnlSeriesStartDateProperty = ValueRequirementNames.PNL_SERIES + "_" + HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY;
    final ValueProperties.Builder requirementConstraints = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.CURRENCY)
        .with(HistoricalValuationFunction.PASSTHROUGH_PREFIX + ValuePropertyNames.CURRENCY, desiredCurrencies)
        .withoutAny(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS)
        .withoutAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withoutAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withoutAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(pnlSeriesStartDateProperty, outputConstraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY))
        .withOptional(pnlSeriesStartDateProperty);

    final String startDate = getPriceSeriesStart(outputConstraints);
    if (startDate == null) {
      return null;
    }
    requirementConstraints.with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, startDate);
    removeTransformationProperties(requirementConstraints);
    return ImmutableSet.of(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, target.toSpecification(), requirementConstraints.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    final Entry<ValueSpecification, ValueRequirement> tsInput = Iterables.getOnlyElement(inputs.entrySet());
    final ValueSpecification tsSpec = tsInput.getKey();
    final String pnlStartDate = tsInput.getValue()
        .getConstraint(ValueRequirementNames.PNL_SERIES + "_" + HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    final ValueSpecification valueSpec = getResultSpec(target, tsSpec.getProperties(), pnlStartDate, null);
    return ImmutableSet.of(valueSpec);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ComputedValue priceTsComputedValue = inputs.getComputedValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final LocalDateDoubleTimeSeries priceTs = (LocalDateDoubleTimeSeries) priceTsComputedValue.getValue();
    if (priceTs.isEmpty()) {
      return null;
    }
    final DateDoubleTimeSeries<?> pnlTs = calculatePnlSeries(priceTs, executionContext, desiredValue);
    final String pnlStartDate = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    final ValueSpecification valueSpec = getResultSpec(target, priceTsComputedValue.getSpecification().getProperties(), pnlStartDate, desiredValue);
    return ImmutableSet.of(new ComputedValue(valueSpec, pnlTs));
  }

  // -------------------------------------------------------------------------
  protected String getPriceSeriesStart(final ValueProperties outputConstraints) {
    final Set<String> samplingPeriodValues = outputConstraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriodValues != null && !samplingPeriodValues.isEmpty()) {
      return DateConstraint.VALUATION_TIME.minus(samplingPeriodValues.iterator().next()).toString();
    }
    final Set<String> startDates = outputConstraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if (startDates != null && !startDates.isEmpty()) {
      return Iterables.getOnlyElement(startDates);
    }
    return null;
  }

  protected void removeTransformationProperties(final ValueProperties.Builder builder) {
    builder.withoutAny(ValuePropertyNames.TRANSFORMATION_METHOD);
  }

  protected void addTransformationProperties(final ValueProperties.Builder builder, final ValueRequirement desiredValue) {
    builder.with(ValuePropertyNames.TRANSFORMATION_METHOD, "None");
  }

  protected DateDoubleTimeSeries<?> calculatePnlSeries(final LocalDateDoubleTimeSeries priceSeries, final FunctionExecutionContext executionContext,
      final ValueRequirement desiredValue) {
    final int pnlVectorSize = priceSeries.size() - 1;
    final double[] pnlValues = new double[pnlVectorSize];
    for (int i = 0; i < pnlVectorSize; i++) {
      pnlValues[i] = priceSeries.getValueAtIndex(i + 1) - priceSeries.getValueAtIndex(i);
    }
    final int[] pnlDates = new int[priceSeries.size() - 1];
    System.arraycopy(priceSeries.timesArrayFast(), 1, pnlDates, 0, pnlDates.length);
    final LocalDateDoubleTimeSeries pnlTs = ImmutableLocalDateDoubleTimeSeries.of(pnlDates, pnlValues);
    return pnlTs;
  }

  // -------------------------------------------------------------------------
  private ValueSpecification getResultSpec(final ComputationTarget target, final ValueProperties priceTsProperties, final String pnlStartDate,
      final ValueRequirement desiredValue) {
    final Set<String> currencies = priceTsProperties.getValues(HistoricalValuationFunction.PASSTHROUGH_PREFIX + ValuePropertyNames.CURRENCY);
    if (currencies == null || currencies.size() != 1) {
      throw new OpenGammaRuntimeException("Expected a single currency for historical valuation series but got " + currencies);
    }
    final ValueProperties.Builder builder = priceTsProperties.copy()
        .withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .with(ValuePropertyNames.CURRENCY, currencies)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Full")
        .withoutAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, pnlStartDate)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, ScheduleCalculatorFactory.DAILY)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, TimeSeriesSamplingFunctionFactory.NO_PADDING);

    addTransformationProperties(builder, desiredValue);
    return new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), builder.get());
  }

}
