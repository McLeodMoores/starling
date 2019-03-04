/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.HistoricalShockMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;

/**
 * Runs an execution sequence over a series of historical dates, returning the results as time-series.
 */
public class HistoricalViewEvaluationFunction extends ViewEvaluationFunction<HistoricalViewEvaluationTarget, HistoricalViewEvaluationResultBuilder> {

  /**
   * Name of a property on an output value that contains the market data used rather than the computed results.
   */
  public static final String MARKET_DATA_PROPERTY_NAME = "Type";

  /**
   * Value taken by the {@link #MARKET_DATA_PROPERTY_NAME} property when the output contains market data used rather than the computed results.
   */
  public static final String MARKET_DATA_PROPERTY_VALUE = "MarketData";

  public HistoricalViewEvaluationFunction() {
    super(ValueRequirementNames.HISTORICAL_TIME_SERIES, HistoricalViewEvaluationTarget.class);
  }

  protected ValueSpecification getMarketDataResultSpec(final ComputationTargetSpecification targetSpec) {
    return new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES, targetSpec, createValueProperties().with(MARKET_DATA_PROPERTY_NAME, MARKET_DATA_PROPERTY_VALUE).get());
  }

  // CompiledFunctionDefinition

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = super.getResults(context, target);
    results.add(getMarketDataResultSpec(target.toSpecification()));
    return results;
  }

  // ViewEvaluationFunction

  @Override
  protected ViewCycleExecutionOptions getDefaultCycleOptions(final FunctionExecutionContext context) {
    return ViewCycleExecutionOptions.builder().setValuationTime(context.getValuationTime()).setResolverVersionCorrection(context.getComputationTargetResolver().getVersionCorrection()).create();
  }

  @Override
  protected EnumSet<ViewExecutionFlags> getViewExecutionFlags(final Set<ValueRequirement> desiredValues) {
    final EnumSet<ViewExecutionFlags> flags = super.getViewExecutionFlags(desiredValues);
    for (final ValueRequirement desiredValue : desiredValues) {
      if (!MARKET_DATA_PROPERTY_VALUE.equals(desiredValue.getConstraint(MARKET_DATA_PROPERTY_NAME))) {
        return flags;
      }
    }
    // No result values were requested, so don't bother executing the cycles
    flags.add(ViewExecutionFlags.FETCH_MARKET_DATA_ONLY);
    return flags;
  }

  @Override
  protected HistoricalViewEvaluationResultBuilder createResultBuilder(final ViewEvaluationTarget target, final Set<ValueRequirement> desiredValues) {
    boolean includeMarketData = false;
    for (final ValueRequirement desiredValue : desiredValues) {
      if (MARKET_DATA_PROPERTY_VALUE.equals(desiredValue.getConstraint(MARKET_DATA_PROPERTY_NAME))) {
        includeMarketData = true;
        break;
      }
    }
    return new HistoricalViewEvaluationResultBuilder(target.getViewDefinition(), includeMarketData);
  }

  @Override
  protected void store(final ViewComputationResultModel results, final HistoricalViewEvaluationResultBuilder resultBuilder) {
    resultBuilder.store(getResultsDate(results.getViewCycleExecutionOptions()), results);
  }

  @Override
  protected void store(final CompiledViewDefinition compiledViewDefinition, final HistoricalViewEvaluationResultBuilder resultBuilder) {
    resultBuilder.store(compiledViewDefinition);
  }

  @Override
  protected Set<ComputedValue> buildResults(final ComputationTarget target, final HistoricalViewEvaluationResultBuilder resultBuilder) {
    final Map<String, HistoricalViewEvaluationResult> viewResults = resultBuilder.getResults();
    final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(viewResults.size() + 1);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final Map.Entry<String, HistoricalViewEvaluationResult> viewResult : viewResults.entrySet()) {
      final String calcConfigName = viewResult.getKey();
      final HistoricalViewEvaluationResult value = viewResult.getValue();
      results.add(new ComputedValue(getResultSpec(calcConfigName, targetSpec), value));
    }
    final HistoricalViewEvaluationMarketData viewMarketData = resultBuilder.getMarketData();
    if (viewMarketData != null) {
      results.add(new ComputedValue(getMarketDataResultSpec(targetSpec), viewMarketData));
    }
    return results;
  }

  //-------------------------------------------------------------------------
  private static LocalDate getResultsDate(final ViewCycleExecutionOptions cycleExecutionOptions) {
    // NOTE jonathan 2013-02-28 -- could imagine using constraints
    final List<MarketDataSpecification> marketDataSpecifications = cycleExecutionOptions.getMarketDataSpecifications();
    if (marketDataSpecifications.size() != 1) {
      throw new OpenGammaRuntimeException("Expected cycle execution options to contain exactly 1 market data specification but found " +
          marketDataSpecifications.size() + ": " + cycleExecutionOptions);
    }
    final MarketDataSpecification marketDataSpec = marketDataSpecifications.get(0);
    if (marketDataSpec instanceof FixedHistoricalMarketDataSpecification) {
      return ((FixedHistoricalMarketDataSpecification) marketDataSpec).getSnapshotDate();
    } else if (marketDataSpec instanceof HistoricalShockMarketDataSpecification) {
      final MarketDataSpecification spec2 = ((HistoricalShockMarketDataSpecification) marketDataSpec).getHistoricalSpecification2();
      if (spec2 instanceof FixedHistoricalMarketDataSpecification) {
        return ((FixedHistoricalMarketDataSpecification) spec2).getSnapshotDate();
      }
      throw new OpenGammaRuntimeException("Unsupported inner market data specification: " + spec2);
    } else {
      throw new OpenGammaRuntimeException("Unsupported market data specification: " + marketDataSpec);
    }
  }

}
