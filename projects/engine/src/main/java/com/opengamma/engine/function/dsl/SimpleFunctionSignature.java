/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.engine.target.ComputationTargetType;

/**
 * A simple function signature.
 */
class SimpleFunctionSignature implements FunctionSignature {

  private final String _name;
  private List<FunctionOutput> _outputs = new ArrayList<>();
  private List<FunctionInput> _inputs = new ArrayList<>();
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
    final List<FunctionInput> inputs = new ArrayList<>(_inputs);
    inputs.add(0, input);
    signature.inputs(inputs.toArray(new FunctionInput[0]));
    return signature;
  }

  @Override
  public FunctionSignature addOutput(final FunctionOutput output) {
    final SimpleFunctionSignature signature = new SimpleFunctionSignature(_name, _computationTargetType);
    final List<FunctionOutput> outputs = new ArrayList<>(_outputs);
    outputs.add(0, output);
    signature.outputs(outputs.toArray(new FunctionOutput[0]));
    return signature;
  }

  @Override
  public FunctionSignature outputs(final FunctionOutput... outputs) {
    _outputs = new ArrayList<>(Arrays.asList(outputs));
    return this;
  }

  @Override
  public FunctionSignature inputs(final FunctionInput... inputs) {
    _inputs = new ArrayList<>(Arrays.asList(inputs));
    return this;
  }

  @Override
  public FunctionSignature targetClass(final Class<?> clazz) {
    _computationTargetClass = clazz;
    return this;
  }

  @Override
  public List<FunctionOutput> getOutputs() {
    return _outputs;
  }

  @Override
  public List<FunctionInput> getInputs() {
    return _inputs;
  }

}
