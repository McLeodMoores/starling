/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import java.util.stream.Stream;

import com.opengamma.engine.target.ComputationTargetType;

/**
 * DSL function signature.
 */
public interface FunctionSignature {

  /**
   * Sets the outputs of the function.
   *
   * @param outputs
   *          the outputs, not null
   * @return the signature
   */
  FunctionSignature outputs(FunctionOutput... outputs);

  /**
   * Sets the inputs of the function.
   *
   * @param inputs
   *          the inputs, not null
   * @return the signature
   */
  FunctionSignature inputs(FunctionInput... inputs);

  /**
   * Gets the outputs.
   *
   * @return the outputs
   */
  Stream<FunctionOutput> getOutputs();

  /**
   * Gets the inputs.
   *
   * @return the inputs
   */
  Stream<FunctionInput> getInputs();

  /**
   * Gets the function name.
   *
   * @return the name
   */
  String getName();

  /**
   * Gets the computation target type.
   *
   * @return the target type
   */
  ComputationTargetType getComputationTargetType();

  /**
   * Adds an input.
   *
   * @param input
   *          the input, not null
   * @return the signature
   */
  FunctionSignature addInput(FunctionInput input);

  /**
   * Adds an output.
   *
   * @param output
   *          the output, not null
   * @return the signature
   */
  FunctionSignature addOutput(FunctionOutput output);

  /**
   * Gets the class of the computation target.
   *
   * @return the class
   */
  Class<?> getComputationTargetClass();

  /**
   * Sets the class of the computation target.
   *
   * @param clazz
   *          the class
   * @return the signature
   */
  FunctionSignature targetClass(Class<?> clazz);

}
