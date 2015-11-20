/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.COMMONS_ALGEBRA;
import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import java.util.Arrays;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.linearalgebra.QRDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.QRDecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.regression.LeastSquaresRegressionResult;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Derive coefficients of n-degree polynomial that minimizes least squares error of fit by using QR decomposition and back substitution
 */
public class PolynomialsLeastSquaresFitter {

  private QRDecompositionResult _qrResult;
  private final double[] _renorm = new double[2];

  /**
   * Given a set of data (X_i, Y_i) and degrees of a polynomial, determines optimal coefficients of the polynomial
   * @param xData X values of data
   * @param yData Y values of data
   * @param degree Degree of polynomial which fits the given data
   * @return LeastSquaresRegressionResult Containing optimal coefficients of the polynomial and difference between yData[i] and f(xData[i]),
   * where f() is the polynomial with the derived coefficients
   */
  public LeastSquaresRegressionResult regress(final double[] xData, final double[] yData, final int degree) {

    return regress(xData, yData, degree, false);
  }

  /**
   * Alternative regression method with different output
   * @param xData X values of data
   * @param yData Y values of data
   * @param degree Degree of polynomial which fits the given data
   * @param normalize Normalize xData by mean and standard deviation if normalize == true
   * @return PolynomialsLeastSquaresRegressionResult containing coefficients, rMatrix, degrees of freedom, norm of residuals, and mean, standard deviation
   */
  public PolynomialsLeastSquaresFitterResult regressVerbose(final double[] xData, final double[] yData, final int degree, final boolean normalize) {

    final LeastSquaresRegressionResult result = regress(xData, yData, degree, normalize);

    final int nData = xData.length;
    final DoubleMatrix2D rMatriX = _qrResult.getR();

    final DoubleMatrix1D resResult = new DoubleMatrix1D(result.getResiduals());
    final double resNorm = OG_ALGEBRA.getNorm2(resResult);

    if (normalize == true) {
      return new PolynomialsLeastSquaresFitterResult(result.getBetas(), rMatriX, nData - degree - 1, resNorm, _renorm);
    }
    return new PolynomialsLeastSquaresFitterResult(result.getBetas(), rMatriX, nData - degree - 1, resNorm);
  }

  /**
   * This regression method is private and called in other regression methods
   * @param xData X values of data
   * @param yData Y values of data
   * @param degree Degree of polynomial which fits the given data
   * @param normalize Normalize xData by mean and standard deviation if normalize == true
   * @return LeastSquaresRegressionResult Containing optimal coefficients of the polynomial and difference between yData[i] and f(xData[i])
   */
  private LeastSquaresRegressionResult regress(final double[] xData, final double[] yData, final int degree, final boolean normalize) {

    ArgumentChecker.notNull(xData, "xData");
    ArgumentChecker.notNull(yData, "yData");

    ArgumentChecker.isTrue(degree >= 0, "Minus degree");
    ArgumentChecker.isTrue(xData.length == yData.length, "xData length should be the same as yData length");
    ArgumentChecker.isTrue(xData.length > degree, "Not enough amount of data");

    final int nData = xData.length;

    for (int i = 0; i < nData; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xData[i]), "xData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xData[i]), "xData containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(yData[i]), "yData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(yData[i]), "yData containing Infinity");
    }

    for (int i = 0; i < nData; ++i) {
      for (int j = i + 1; j < nData; ++j) {
        ArgumentChecker.isFalse(xData[i] == xData[j] && yData[i] != yData[j], "Two distinct data on x=const. line");
      }
    }

    int nRepeat = 0;
    for (int i = 0; i < nData; ++i) {
      for (int j = i + 1; j < nData; ++j) {
        if (xData[i] == xData[j] && yData[i] == yData[j]) {
          ++nRepeat;
        }
      }
    }
    ArgumentChecker.isFalse(nRepeat > nData - degree - 1, "Too many repeated data");

    final double[][] tmpMatrix = new double[nData][degree + 1];

    if (normalize == true) {
      final double[] normData = normaliseData(xData);
      for (int i = 0; i < nData; ++i) {
        for (int j = 0; j < degree + 1; ++j) {
          tmpMatrix[i][j] = Math.pow(normData[i], j);
        }
      }
    } else {
      for (int i = 0; i < nData; ++i) {
        for (int j = 0; j < degree + 1; ++j) {
          tmpMatrix[i][j] = Math.pow(xData[i], j);
        }
      }
    }

    final DoubleMatrix2D xDataMatrix = new DoubleMatrix2D(tmpMatrix);
    final DoubleMatrix1D yDataVector = new DoubleMatrix1D(yData);

