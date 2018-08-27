/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Retrieves an FXForwardCurveDefinition.
 */
public class FXForwardCurveDefinitionFunction extends AbstractFunction {

  private final UnorderedCurrencyPair _currencies;
  private final String _curveName;
  private final ComputationTargetSpecification _targetSpec;

  private ConfigDBFXForwardCurveDefinitionSource _fxForwardCurveDefinitionSource;

  public FXForwardCurveDefinitionFunction(final String ccy1, final String ccy2, final String curveName) {
    this(UnorderedCurrencyPair.of(Currency.of(ccy1), Currency.of(ccy2)), curveName);
  }

  public FXForwardCurveDefinitionFunction(final UnorderedCurrencyPair currencies, final String curveName) {
    _currencies = currencies;
    _curveName = curveName;
    _targetSpec = ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencies);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _fxForwardCurveDefinitionSource = ConfigDBFXForwardCurveDefinitionSource.init(context, this);
  }

  private class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final FXForwardCurveDefinition _curveDefinition;

    public CompiledImpl(final FXForwardCurveDefinition curveDefinition) {
      _curveDefinition = curveDefinition;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _currencies.equals(target.getValue());
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties properties = createResultProperties();
      return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION, _targetSpec, properties));
    }

    protected ValueProperties createResultProperties() {
      return createValueProperties().with(ValuePropertyNames.CURVE, _curveName).get();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      return ImmutableSet.of();
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
        throws AsynchronousExecution {
      return ImmutableSet.of(new ComputedValue(new ValueSpecification(ValueRequirementNames.FX_FORWARD_CURVE_DEFINITION, _targetSpec, createResultProperties()), _curveDefinition));
    }

  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final FXForwardCurveDefinition definition = _fxForwardCurveDefinitionSource.getDefinition(_curveName, _currencies.toString());
    return new CompiledImpl(definition);
  }

}
