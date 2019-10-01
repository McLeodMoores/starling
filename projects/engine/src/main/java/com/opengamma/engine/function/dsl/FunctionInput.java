/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

/**
 * A function input gate.
 */
public class FunctionInput extends FunctionGate<FunctionInput> {

  /**
   * Creates an instance.
   *
   * @param name
   *          the name
   */
  public FunctionInput(final String name) {
    super(name);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FunctionInput[");
    sb.append("name=");
    sb.append(getName());
    sb.append(", spec=");
    sb.append(getComputationTargetSpecification());
    sb.append(", properties=");
    sb.append(getValueProperties());
    sb.append("]");
    return sb.toString();
  }
}
