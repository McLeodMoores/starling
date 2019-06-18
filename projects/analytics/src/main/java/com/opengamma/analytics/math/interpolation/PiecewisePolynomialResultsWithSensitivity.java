/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * Result of interpolation by piecewise polynomial containing information about:
 * <ul>
 * <li>the knot positions;</li>
 * <li>a coefficient matrix whose i-th row vector is { a_n, a_{n-1}, ...} for the i-th interval, where a_n, a_{n-1},... are coefficients of f(x) = a_n (x-x_i)^n
 * + a_{n-1} (x-x_i)^{n-1} + .... In multidimensional cases, coefficients for the i-th interval of the j-th spline is in (j*(i-1) + i) -th row vector;</li>
 * <li>the number of coefficients in polynomial, which is equal to (polynomial degree) + 1;</li>
 * <li>the number of splines</li>
 * <li>the sensitivity of the coefficients whose (i,j,k)th element is \frac{\partial a^i_{n-j}}{\partial y_k}
 * </ul>
 */
public class PiecewisePolynomialResultsWithSensitivity extends PiecewisePolynomialResult {

  private final DoubleMatrix2D[] _coeffSense;

  /**
   * @param knots
   *          the knot positions
   * @param coefMatrix
   *          the polynomial coefficient matrix
   * @param order
   *          the order
   * @param dim
   *          the number of splines
   * @param coeffSense
   *          the sensitivity of the coefficients to the nodes (y-values)
   */
  public PiecewisePolynomialResultsWithSensitivity(final DoubleMatrix1D knots, final DoubleMatrix2D coefMatrix, final int order,
      final int dim, final DoubleMatrix2D[] coeffSense) {
    super(knots, coefMatrix, order, dim);
    if (dim != 1) {
      throw new NotImplementedException();
    }
    ArgumentChecker.noNulls(coeffSense, "null coeffSense"); // coefficient
    _coeffSense = coeffSense;
  }

  /**
   * Gets the sensitivities of the coefficients to the nodes.
   * 
   * @return the sensitivities
   */
  public DoubleMatrix2D[] getCoefficientSensitivityAll() {
    return _coeffSense;
  }

  /**
   * Gets the sensitivities of the coefficients to the nodes for the i-th interval.
   * 
   * @param interval
   *          the interval
   * @return the sensitivities for the i-th interval
   */
  public DoubleMatrix2D getCoefficientSensitivity(final int interval) {
    return _coeffSense[interval];
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_coeffSense);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof PiecewisePolynomialResultsWithSensitivity)) {
      return false;
    }
    final PiecewisePolynomialResultsWithSensitivity other = (PiecewisePolynomialResultsWithSensitivity) obj;
    if (!Arrays.equals(_coeffSense, other._coeffSense)) {
      return false;
    }
    return true;
  }

}
