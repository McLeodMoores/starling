/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Result of interpolation by piecewise polynomial containing information about:
 * <ul>
 * <li>the knot positions;</li>
 * <li>a coefficient matrix whose i-th row vector is { a_n, a_{n-1}, ...} for the i-th interval, where a_n, a_{n-1},... are coefficients of f(x) = a_n (x-x_i)^n
 * + a_{n-1} (x-x_i)^{n-1} + .... In multidimensional cases, coefficients for the i-th interval of the j-th spline is in (j*(i-1) + i) -th row vector;</li>
 * <li>the number of coefficients in polynomial, which is equal to (polynomial degree) + 1;</li>
 * <li>the number of splines</li>
 * </ul>
 */
public class PiecewisePolynomialResult {

  private final DoubleMatrix1D _knots;
  private final DoubleMatrix2D _coefMatrix;
  private final int _nIntervals;
  private final int _order;
  private final int _dim;

  /**
   * @param knots
   *          the knot positions
   * @param coefMatrix
   *          the polynomial coefficient matrix
   * @param order
   *          the order
   * @param dim
   *          the number of splines
   */
  public PiecewisePolynomialResult(final DoubleMatrix1D knots, final DoubleMatrix2D coefMatrix, final int order, final int dim) {

    _knots = knots;
    _coefMatrix = coefMatrix;
    _nIntervals = knots.getNumberOfElements() - 1;
    _order = order;
    _dim = dim;

  }

  /**
   * Gets the knots.
   *
   * @return Knots as DoubleMatrix1D
   */
  public DoubleMatrix1D getKnots() {
    return _knots;
  }

  /**
   * Gets the coefficient matrix.
   *
   * @return Coefficient Matrix
   */
  public DoubleMatrix2D getCoefMatrix() {
    return _coefMatrix;
  }

  /**
   * Gets the number of intervals.
   *
   * @return Number of Intervals
   */
  public int getNumberOfIntervals() {
    return _nIntervals;
  }

  /**
   * Gets the order of the polynomial.
   *
   * @return Number of coefficients in polynomial; 2 if _nIntervals=1, 3 if _nIntervals=2, 4 otherwise
   */
  public int getOrder() {
    return _order;
  }

  /**
   * Gets the dimension of the spline.
   *
   * @return Dimension of spline
   */
  public int getDimensions() {
    return _dim;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _coefMatrix.hashCode();
    result = prime * result + _dim;
    result = prime * result + _knots.hashCode();
    result = prime * result + _order;
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
    if (!(obj instanceof PiecewisePolynomialResult)) {
      return false;
    }
    final PiecewisePolynomialResult other = (PiecewisePolynomialResult) obj;
    if (!_coefMatrix.equals(other._coefMatrix)) {
      return false;
    }
    if (_dim != other._dim) {
      return false;
    }
    if (!_knots.equals(other._knots)) {
      return false;
    }
    if (_order != other._order) {
      return false;
    }
    return true;
  }

}
