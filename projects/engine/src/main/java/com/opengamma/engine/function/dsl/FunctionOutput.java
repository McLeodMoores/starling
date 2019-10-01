/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

/**
 * A function output gate.
 */
public class FunctionOutput extends FunctionGate<FunctionOutput> {

  /**
   * Creates an instance.
   *
   * @param name
   *          the name
   */
  public FunctionOutput(final String name) {
    super(name);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FunctionOutput[");
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
