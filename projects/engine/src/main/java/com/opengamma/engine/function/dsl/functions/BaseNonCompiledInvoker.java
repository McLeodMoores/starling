/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.functions;

import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.lambdava.functions.Function2;
import com.opengamma.lambdava.streams.Stream;
import com.opengamma.lambdava.streams.StreamI;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

public abstract class BaseNonCompiledInvoker extends AbstractFunction.NonCompiledInvoker {

  private FunctionSignature _functionSignature;

  private static Pair<Map<String, StreamI<FunctionInput>>, Map<String, StreamI<FunctionOutput>>> ioputsByName(final FunctionSignature signature) {
    final Map<String, StreamI<FunctionInput>> inputsByName = signature.getInputs().reduce(new HashMap<String, StreamI<FunctionInput>>(),
        new Function2<HashMap<String, StreamI<FunctionInput>>, FunctionInput, HashMap<String, StreamI<FunctionInput>>>() {
      @Override
      public HashMap<String, StreamI<FunctionInput>> execute(final HashMap<String, StreamI<FunctionInput>> acc, final FunctionInput functionInput) {
        final String name = functionInput.getName();
        if (name == null) {
          throw new IllegalArgumentException("Input's name must be provided");
        }
        StreamI<FunctionInput> inputs = acc.get(name);
        if (inputs == null) {
          inputs = Stream.empty();
          acc.put(name, inputs);
        }
        acc.put(name, inputs.cons(functionInput));
        return acc;
      }
    });

    final Map<String, StreamI<FunctionOutput>> outputsByName = signature.getOutputs().reduce(new HashMap<String, StreamI<FunctionOutput>>(),
        new Function2<HashMap<String, StreamI<FunctionOutput>>, FunctionOutput, HashMap<String, StreamI<FunctionOutput>>>() {
      @Override
      public HashMap<String, StreamI<FunctionOutput>> execute(final HashMap<String, StreamI<FunctionOutput>> acc, final FunctionOutput functionOutput) {
        final String name = functionOutput.getName();
        if (name == null) {
          throw new IllegalArgumentException("Output's name must be provided");
        }
        StreamI<FunctionOutput> outputs = acc.get(name);
        if (outputs == null) {
          outputs = Stream.empty();
        }
        acc.put(name, outputs.cons(functionOutput));
        return acc;
      }
    });
    return Pairs.of(inputsByName, outputsByName);
  }

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
    final Pair<Map<String, StreamI<FunctionInput>>, Map<String, StreamI<FunctionOutput>>> ioputsByName = ioputsByName(signature);

    final Map<String, StreamI<FunctionOutput>> outputsByName = ioputsByName.getSecond();

    final Set<ValueSpecification> valueSpecifications = new HashSet<>();

    for (final String name : outputsByName.keySet()) {
      final StreamI<FunctionOutput> functionOutputs = outputsByName.get(name);
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
    final Pair<Map<String, StreamI<FunctionInput>>, Map<String, StreamI<FunctionOutput>>> ioputsByName = ioputsByName(signature);

    final Map<String, StreamI<FunctionInput>> inputsByName = ioputsByName.getFirst();

    final Set<ValueRequirement> valueRequirements = new HashSet<>();

    for (final String name : inputsByName.keySet()) {
      final StreamI<FunctionInput> functionInputs = inputsByName.get(name);
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
          final StreamI<ValuePropertiesModifier> recorderValueProperties = rvps.getRecordedValueProperties();
          final ValueProperties.Builder valuePropertiesBuilder = recorderValueProperties.reduce(copiedValueProperties,
              new Function2<ValueProperties.Builder, ValuePropertiesModifier, ValueProperties.Builder>() {
            @Override
            public ValueProperties.Builder execute(final ValueProperties.Builder builder, final ValuePropertiesModifier modifier) {
              return modifier.modify(builder);
            }
          });
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
    final Pair<Map<String, StreamI<FunctionInput>>, Map<String, StreamI<FunctionOutput>>> ioputsByName = ioputsByName(signature);

    final Map<String, StreamI<FunctionOutput>> outputsByName = ioputsByName.getSecond();

    final Set<ValueSpecification> valueSpecifications = new HashSet<>();

    for (final String name : outputsByName.keySet()) {
      final StreamI<FunctionOutput> functionOutputs = outputsByName.get(name);
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
          //FunctionInput copyFrom = inputsByName.get(rvps.getCopiedFrom()).first();

          //Find the apropierate valueSpecifications

          final ValueSpecification copyFrom = functional(inputSpecificationsMap.keySet()).filter(new Function1<ValueSpecification, Boolean>() {
            @Override
            public Boolean execute(final ValueSpecification valueSpecification) {
              return valueSpecification.getValueName().equals(rvps.getCopiedFrom());
              //&& valueSpecification.getTargetSpecification().equals(computationTargetSpecification) && valueSpecification.getProperties().isSatisfiedBy()
            }
          }).first();

          ValueProperties.Builder builder;
          if (copyFrom != null) {
            builder = copyFrom.getProperties().copy();
          } else {
            builder = ValueProperties.all().copy();
          }
          final StreamI<ValuePropertiesModifier> recorderValueProperties = rvps.getRecordedValueProperties();
          final ValueProperties.Builder valuePropertiesBuilder = recorderValueProperties.reduce(builder,
              new Function2<ValueProperties.Builder, ValuePropertiesModifier, ValueProperties.Builder>() {
            @Override
            public ValueProperties.Builder execute(final ValueProperties.Builder builder, final ValuePropertiesModifier modifier) {
              return modifier.modify(builder);
            }
          });
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
