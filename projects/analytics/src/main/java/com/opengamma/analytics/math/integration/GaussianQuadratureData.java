/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Class holding the results of calculations of weights and abscissas by {@link QuadratureWeightAndAbscissaFunction}. 
 */
public class GaussianQuadratureData {
  private final double[] _weights;
  private final double[] _abscissas;

  /**
   * @param abscissas An array containing the abscissas, not null
   * @param weights An array containing the weights, not null, must be the same length as the abscissa array
   */
  public GaussianQuadratureData(final double[] abscissas, final double[] weights) {
    Validate.notNull(abscissas, "abscissas");
    Validate.notNull(weights, "weights");
    Validate.isTrue(abscissas.length == weights.length, "Abscissa and weight arrays must be the same length");
    _weights = weights;
    _abscissas = abscissas;
  }

  /**
   * @return The weights
   */
  public double[] getWeights() {
    return _weights;
  }

  /**
   * @return The abscissas
   */
  public double[] getAbscissas() {
    return _abscissas;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_abscissas);
    result = prime * result + Arrays.hashCode(_weights);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GaussianQuadratureData other = (GaussianQuadratureData) obj;
    if (!Arrays.equals(_abscissas, other._abscissas)) {
      return false;
    }
    return Arrays.equals(_weights, other._weights);
  }

}
