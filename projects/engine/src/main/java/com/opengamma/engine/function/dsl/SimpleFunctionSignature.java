/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.lambdava.streams.Functional;
import com.opengamma.lambdava.streams.Stream;

/**
 * A simple function signature.
 */
class SimpleFunctionSignature implements FunctionSignature {

  private final String _name;
  private Functional<FunctionOutput> _outputs = Stream.empty();
  private Functional<FunctionInput> _inputs = Stream.empty();
  private final ComputationTargetType _computationTargetType;
  private Class<?> _computationTargetClass;

  SimpleFunctionSignature(final String name, final ComputationTargetType computationTargetType) {
    _name = name;
    _computationTargetType = computationTargetType;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public ComputationTargetType getComputationTargetType() {
    return _computationTargetType;
  }

  @Override
  public Class<?> getComputationTargetClass() {
    return _computationTargetClass;
  }

  @Override
  public FunctionSignature addInput(final FunctionInput input) {
    final SimpleFunctionSignature signature = new SimpleFunctionSignature(_name, _computationTargetType);
    signature.setInputs(_inputs.cons(input));
    return signature;
  }

  @Override
  public FunctionSignature addOutput(final FunctionOutput output) {
    final SimpleFunctionSignature signature = new SimpleFunctionSignature(_name, _computationTargetType);
    signature.setOutputs(_outputs.cons(output));
    return signature;
  }

  @Override
  public FunctionSignature outputs(final FunctionOutput... outputs) {
    _outputs = Stream.of(outputs);
    return this;
  }

  @Override
  public FunctionSignature inputs(final FunctionInput... inputs) {
    _inputs = Stream.of(inputs);
    return this;
  }

  @Override
  public FunctionSignature targetClass(final Class<?> clazz) {
    _computationTargetClass = clazz;
    return this;
  }

  @Override
  public Functional<FunctionOutput> getOutputs() {
    return _outputs;
  }

  private void setOutputs(final Functional<FunctionOutput> outputs) {
    _outputs = outputs;
  }

  @Override
  public Functional<FunctionInput> getInputs() {
    return _inputs;
  }

  private void setInputs(final Functional<FunctionInput> inputs) {
    _inputs = inputs;
  }
}
