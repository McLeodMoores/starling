/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A function suitable for use in mock environments.
 *
 */
public class MockFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * default unique id
   */
  public static final String UNIQUE_ID = "mock";

  private final ComputationTarget _target;
  private final Set<ValueRequirement> _requirements = new HashSet<>();
  private final Set<ValueSpecification> _resultSpecs = new HashSet<>();
  private final Set<ComputedValue> _results = new HashSet<>();

  /**
   * @param uniqueId identifier of the function
   * @param target Target mock function applies to
   * @param output What the mock function outputs
   * @return A mock function with one input and one output
   */
  public static MockFunction getMockFunction(final String uniqueId, final ComputationTarget target, final Object output) {
    final MockFunction fn = new MockFunction(uniqueId, target);
    fn.addResult(new ValueSpecification("OUTPUT", target.toSpecification(), fn.createValueProperties().get()), output);
    return fn;
  }

  public static MockFunction getMockFunction(final ComputationTarget target, final Object output) {
    return getMockFunction(UNIQUE_ID, target, output);
  }

  public static MockFunction getMockFunction(final ComputationTarget target, final ValueSpecification spec, final Object value) {
    final MockFunction fn = new MockFunction(UNIQUE_ID, target);
    fn.addResult(new ComputedValue(spec, value));
    return fn;
  }

  public static MockFunction getMockFunction(final String uniqueId, final ComputationTarget target, final Object output, final ValueRequirement input) {
    final MockFunction fn = getMockFunction(uniqueId, target, output);
    fn.addRequirement(input);
    return fn;
  }

  public static MockFunction getMockFunction(final String uniqueId, final ComputationTarget target, final Object output, final MockFunction inputFunction) {
    final MockFunction fn = getMockFunction(uniqueId, target, output);
    for (final ValueSpecification resultSpec : inputFunction.getResultSpecs()) {
      fn.addRequirement(resultSpec.toRequirementSpecification());
    }
    return fn;
  }

  public MockFunction(final String uniqueId, final ComputationTarget target) {
    _target = target;
    setUniqueId(uniqueId);
  }

  public MockFunction(final ComputationTarget target) {
    this(UNIQUE_ID, target);
  }

  public void addRequirement(final ValueRequirement requirement) {
    addRequirements(Collections.singleton(requirement));
  }

  public void addRequirements(final Collection<ValueRequirement> requirements) {
    _requirements.addAll(requirements);
  }

  public Set<ValueRequirement> getRequirements() {
    return Collections.unmodifiableSet(_requirements);
  }

  public void addResult(final ValueSpecification valueSpec, final Object result) {
    addResult(new ComputedValue(valueSpec, result));
  }

  public void addResult(final ComputedValue result) {
    addResults(Collections.singleton(result));
  }

  public void addResults(final Collection<ComputedValue> results) {
    _results.addAll(results);
    for (final ComputedValue result : _results) {
      _resultSpecs.add(result.getSpecification());
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return ObjectUtils.equals(target.getUniqueId(), _target.getUniqueId());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.unmodifiableSet(_requirements);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return getResultSpecs();
  }

  public Set<ValueSpecification> getResultSpecs() {
    return _resultSpecs;
  }

  public ValueSpecification getResultSpec() {
    if (_resultSpecs.size() != 1) {
      throw new IllegalStateException("Result count must be 1: " + _resultSpecs.toString());
    }
    return _resultSpecs.iterator().next();
  }

  public Set<ComputedValue> getResults() {
    return _results;
  }

  public ComputedValue getResult() {
    if (_results.size() != 1) {
      throw new IllegalStateException("Result count must be 1: " + _results.toString());
    }
    return _results.iterator().next();
  }

  @Override
  public String getShortName() {
    return getUniqueId() + " for " + _target;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _target.getType();
  }

  public ComputationTarget getTarget() {
    return _target;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Set<ComputedValue> results = new HashSet<>();
    for (final ValueRequirement desiredValue : desiredValues) {
      for (final ComputedValue result : _results) {
        // PLAT-2290; desiredValue is really a ValueSpecification so can do an equality test
        if (desiredValue.getValueName() == result.getSpecification().getValueName()
            && desiredValue.getTargetReference().equals(result.getSpecification().getTargetSpecification())
            && desiredValue.getConstraints().equals(result.getSpecification().getProperties())) {
          results.add(result);
        }
      }
    }
    return results;
  }

  @Override
  public String toString() {
    return getShortName() + " (" + getUniqueId() + ")";
  }

}
