/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 2D interpolator for gridded data using 1D piecewise polynomial spline interpolators. Given a set of data (x0Values_i, x1Values_j, yValues_{ij}), find values,
 * first derivatives and second derivatives of the corresponding interpolant at (x0Keys_k, x1Keys_l).
 */
public class MeshgridInterpolator2D {

  private final PiecewisePolynomialInterpolator[] _method;

  /**
   * Constructor which can take different methods for x0 and x1.
   *
   * @param method
   *          Choose 2 of {@link PiecewisePolynomialInterpolator}
   */
  public MeshgridInterpolator2D(final PiecewisePolynomialInterpolator[] method) {
    ArgumentChecker.notNull(method, "method");
    ArgumentChecker.isTrue(method.length == 2, "two methods should be chosen");

    _method = new PiecewisePolynomialInterpolator[2];
    for (int i = 0; i < 2; ++i) {
      _method[i] = method[i];
    }
  }

  /**
   * Constructor using the same interpolation method for x0 and x1.
   *
   * @param method
   *          {@link PiecewisePolynomialInterpolator}
   */
  public MeshgridInterpolator2D(final PiecewisePolynomialInterpolator method) {
    _method = new PiecewisePolynomialInterpolator[] { method, method };
  }

  /**
   * Gets the interpolated value at <code>(x0Key, x1Key)</code>.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Key
   *          the x0 value for which the interpolated value is required
   * @param x1Key
   *          the x1 value for which the interpolated value is required
   * @return Value of 2D interpolant at <code>(x0Key, x1Key)</code>
   */
  public double interpolate(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double x0Key, final double x1Key) {
    final double[] interpX1 = _method[1].interpolate(x1Values, yValues, x1Key).getData();
    return _method[0].interpolate(x0Values, interpX1, x0Key);
  }

  /**
   * Gets the interpolated values at <code>(x0Key_i, x1Key_j)</code> for all i and j in the vectors.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Keys
   *          the x0 values for which the interpolated value is required
   * @param x1Keys
   *          the x1 values for which the interpolated value is required
   * @return Values of 2D interpolant at <code>(x0Key_i, x1Keys_j)</code>
   */
  public DoubleMatrix2D interpolate(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double[] x0Keys, final double[] x1Keys) {
    final double[][] interpX1 = OG_ALGEBRA.getTranspose(_method[1].interpolate(x1Values, yValues, x1Keys)).getData();
    return OG_ALGEBRA.getTranspose(_method[0].interpolate(x0Values, interpX1, x0Keys));
  }

  /**
   * Gets the first derivative of the surface with respect to x0 at <code>(x0Key, x1Key)</code>.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Key
   *          the x0 value for which the interpolated value is required
   * @param x1Key
   *          the x1 value for which the interpolated value is required
   * @return Value of first derivative with respect to x0 at <code>(x0Key, x1Key)</code>
   */
  public double differentiateX0(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double x0Key, final double x1Key) {
    final PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();
    final double[] interpX0Diff = func.differentiate(_method[0].interpolate(x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData()), x0Key)
        .getData();
    return _method[1].interpolate(x1Values, interpX0Diff, x1Key);
  }

  /**
   * Gets the first derivative of the surface with respect to x0 at <code>(x0Key_i, x1Key_j)</code> for all i and j in the vectors.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Keys
   *          the x0 values for which the interpolated value is required
   * @param x1Keys
   *          the x1 values for which the interpolated value is required
   * @return Values of first derivative with respect to x0 at <code>(x0Key_i, x1Keys_j)</code>
   */
  public DoubleMatrix2D differentiateX0(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double[] x0Keys,
      final double[] x1Keys) {
    final PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();
    final double[][] interpX0Diff = OG_ALGEBRA
        .getTranspose(func.differentiate(_method[0].interpolate(x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData()), x0Keys)).getData();
    return _method[1].interpolate(x1Values, interpX0Diff, x1Keys);
  }

  /**
   * Gets the second derivative of the surface with respect to x0 at <code>(x0Key, x1Key)</code>.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Key
   *          the x0 value for which the interpolated value is required
   * @param x1Key
   *          the x1 value for which the interpolated value is required
   * @return Value of second derivative with respect to x0 at <code>(x0Key, x1Key)</code>
   */
  public double differentiateX0Twice(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double x0Key, final double x1Key) {
    final PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();
    final double[] interpX0Diff = func
        .differentiateTwice(_method[0].interpolate(x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData()), x0Key).getData();
    return _method[1].interpolate(x1Values, interpX0Diff, x1Key);
  }

