/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * OpenGamma implementation of the Cholesky decomposition and its differentiation.
 */
public class CholeskyDecompositionOpenGamma extends Decomposition<CholeskyDecompositionResult> {

  /**
   * The input matrix symmetry is checked. If the relative difference abs(Aij-Aji) &gt; max(abs(Aij), abs(Aji)) * e_sym, the matrix is considered non-symmetric.
   * The default value for the threshold e_sym.
   */
  public static final double DEFAULT_SYMMETRY_THRESHOLD = 1.0E-10;
  /**
   * In the decomposition, the positivity of the matrix is checked. If the absolute value Lii &lt; e_pos the matrix is considered non-positive. The default
   * value for the threshold e_pos.
   */
  public static final double DEFAULT_POSITIVITY_THRESHOLD = 1.0E-10;

  /**
   * {@inheritDoc}
   */
  @Override
  public CholeskyDecompositionResult evaluate(final DoubleMatrix2D x) {
    return evaluate(x, DEFAULT_SYMMETRY_THRESHOLD, DEFAULT_POSITIVITY_THRESHOLD);
  }

  /**
   * Perform the decomposition with a given symmetry and positivity threshold.
   * @param matrix The matrix to decompose.
   * @param symmetryThreshold The symmetry threshold.
   * @param positivityThreshold The positivity threshold.
   * @return The Cholesky decomposition.
   */
  public CholeskyDecompositionResult evaluate(final DoubleMatrix2D matrix, final double symmetryThreshold, final double positivityThreshold) {
    Validate.notNull(matrix, "Matrix null");
    final int nbRow = matrix.getNumberOfRows();
    final int nbCol = matrix.getNumberOfColumns();
    Validate.isTrue(nbRow == nbCol, "Matrix not square");
    final double[][] l = new double[nbRow][nbRow];
    // Check symmetry and initial fill of _lTArray
    final double[][] matrixData = matrix.getData();
    for (int looprow = 0; looprow < nbRow; looprow++) {
      for (int loopcol = 0; loopcol <= looprow; loopcol++) {
        final double maxValue = Math.max(Math.abs(matrixData[looprow][loopcol]), Math.abs(matrixData[loopcol][looprow]));
        final double diff = Math.abs(matrixData[looprow][loopcol] - matrixData[loopcol][looprow]);
        Validate.isTrue(diff <= maxValue * symmetryThreshold, "Matrix not symmetrical");
        l[looprow][loopcol] = matrixData[looprow][loopcol];
      }
    }
    // The decomposition
    for (int loopcol = 0; loopcol < nbCol; loopcol++) {
      Validate.isTrue(l[loopcol][loopcol] > positivityThreshold, "Matrix not positive");
      l[loopcol][loopcol] = Math.sqrt(l[loopcol][loopcol]); // Pivot
      final double lInverse = 1.0 / l[loopcol][loopcol];
      for (int looprow = loopcol + 1; looprow < nbRow; looprow++) { // Current column
        l[looprow][loopcol] *= lInverse;
      }
      for (int j = loopcol + 1; j < nbRow; j++) { // Other columns
        for (int i = j; i < nbRow; i++) {
          l[i][j] -= l[i][loopcol] * l[j][loopcol];
        }
      }
    }
    return new CholeskyDecompositionOpenGammaResult(l);
  }

}
