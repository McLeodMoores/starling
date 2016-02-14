/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.Objects;

import org.threeten.bp.Period;

/**
 * Class with the attributed required to generate an interest rate (IR) instrument from the market quotes.
 * The attributes are composed of one or two tenors (the start period and the end period).
 */
public class GeneratorAttributeIR extends GeneratorAttribute {

  /**
   * The start period.
   */
  private final Period _startPeriod;
  /**
   * The end period.
   */
  private final Period _endPeriod;

  /**
   * Constructor.
   * @param startPeriod The start period.
   * @param endPeriod The end period.
   */
  public GeneratorAttributeIR(final Period startPeriod, final Period endPeriod) {
    super();
    _startPeriod = startPeriod;
    _endPeriod = endPeriod;
  }

  /**
   * Constructor. By default the start period is set to ZERO.
   * @param endPeriod The end period.
   */
  public GeneratorAttributeIR(final Period endPeriod) {
    _startPeriod = Period.ZERO;
    _endPeriod = endPeriod;
  }

  /**
   * Gets the startPeriod field.
   * @return the startPeriod
   */
  public Period getStartPeriod() {
    return _startPeriod;
  }

  /**
   * Gets the endPeriod field.
   * @return the endPeriod
   */
  public Period getEndPeriod() {
    return _endPeriod;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + (_endPeriod == null ? 0 : _endPeriod.hashCode());
    result = prime * result
        + (_startPeriod == null ? 0 : _startPeriod.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GeneratorAttributeIR)) {
      return false;
    }
    final GeneratorAttributeIR other = (GeneratorAttributeIR) obj;
    if (!Objects.equals(_endPeriod, other._endPeriod)) {
      return false;
    }
    return Objects.equals(_startPeriod, other._startPeriod);
  }

}