  /**
   * Gets the second derivative of the surface with respect to x0 at <code>(x0Key_i, x1Key_j)</code> for all i and j in the vectors.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Keys
   *          the x0 values for which the interpolated value is required
   * @param x1Keys
   *          the x1 values for which the interpolated value is required
   * @return Values of second derivative with respect to x0 at <code>(x0Key_i, x1Keys_j)</code>
   */
  public DoubleMatrix2D differentiateX0Twice(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double[] x0Keys,
      final double[] x1Keys) {
    final PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();
    final double[][] interpX0Diff = OG_ALGEBRA
        .getTranspose(func.differentiateTwice(_method[0].interpolate(x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData()), x0Keys))
        .getData();
    return _method[1].interpolate(x1Values, interpX0Diff, x1Keys);
  }

  /**
   * Gets the first derivative of the surface with respect to x1 at <code>(x0Key, x1Key)</code>.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Key
   *          the x0 value for which the interpolated value is required
   * @param x1Key
   *          the x1 value for which the interpolated value is required
   * @return Value of first derivative with respect to x1 at <code>(x0Key, x1Key)</code>
   */
  public double differentiateX1(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double x0Key, final double x1Key) {
    final PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();
    final double[] interpX1Diff = func.differentiate(_method[1].interpolate(x1Values, yValues), x1Key).getData();
    return _method[0].interpolate(x0Values, interpX1Diff, x0Key);
  }

  /**
   * Gets the first derivative of the surface with respect to x1 at <code>(x0Key_i, x1Key_j)</code> for all i and j in the vectors.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Keys
   *          the x0 values for which the interpolated value is required
   * @param x1Keys
   *          the x1 values for which the interpolated value is required
   * @return Values of first derivative with respect to x1 at <code>(x0Key_i, x1Keys_j)</code>
   */
  public DoubleMatrix2D differentiateX1(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double[] x0Keys,
      final double[] x1Keys) {
    final PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();
    final double[][] interpX1Diff = OG_ALGEBRA.getTranspose(func.differentiate(_method[1].interpolate(x1Values, yValues), x1Keys)).getData();
    return OG_ALGEBRA.getTranspose(_method[0].interpolate(x0Values, interpX1Diff, x0Keys));
  }

  /**
   * Gets the second derivative of the surface with respect to x1 at <code>(x0Key, x1Key)</code>.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Key
   *          the x0 value for which the interpolated value is required
   * @param x1Key
   *          the x1 value for which the interpolated value is required
   * @return Value of second derivative with respect to x1 at <code>(x0Key, x1Key)</code>
   */
  public double differentiateX1Twice(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double x0Key, final double x1Key) {
    final PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();
    final double[] interpX1Diff = func.differentiateTwice(_method[1].interpolate(x1Values, yValues), x1Key).getData();
    return _method[0].interpolate(x0Values, interpX1Diff, x0Key);
  }

  /**
   * Gets the second derivative of the surface with respect to x1 at <code>(x0Key_i, x1Key_j)</code> for all i and j in the vectors.
   *
   * @param x0Values
   *          the values in the x0 direction
   * @param x1Values
   *          the values in the x1 direction
   * @param yValues
   *          the values in the y direction
   * @param x0Keys
   *          the x0 values for which the interpolated value is required
   * @param x1Keys
   *          the x1 values for which the interpolated value is required
   * @return Values of second derivative with respect to x1 at <code>(x0Key_i, x1Keys_j)</code>
   */
  public DoubleMatrix2D differentiateX1Twice(final double[] x0Values, final double[] x1Values, final double[][] yValues, final double[] x0Keys,
      final double[] x1Keys) {
    final PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();
    final double[][] interpX1Diff = OG_ALGEBRA.getTranspose(func.differentiateTwice(_method[1].interpolate(x1Values, yValues), x1Keys)).getData();
    return OG_ALGEBRA.getTranspose(_method[0].interpolate(x0Values, interpX1Diff, x0Keys));
  }
}
