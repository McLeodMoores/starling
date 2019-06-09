/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivities;

/**
 * Holds the factor type of a FactorExposureEntry.
 */
public final class FactorType {
  /**
   * A constant representing yield curve risk factors.
   */
  public static final FactorType YIELD = new FactorType("yieldRiskFactor");
  /**
   * A constant representing volatility risk factors.
   */
  public static final FactorType VOLATILITY = new FactorType("volatilityRiskFactor");
  /**
   * A constant representing CDS spread risk factors.
   */
  public static final FactorType CDS_SPREAD = new FactorType("CDSSpreadRiskFactor");
  /**
   * A constant representing equity risk factors.
   */
  public static final FactorType EQUITY = new FactorType("equityRiskFactor");

  private final String _type;

  private FactorType(final String type) {
    _type = type;
  }

  public static FactorType of(final String factorType) {
    return new FactorType(factorType);
  }

  public String getFactorType() {
    return _type;
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof FactorType)) {
      return false;
    }
    final FactorType other = (FactorType) o;
    return other.getFactorType().equals(getFactorType());
  }

  @Override
  public int hashCode() {
    return getFactorType().hashCode();
  }

  @Override
  public String toString() {
    return getFactorType();
  }
}
