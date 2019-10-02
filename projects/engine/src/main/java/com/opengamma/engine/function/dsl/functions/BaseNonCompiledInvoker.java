/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.functions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.dsl.FunctionInput;
import com.opengamma.engine.function.dsl.FunctionOutput;
import com.opengamma.engine.function.dsl.FunctionSignature;
import com.opengamma.engine.function.dsl.TargetSpecificationReference;
import com.opengamma.engine.function.dsl.properties.RecordingValueProperties;
import com.opengamma.engine.function.dsl.properties.ValuePropertiesModifier;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

public abstract class BaseNonCompiledInvoker extends AbstractFunction.NonCompiledInvoker {

  private FunctionSignature _functionSignature;

  protected abstract FunctionSignature functionSignature();

  private FunctionSignature getFunctionSignature() {
    if (_functionSignature == null) {
      _functionSignature = functionSignature();
    }
    return _functionSignature;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return getFunctionSignature().getComputationTargetType();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final FunctionSignature signature = getFunctionSignature();
    final Map<String, List<FunctionOutput>> outputsByName = signature.getOutputs().stream().collect(Collectors.groupingBy(FunctionOutput::getName));

    final Set<ValueSpecification> valueSpecifications = new HashSet<>();

    for (final String name : outputsByName.keySet()) {
      final List<FunctionOutput> functionOutputs = outputsByName.get(name);
      for (final FunctionOutput functionOutput : functionOutputs) {
        final TargetSpecificationReference tsr = functionOutput.getTargetSpecificationReference();
        final ComputationTargetSpecification cts = functionOutput.getComputationTargetSpecification();
        final RecordingValueProperties rvps = functionOutput.getRecordingValueProperties();
        final ValueProperties vps = functionOutput.getValueProperties();
        if (tsr == null && cts == null) {
          throw new IllegalArgumentException("Target specification must be provided by function DSL, but there wasn't any for input: " + name);
        }
        if (rvps == null && vps == null) {
          throw new IllegalArgumentException("Value properties must be provided by function DSL, but there wasn't any for input: " + name);
        }

        ComputationTargetSpecification computationTargetSpecification;
        if (cts != null) {
          computationTargetSpecification = cts;
        } else {
          computationTargetSpecification = target.toSpecification();
        }
        ValueProperties valueProperties;
        if (vps != null) {
          valueProperties = vps;
        } else {
          valueProperties = ValueProperties.all();
        }
        final ValueSpecification valueSpecification = new ValueSpecification(name, computationTargetSpecification, valueProperties);
        valueSpecifications.add(valueSpecification);
      }
    }
    return valueSpecifications;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FunctionSignature signature = getFunctionSignature();

    final Map<String, List<FunctionInput>> inputsByName = signature.getInputs().stream().collect(Collectors.groupingBy(FunctionInput::getName));

    final Set<ValueRequirement> valueRequirements = new HashSet<>();

    for (final String name : inputsByName.keySet()) {
      final List<FunctionInput> functionInputs = inputsByName.get(name);
      for (final FunctionInput functionInput : functionInputs) {
        final TargetSpecificationReference tsr = functionInput.getTargetSpecificationReference();
        final ComputationTargetSpecification cts = functionInput.getComputationTargetSpecification();
        final RecordingValueProperties rvps = functionInput.getRecordingValueProperties();
        final ValueProperties vps = functionInput.getValueProperties();
        if (tsr == null && cts == null) {
          throw new IllegalArgumentException("Target specification must be provided by function DSL, but there wasn't any for input: " + name);
        }
        if (rvps == null && vps == null) {
          throw new IllegalArgumentException("Value properties must be provided by function DSL, but there wasn't any for input: " + name);
        }

        ComputationTargetSpecification computationTargetSpecification;
        if (cts != null) {
          computationTargetSpecification = cts;
        } else {
          computationTargetSpecification = target.toSpecification();
        }
        ValueProperties valueProperties;
        if (vps != null) {
          valueProperties = vps;
        } else {
          final ValueProperties.Builder copiedValueProperties = desiredValue.getConstraints().copy();
          final Stream<ValuePropertiesModifier> recorderValueProperties = rvps.getRecordedValueProperties();
          final ValueProperties.Builder valuePropertiesBuilder = recorderValueProperties.reduce(
              copiedValueProperties,
              (builder, modifier) -> modifier.modify(builder),
              (t, u) -> t.get().union(u.get()).copy());
          valueProperties = valuePropertiesBuilder.get();
        }
        final ValueRequirement valueRequirement = new ValueRequirement(name, computationTargetSpecification, valueProperties);
        valueRequirements.add(valueRequirement);
      }
    }
    return valueRequirements;

  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputSpecificationsMap) {
    final FunctionSignature signature = getFunctionSignature();
    final Map<String, List<FunctionOutput>> outputsByName = signature.getOutputs().stream().collect(Collectors.groupingBy(FunctionOutput::getName));

    final Set<ValueSpecification> valueSpecifications = new HashSet<>();

    for (final String name : outputsByName.keySet()) {
      final List<FunctionOutput> functionOutputs = outputsByName.get(name);
      for (final FunctionOutput functionOutput : functionOutputs) {
        final TargetSpecificationReference tsr = functionOutput.getTargetSpecificationReference();
        final ComputationTargetSpecification cts = functionOutput.getComputationTargetSpecification();
        final RecordingValueProperties rvps = functionOutput.getRecordingValueProperties();
        final ValueProperties vps = functionOutput.getValueProperties();
        if (tsr == null && cts == null) {
          throw new IllegalArgumentException("Target specification must be provided by function DSL, but there wasn't any for input: " + name);
        }
        if (rvps == null && vps == null) {
          throw new IllegalArgumentException("Value properties must be provided by function DSL, but there wasn't any for input: " + name);
        }

        final ComputationTargetSpecification computationTargetSpecification;
        if (cts != null) {
          computationTargetSpecification = cts;
        } else {
          computationTargetSpecification = target.toSpecification();
        }
        ValueProperties valueProperties;
        if (vps != null) {
          valueProperties = vps;
        } else {
          // Find the appropriate valueSpecifications
          final Optional<ValueSpecification> copyFrom = inputSpecificationsMap.keySet().stream().filter(v -> v.getValueName().equals(rvps.getCopiedFrom()))
              .findFirst();
          final ValueProperties.Builder builder = copyFrom.isPresent() ? copyFrom.get().getProperties().copy() : ValueProperties.all().copy();
          final Stream<ValuePropertiesModifier> recorderValueProperties = rvps.getRecordedValueProperties();
          final ValueProperties.Builder valuePropertiesBuilder = recorderValueProperties.reduce(
              builder,
              (builder1, modifier) -> modifier.modify(builder1),
              (t, u) -> t.get().union(u.get()).copy());
          valueProperties = valuePropertiesBuilder.get();
        }
        final ValueSpecification valueSpecification = new ValueSpecification(name, computationTargetSpecification, valueProperties);
        valueSpecifications.add(valueSpecification);
      }
    }
    return valueSpecifications;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FunctionSignature signature = getFunctionSignature();
    final ComputationTargetType ctt = signature.getComputationTargetType();
    final Class<?> ctc = signature.getComputationTargetClass();
    if (ctt != null && !ctt.isCompatible(target.getType())) {
      return false;
    }
    if (ctc != null && !ctc.isAssignableFrom(target.getValue().getClass())) {
      return false;
    }
    return true;
  }
}
