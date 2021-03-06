/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Rewrites a requirement to satisfy the currency requirements generated by {@link CurrencyConversionFunction} into one that will query a
 * {@link CurrencyMatrix}.
 */
public class CurrencyMatrixLookupFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Property name when applied to a {@link CurrencyPair} target to allow the matrix to be selected.
   */
  protected static final String CURRENCY_MATRIX_NAME_PROPERTY = "CurrencyMatrix";

  private final String _defaultCurrencyMatrixName;
  private final String[] _additionalProperties;

  public CurrencyMatrixLookupFunction(final String defaultCurrencyMatrixName) {
    _defaultCurrencyMatrixName = defaultCurrencyMatrixName;
    _additionalProperties = null;
  }

  public CurrencyMatrixLookupFunction(final String[] params) {
    _defaultCurrencyMatrixName = params[0];
    _additionalProperties = new String[params.length - 1];
    System.arraycopy(params, 1, _additionalProperties, 0, _additionalProperties.length);
  }

  @Override
  protected ValueProperties.Builder createValueProperties() {
    final ValueProperties.Builder properties = super.createValueProperties();
    if (_additionalProperties != null) {
      for (int i = 0; i < _additionalProperties.length; i += 2) {
        properties.with(_additionalProperties[i], _additionalProperties[i + 1]);
      }
    }
    return properties;
  }

  protected String getDefaultCurrencyMatrixName() {
    return _defaultCurrencyMatrixName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return CurrencyPair.TYPE;
  }

  private ValueSpecification createSpotRateResult(final ComputationTargetSpecification targetSpec, final ValueProperties properties) {
    return new ValueSpecification(ValueRequirementNames.SPOT_RATE, targetSpec, properties);
  }

  private ValueSpecification createHistoricalTimeSeriesResult(final ComputationTargetSpecification targetSpec, ValueProperties properties) {
    properties = properties.copy()
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.NO_VALUE, HistoricalTimeSeriesFunctionUtils.YES_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.NO_VALUE, HistoricalTimeSeriesFunctionUtils.YES_VALUE)
        .get();
    return new ValueSpecification(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, targetSpec, properties);
  }

  private ValueSpecification createTimeSeriesLatestResult(final ComputationTargetSpecification targetSpec, final ValueProperties properties) {
    return new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, targetSpec, properties);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties properties = createValueProperties().withAny(CURRENCY_MATRIX_NAME_PROPERTY).get();
    return ImmutableSet.of(createSpotRateResult(targetSpec, properties), createHistoricalTimeSeriesResult(targetSpec, properties),
        createTimeSeriesLatestResult(targetSpec, properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final CurrencyPair currencies = (CurrencyPair) target.getValue();
    final ValueProperties.Builder constraints = ValueProperties
        .with(AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY, currencies.getCounter().getCode())
        .with(AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY, currencies.getBase().getCode());
    ExternalIdBundle matrixIdentifiers = null;
    if (desiredValue.getConstraints().getProperties() != null) {
      for (final String constraintName : desiredValue.getConstraints().getProperties()) {
        if (ValuePropertyNames.FUNCTION.equals(constraintName) || constraintName.startsWith(ValuePropertyNames.OUTPUT_RESERVED_PREFIX)) {
          continue;
        }
        final Set<String> values = desiredValue.getConstraints().getValues(constraintName);
        if (CURRENCY_MATRIX_NAME_PROPERTY.equals(constraintName)) {
          if (values.isEmpty()) {
            matrixIdentifiers = ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, getDefaultCurrencyMatrixName()).toBundle();
          } else {
            if (values.size() == 1) {
              matrixIdentifiers = ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, values.iterator().next()).toBundle();
            } else {
              final Collection<ExternalId> identifiers = new ArrayList<>(values.size());
              for (final String matrixName : values) {
                identifiers.add(ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, matrixName));
              }
              matrixIdentifiers = ExternalIdBundle.of(identifiers);
            }
          }
        } else {
          if (values.isEmpty()) {
            constraints.withAny(constraintName);
          } else {
            constraints.with(constraintName, values);
          }
          if (desiredValue.getConstraints().isOptional(constraintName)) {
            constraints.withOptional(constraintName);
          }
        }
      }
    }
    if (matrixIdentifiers == null) {
      matrixIdentifiers = ExternalId.of(CurrencyMatrixResolver.IDENTIFIER_SCHEME, getDefaultCurrencyMatrixName()).toBundle();
    }
    return Collections.singleton(
        new ValueRequirement(desiredValue.getValueName(), new ComputationTargetRequirement(CurrencyMatrixResolver.TYPE, matrixIdentifiers), constraints.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(inputs.size());
    for (final Map.Entry<ValueSpecification, ValueRequirement> inputEntry : inputs.entrySet()) {
      final ValueProperties.Builder properties = createValueProperties();
      properties.with(CURRENCY_MATRIX_NAME_PROPERTY,
          inputEntry.getValue().getTargetReference().getRequirement().getIdentifiers().getValue(CurrencyMatrixResolver.IDENTIFIER_SCHEME));
      final ValueProperties inputProperties = inputEntry.getKey().getProperties();
      for (final String propertyName : inputProperties.getProperties()) {
        if (!AbstractCurrencyMatrixSourcingFunction.SOURCE_CURRENCY_PROPERTY.equals(propertyName)
            && !AbstractCurrencyMatrixSourcingFunction.TARGET_CURRENCY_PROPERTY.equals(propertyName)
            && !ValuePropertyNames.FUNCTION.equals(propertyName)) {
          final Set<String> values = inputProperties.getValues(propertyName);
          if (values.isEmpty()) {
            properties.withAny(propertyName);
          } else {
            properties.with(propertyName, values);
          }
        }
      }
      results.add(new ValueSpecification(inputEntry.getKey().getValueName(), target.toSpecification(), properties.get()));
    }
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(desiredValues.size());
    for (final ValueRequirement desiredValue : desiredValues) {
      final Object input = inputs.getValue(desiredValue.getValueName());
      results.add(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), target.toSpecification(), desiredValue.getConstraints()), input));
    }
    return results;
  }

  public int getPriority() {
    if (getDefaultCurrencyMatrixName().contains(CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA)) {
      return -1;
    }
    return 0;
  }

}
