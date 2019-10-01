/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import com.opengamma.engine.target.ComputationTargetType;

/**
 * A factory for DSL functions.
 */
public final class Function {

  /**
   * Creates the function signature.
   *
   * @param name
   *          the name
   * @param computationTargetType
   *          the type
   * @return the signature, not null
   */
  public static FunctionSignature function(final String name, final ComputationTargetType computationTargetType) {
    return new SimpleFunctionSignature(name, computationTargetType);
  }

  /**
   * Creates the function output gate.
   *
   * @param name
   *          the name
   * @return the output, not null
   */
  public static FunctionOutput output(final String name) {
    return new FunctionOutput(name);
  }

  /**
   * Creates the function input gate.
   *
   * @param name
   *          the name
   * @return the input, not null
   */
  public static FunctionInput input(final String name) {
    return new FunctionInput(name);
  }

  private Function() {
  }
}
