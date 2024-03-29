/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * C2 cubic spline interpolator with Clamped/Not-A-Knot endpoint conditions.
 */
public class CubicSplineInterpolator extends PiecewisePolynomialInterpolator {

  private CubicSplineSolver _solver;

  /**
   * If (xValues length) = (yValues length), Not-A-Knot endpoint conditions are used If (xValues length) + 2 = (yValues length), Clamped endpoint conditions are
   * used.
   *
   * @param xValues
   *          X values of data
   * @param yValues
   *          Y values of data
   * @return {@link PiecewisePolynomialResult} containing knots, coefficients of piecewise polynomials, number of intervals, degree of polynomials, dimension of
   *         spline
   */
  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[] yValues) {

    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValues, "yValues");

    ArgumentChecker.isTrue(xValues.length == yValues.length | xValues.length + 2 == yValues.length,
        "(xValues length = yValues length) or (xValues length + 2 = yValues length)");
    ArgumentChecker.isTrue(xValues.length > 1, "Data points should be more than 1");

    final int nDataPts = xValues.length;
    final int nYdata = yValues.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xData containing Infinity");
    }
    for (int i = 0; i < nYdata; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(yValues[i]), "yData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(yValues[i]), "yData containing Infinity");
    }

    for (int i = 0; i < nDataPts; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "Data should be distinct");
      }
    }

    double[] xValuesSrt = new double[nDataPts];
    double[] yValuesSrt = new double[nDataPts];

    xValuesSrt = Arrays.copyOf(xValues, nDataPts);

    if (xValues.length + 2 == yValues.length) {
      _solver = new CubicSplineClampedSolver(yValues[0], yValues[nDataPts + 1]);
      yValuesSrt = Arrays.copyOfRange(yValues, 1, nDataPts + 1);
    } else {
      _solver = new CubicSplineNakSolver();
      yValuesSrt = Arrays.copyOf(yValues, nDataPts);
    }
    ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

    final DoubleMatrix2D coefMatrix = _solver.solve(xValuesSrt, yValuesSrt);
    final int nCoefs = coefMatrix.getNumberOfColumns();

    for (int i = 0; i < _solver.getKnotsMat1D(xValuesSrt).getNumberOfElements() - 1; ++i) {
      for (int j = 0; j < nCoefs; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(coefMatrix.getData()[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(coefMatrix.getData()[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(_solver.getKnotsMat1D(xValuesSrt), coefMatrix, nCoefs, 1);

  }

  /**
   * If (xValues length) = (yValuesMatrix NumberOfColumn), Not-A-Knot endpoint conditions are used If (xValues length) + 2 = (yValuesMatrix NumberOfColumn),
   * Clamped endpoint conditions are used.
   *
   * @param xValues
   *          X values of data
   * @param yValuesMatrix
   *          Y values of data, where NumberOfRow defines dimension of the spline
   * @return {@link PiecewisePolynomialResult} containing knots, coefficients of piecewise polynomials, number of intervals, degree of polynomials, dimension of
   *         spline
   */
  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[][] yValuesMatrix) {

    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValuesMatrix, "yValuesMatrix");

    ArgumentChecker.isTrue(xValues.length == yValuesMatrix[0].length | xValues.length + 2 == yValuesMatrix[0].length,
        "(xValues length = yValuesMatrix's row vector length) or (xValues length + 2 = yValuesMatrix's row vector length)");
    ArgumentChecker.isTrue(xValues.length > 1, "Data points should be more than 1");

    final int nDataPts = xValues.length;
    final int nYdata = yValuesMatrix[0].length;
    final int dim = yValuesMatrix.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xData containing Infinity");
    }
    for (int i = 0; i < nYdata; ++i) {
      for (int j = 0; j < dim; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(yValuesMatrix[j][i]), "yValuesMatrix containing NaN");
        ArgumentChecker.isFalse(Double.isInfinite(yValuesMatrix[j][i]), "yValuesMatrix containing Infinity");
      }
    }

    for (int k = 0; k < dim; ++k) {
      for (int i = 0; i < nDataPts; ++i) {
        for (int j = i + 1; j < nDataPts; ++j) {
          ArgumentChecker.isFalse(xValues[i] == xValues[j], "Data should be distinct");
        }
      }
    }

    double[] xValuesSrt = new double[nDataPts];
    final double[][] yValuesMatrixSrt = new double[dim][nDataPts];

    if (xValues.length + 2 == yValuesMatrix[0].length) {
      final double[] iniConds = new double[dim];
      final double[] finConds = new double[dim];
      for (int i = 0; i < dim; ++i) {
        iniConds[i] = yValuesMatrix[i][0];
        finConds[i] = yValuesMatrix[i][nDataPts + 1];
      }
      _solver = new CubicSplineClampedSolver(iniConds, finConds);

      for (int i = 0; i < dim; ++i) {
        xValuesSrt = Arrays.copyOf(xValues, nDataPts);
        final double[] yValuesSrt = Arrays.copyOfRange(yValuesMatrix[i], 1, nDataPts + 1);
        ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

        yValuesMatrixSrt[i] = Arrays.copyOf(yValuesSrt, nDataPts);
      }
    } else {
      _solver = new CubicSplineNakSolver();
      for (int i = 0; i < dim; ++i) {
        xValuesSrt = Arrays.copyOf(xValues, nDataPts);
        final double[] yValuesSrt = Arrays.copyOf(yValuesMatrix[i], nDataPts);
        ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

        yValuesMatrixSrt[i] = Arrays.copyOf(yValuesSrt, nDataPts);
      }
    }

    final DoubleMatrix2D[] coefMatrix = _solver.solveMultiDim(xValuesSrt, new DoubleMatrix2D(yValuesMatrixSrt));

    final int nIntervals = coefMatrix[0].getNumberOfRows();
    final int nCoefs = coefMatrix[0].getNumberOfColumns();
    final double[][] resMatrix = new double[dim * nIntervals][nCoefs];

    for (int i = 0; i < nIntervals; ++i) {
      for (int j = 0; j < dim; ++j) {
        resMatrix[dim * i + j] = coefMatrix[j].getRowVector(i).getData();
      }
    }

    for (int i = 0; i < dim * nIntervals; ++i) {
      for (int j = 0; j < nCoefs; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(resMatrix[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(resMatrix[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(_solver.getKnotsMat1D(xValuesSrt), new DoubleMatrix2D(resMatrix), nCoefs, dim);
  }

  @Override
  public PiecewisePolynomialResultsWithSensitivity interpolateWithSensitivity(final double[] xValues, final double[] yValues) {
    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValues, "yValues");

    ArgumentChecker.isTrue(xValues.length == yValues.length | xValues.length + 2 == yValues.length,
        "(xValues length = yValues length) or (xValues length + 2 = yValues length)");
    ArgumentChecker.isTrue(xValues.length > 1, "Data points should be more than 1");

    final int nDataPts = xValues.length;
    final int nYdata = yValues.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xData containing Infinity");
    }
    for (int i = 0; i < nYdata; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(yValues[i]), "yData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(yValues[i]), "yData containing Infinity");
    }

    for (int i = 0; i < nDataPts; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "Data should be distinct");
      }
    }

    double[] yValuesSrt = new double[nDataPts];

    if (xValues.length + 2 == yValues.length) {
      _solver = new CubicSplineClampedSolver(yValues[0], yValues[nDataPts + 1]);
      yValuesSrt = Arrays.copyOfRange(yValues, 1, nDataPts + 1);
    } else {
      _solver = new CubicSplineNakSolver();
      yValuesSrt = Arrays.copyOf(yValues, nDataPts);
    }

    final DoubleMatrix2D[] resMatrix = _solver.solveWithSensitivity(xValues, yValuesSrt);
    final int len = resMatrix.length;
    for (int k = 0; k < len; k++) {
      final DoubleMatrix2D m = resMatrix[k];
      final int rows = m.getNumberOfRows();
      final int cols = m.getNumberOfColumns();
      for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
          ArgumentChecker.isTrue(Doubles.isFinite(m.getEntry(i, j)), "Matrix contains a NaN or infinite");
        }
      }
    }

    final DoubleMatrix2D coefMatrix = resMatrix[0];
    final DoubleMatrix2D[] coefSenseMatrix = new DoubleMatrix2D[len - 1];
    System.arraycopy(resMatrix, 1, coefSenseMatrix, 0, len - 1);
    final int nCoefs = coefMatrix.getNumberOfColumns();

    return new PiecewisePolynomialResultsWithSensitivity(_solver.getKnotsMat1D(xValues), coefMatrix, nCoefs, 1, coefSenseMatrix);
  }
}
