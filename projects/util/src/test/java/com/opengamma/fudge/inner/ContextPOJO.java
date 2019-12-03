/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.inner;

/**
 * Mock class.
 */
public class ContextPOJO {

  private double _value;

  /**
   *
   */
  public ContextPOJO() {
  }

  /**
   * @return the value
   */
  public double getValue() {
    return _value;
  }

  /**
   * @param value
   *          the value
   */
  public void setValue(final double value) {
    this._value = value;
  }

}