    final double vandNorm = COMMONS_ALGEBRA.getNorm2(xDataMatrix);
    ArgumentChecker.isFalse(vandNorm > 1e9, "Too large input data or too many degrees");

    return regress(xDataMatrix, yDataVector, nData, degree);

  }

  /**
   * This regression method is private and called in other regression methods
   * @param xDataMatrix _nData x (_degree + 1) matrix whose low vector is (xData[i]^0, xData[i]^1, ..., xData[i]^{_degree})
   * @param yDataVector yData of DoubleMatrix1D
   * @param nData Number of data points
   * @param degree
   */
  private LeastSquaresRegressionResult regress(final DoubleMatrix2D xDataMatrix, final DoubleMatrix1D yDataVector, final int nData, final int degree) {

    final Decomposition<QRDecompositionResult> qrComm = new QRDecompositionCommons();

    final DecompositionResult decompResult = qrComm.evaluate(xDataMatrix);
    _qrResult = (QRDecompositionResult) decompResult;

    final DoubleMatrix2D qMatrix = _qrResult.getQ();
    final DoubleMatrix2D rMatrix = _qrResult.getR();

    final double[] betas = backSubstitution(qMatrix, rMatrix, yDataVector, degree);
    final double[] residuals = residualsSolver(xDataMatrix, betas, yDataVector);

    for (int i = 0; i < degree + 1; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(betas[i]), "Input is too large or small");
    }
    for (int i = 0; i < nData; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(residuals[i]), "Input is too large or small");
    }

    return new LeastSquaresRegressionResult(betas, residuals, 0.0, null, 0.0, 0.0, null, null, true);

  }

  /**
   * Under the QR decomposition, xDataMatrix = qMatrix * rMatrix, optimal coefficients of the polynomial are computed by back substitution
   * @param qMatrix
   * @param rMatrix
   * @param yDataVector
   * @param degree
   * @return Coefficients of the polynomial which minimize least square
   */
  private double[] backSubstitution(final DoubleMatrix2D qMatrix, final DoubleMatrix2D rMatrix, final DoubleMatrix1D yDataVector, final int degree) {

    final double[] res = new double[degree + 1];
    Arrays.fill(res, 0.);

    final DoubleMatrix2D tpMatrix = OG_ALGEBRA.getTranspose(qMatrix);
    final DoubleMatrix1D yDataVecConv = (DoubleMatrix1D) OG_ALGEBRA.multiply(tpMatrix, yDataVector);

    final double[] yDataVecConvDoub = yDataVecConv.getData();
    final double[][] rMatrixDoub = rMatrix.getData();

    for (int i = 0; i < degree + 1; ++i) {
      double tmp = 0.;
      for (int j = 0; j < i; ++j) {
        tmp -= rMatrixDoub[degree - i][degree - j] * res[degree - j] / rMatrixDoub[degree - i][degree - i];
      }
      res[degree - i] = yDataVecConvDoub[degree - i] / rMatrixDoub[degree - i][degree - i] + tmp;
    }

    return res;
  }

  /**
   *
   * @param xDataMatrix
   * @param betas Optimal coefficients of the polynomial
   * @param yDataVector
   * @return Difference between yData[i] and f(xData[i]), where f() is the polynomial with derived coefficients
   */
  private double[] residualsSolver(final DoubleMatrix2D xDataMatrix, final double[] betas, final DoubleMatrix1D yDataVector) {

    final DoubleMatrix1D betasVector = new DoubleMatrix1D(betas);

    final DoubleMatrix1D modelValuesVector = (DoubleMatrix1D) OG_ALGEBRA.multiply(xDataMatrix, betasVector);
    final DoubleMatrix1D res = (DoubleMatrix1D) OG_ALGEBRA.subtract(yDataVector, modelValuesVector);

    return res.getData();

  }

  /**
   * Normalize x_i as x_i -> (x_i - mean)/(standard deviation)
   * @param xData X values of data
   * @return Normalized X values
   */
  private double[] normaliseData(final double[] xData) {

    final int nData = xData.length;
    final double[] res = new double[nData];

    Function1D<double[], Double> calculator = new MeanCalculator();
    _renorm[0] = calculator.evaluate(xData);
    calculator = new SampleStandardDeviationCalculator();
    _renorm[1] = calculator.evaluate(xData);

    final double tmp = _renorm[0] / _renorm[1];
    for (int i = 0; i < nData; ++i) {
      res[i] = xData[i] / _renorm[1] - tmp;
    }

    return res;
  }

}
