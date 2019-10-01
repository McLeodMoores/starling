/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import java.util.stream.Stream;

import com.opengamma.engine.target.ComputationTargetType;

/**
 * A simple function signature.
 */
class SimpleFunctionSignature implements FunctionSignature {

  private final String _name;
  private Stream<FunctionOutput> _outputs = Stream.empty();
  private Stream<FunctionInput> _inputs = Stream.empty();
  private final ComputationTargetType _computationTargetType;
  private Class<?> _computationTargetClass;

  /**
   * @param name
   *          the name of the function, not null
   * @param computationTargetType
   *          the computation target type, not null
   */
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
    signature.setInputs(Stream.concat(Stream.of(input), _inputs));
    return signature;
  }

  @Override
  public FunctionSignature addOutput(final FunctionOutput output) {
    final SimpleFunctionSignature signature = new SimpleFunctionSignature(_name, _computationTargetType);
    signature.setOutputs(Stream.concat(Stream.of(output), _outputs));
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
  public Stream<FunctionOutput> getOutputs() {
    return _outputs;
  }

  private void setOutputs(final Stream<FunctionOutput> outputs) {
    _outputs = outputs;
  }

  @Override
  public Stream<FunctionInput> getInputs() {
    return _inputs;
  }

  private void setInputs(final Stream<FunctionInput> inputs) {
    _inputs = inputs;
  }
}
