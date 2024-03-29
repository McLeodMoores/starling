/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.ArrayList;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Result of 2D interpolation by piecewise polynomial f(x0,x1) containing _knots0: Positions of knots in x0 direction _knots1: Positions of knots in x1
 * direction _coefMatrix: Coefficient matrix whose (i,j) element is a DoubleMatrix2D containing coefficients for the square, _knots0_i &lt; x0 &lt;
 * _knots0_{i+1}, _knots1_j &lt; x1 &lt; _knots1_{j+1}, Each DoubleMatrix2D is c_ij where f(x0,x1) = sum_{i=0}^{order0-1} sum_{j=0}^{order1-1} coefMat_{ij}
 * (x0-knots0_i)^{order0-1-i} (x1-knots1_j)^{order0-1-j} _nIntervals: Number of intervals in x0 direction and x1 direction, respectively, which should be
 * (Number of knots) - 1 _order: Number of coefficients in polynomial in terms of x0 and x1, respectively, which is equal to (polynomial degree) + 1.
 */
public class PiecewisePolynomialResult2D {

  private final DoubleMatrix1D _knots0;
  private final DoubleMatrix1D _knots1;
  private final DoubleMatrix2D[][] _coefMatrix;
  private final int[] _nIntervals;
  private final int[] _order;

  /**
   * @param knots0
   *          The knots in the x0 direction
   * @param knots1
   *          The knots in the x1 direction
   * @param coefMatrix
   *          The coefficient matrix
   * @param order
   *          The order of the polynomial
   */
  public PiecewisePolynomialResult2D(final DoubleMatrix1D knots0, final DoubleMatrix1D knots1, final DoubleMatrix2D[][] coefMatrix, final int[] order) {

    _knots0 = knots0;
    _knots1 = knots1;
    _coefMatrix = coefMatrix;
    _nIntervals = new int[2];
    _nIntervals[0] = knots0.getNumberOfElements() - 1;
    _nIntervals[1] = knots1.getNumberOfElements() - 1;
    _order = order;
  }

  /**
   * Access _knots0 and _knots1.
   *
   * @return _knots0 and _knots1 contained in a ArrayList
   */
  public ArrayList<DoubleMatrix1D> getKnots2D() {
    final ArrayList<DoubleMatrix1D> res = new ArrayList<>();
    res.add(_knots0);
    res.add(_knots1);

    return res;
  }

  /**
   * Access _knots0.
   *
   * @return _knots0
   */
  public DoubleMatrix1D getKnots0() {
    return _knots0;
  }

  /**
   * Access _knots1.
   *
   * @return knots1
   */
  public DoubleMatrix1D getKnots1() {
    return _knots1;
  }

  /**
   * Access _coefMatrix.
   *
   * @return _coefMatrix
   */
  public DoubleMatrix2D[][] getCoefs() {
    return _coefMatrix;
  }

  /**
   * Access _nIntervals.
   *
   * @return _nIntervals
   */
  public int[] getNumberOfIntervals() {
    return _nIntervals;
  }

  /**
   * Access _order.
   *
   * @return _order
   */
  public int[] getOrder() {
    return _order;
  }

}
