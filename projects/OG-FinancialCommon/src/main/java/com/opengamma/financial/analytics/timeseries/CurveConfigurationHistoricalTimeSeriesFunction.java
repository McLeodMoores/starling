/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function to source time series data for each of the instruments in a {@link CurveSpecification} from a {@link HistoricalTimeSeriesSource} attached to the execution context. These time series are
 * used to convert {@link InstrumentDefinition}s into the {@link InstrumentDerivative}s used in pricing and curve construction.
 */
public class CurveConfigurationHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(CurveConfigurationHistoricalTimeSeriesFunction.class);

  private ConfigDBCurveConstructionConfigurationSource _curveConstructionConfigurationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues)
    throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    for (final ComputedValue value : inputs.getAllValues()) {
      // have only asked for time series bundles
      final HistoricalTimeSeriesBundle tsForCurve = (HistoricalTimeSeriesBundle) value.getValue();
      final Set<String> fields = tsForCurve.getFields();
      for (final String field : fields) {
        final Map<ExternalIdBundle, HistoricalTimeSeries> entry = tsForCurve.getEntryForField(field);
        for (final Map.Entry<ExternalIdBundle, HistoricalTimeSeries> e : entry.entrySet()) {
          bundle.add(field, e.getKey(), e.getValue());
        }
      }
    }
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES,
        target.toSpecification(), desiredValue.getConstraints().copy().get());
    return Collections.singleton(new ComputedValue(spec, bundle));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, target.toSpecification(),
        properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curveConstructionConfigs = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG);
    if (curveConstructionConfigs == null || curveConstructionConfigs.size() != 1) {
      return null;
    }
    final String curveConstructionConfig = Iterables.getOnlyElement(curveConstructionConfigs);
    final CurveConstructionConfiguration constructionConfig = _curveConstructionConfigurationSource.getCurveConstructionConfiguration(curveConstructionConfig);
    final Set<ValueRequirement> requirements = new HashSet<>();
    final List<CurveGroupConfiguration> groups = constructionConfig.getCurveGroups();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final CurveGroupConfiguration group : groups) {
      //TODO do we want to put information in about whether or not to use fixing time series?
      //TODO do we want to exclude node types that definitely don't need fixing data?
      for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        final String curveName = entry.getKey();
        final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
        requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_HISTORICAL_TIME_SERIES, targetSpec, properties));
      }
    }
    return requirements;
  }

}
