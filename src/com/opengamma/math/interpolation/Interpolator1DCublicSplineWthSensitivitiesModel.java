/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import com.opengamma.math.linearalgebra.TridiagonalMatrixInvertor;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class Interpolator1DCublicSplineWthSensitivitiesModel implements Interpolator1DWithSensitivitiesModel {

  private final Interpolator1DModel _underlyingData;
  private final DoubleMatrix1D _secondDerivatives;
  private final DoubleMatrix2D _secondDevSensitivities;

  private final double leftFirstDev = 0;
  private final double rightFirstDev = 0;
  private final Boolean leftNatural = true;
  private final Boolean rightNatural = true;

  public Interpolator1DCublicSplineWthSensitivitiesModel(Interpolator1DModel underlyingData) {
    _underlyingData = underlyingData;

    final double[] x = underlyingData.getKeys();
    final double[] y = underlyingData.getValues();
    final int n = x.length;
    double[] deltaX = new double[n - 1];
    double[] deltaYOverDeltaX = new double[n - 1];
    double[] oneOverDeltaX = new double[n - 1];

    for (int i = 0; i < n - 1; i++) {
      deltaX[i] = x[i + 1] - x[i];
      oneOverDeltaX[i] = 1.0 / deltaX[i];
      deltaYOverDeltaX[i] = (y[i + 1] - y[i]) * oneOverDeltaX[i];
    }

    DoubleMatrix2D inverseTriDiag = getInverseTriDiag(deltaX);
    DoubleMatrix2D rhsMatrix = getRHSMatrix(oneOverDeltaX);
    DoubleMatrix1D rhsVector = getRHSVector(deltaYOverDeltaX);
    _secondDevSensitivities = (DoubleMatrix2D) OG_ALGEBRA.multiply(inverseTriDiag, rhsMatrix);
    _secondDerivatives = (DoubleMatrix1D) OG_ALGEBRA.multiply(inverseTriDiag, rhsVector);
  }

  private DoubleMatrix2D getInverseTriDiag(double[] deltaX) {

    final int n = deltaX.length + 1;

    double[] a = new double[n];
    double[] b = new double[n - 1];
    double[] c = new double[n - 1];

    for (int i = 1; i < n - 1; i++) {
      a[i] = (deltaX[i - 1] + deltaX[i]) / 3.0;
      b[i] = deltaX[i] / 6.0;
      c[i - 1] = deltaX[i - 1] / 6.0;
    }

    //Boundary condition
    if (leftNatural) {
      a[0] = 1.0;
      b[0] = 0.0;
    } else {
      a[0] = -deltaX[0] / 3.0;
      b[0] = deltaX[0] / 6.0;
    }

    if (rightNatural) {
      a[n - 1] = 1.0;
      c[n - 2] = 0.0;
    } else {
      a[n - 1] = deltaX[n - 2] / 3.0;
      c[n - 2] = deltaX[n - 2] / 6.0;
    }

    return TridiagonalMatrixInvertor.getInverse(a, b, c);
  }

  private DoubleMatrix2D getRHSMatrix(double[] oneOverDeltaX) {
    final int n = oneOverDeltaX.length + 1;

    double[][] res = new double[n][n];
    for (int i = 1; i < n - 1; i++) {
      res[i][i - 1] = oneOverDeltaX[i - 1];
      res[i][i] = -oneOverDeltaX[i] - oneOverDeltaX[i - 1];
      res[i][i + 1] = oneOverDeltaX[i];
    }
    if (!leftNatural) {
      res[0][0] = oneOverDeltaX[0];
      res[0][1] = -oneOverDeltaX[0];
    }

    if (!rightNatural) {
      res[n - 1][n - 1] = -oneOverDeltaX[n - 2];
      res[n - 2][n - 2] = oneOverDeltaX[n - 2];
    }
    return new DoubleMatrix2D(res);
  }

  private DoubleMatrix1D getRHSVector(double[] deltaYOverDeltaX) {
    final int n = deltaYOverDeltaX.length + 1;
    double[] res = new double[n];

    for (int i = 1; i < n - 1; i++) {
      res[i] = deltaYOverDeltaX[i] - deltaYOverDeltaX[i - 1];
    }
    if (!leftNatural) {
      res[0] = leftFirstDev - deltaYOverDeltaX[0];
    }

    if (!rightNatural) {
      res[n - 1] = rightFirstDev - deltaYOverDeltaX[n - 2];
    }
    return new DoubleMatrix1D(res);
  }

  public DoubleMatrix1D getSecondDerivatives() {
    return _secondDerivatives;
  }

  public DoubleMatrix2D getSecondDerivativiesSensitivities() {
    return _secondDevSensitivities;
  }

  @Override
  public boolean containsKey(final Double key) {
    return _underlyingData.containsKey(key);
  }

  @Override
  public Double firstKey() {
    return _underlyingData.firstKey();
  }

  @Override
  public Double firstValue() {
    return _underlyingData.firstValue();
  }

  @Override
  public Double get(final Double key) {
    return _underlyingData.get(key);
  }

  @Override
  public InterpolationBoundedValues getBoundedValues(final Double key) {
    return _underlyingData.getBoundedValues(key);
  }

  @Override
  public double[] getKeys() {
    return _underlyingData.getKeys();
  }

  @Override
  public int getLowerBoundIndex(final Double value) {
    return _underlyingData.getLowerBoundIndex(value);
  }

  @Override
  public Double getLowerBoundKey(final Double value) {
    return _underlyingData.getLowerBoundKey(value);
  }

  @Override
  public double[] getValues() {
    return _underlyingData.getValues();
  }

  @Override
  public Double higherKey(final Double key) {
    return _underlyingData.higherKey(key);
  }

  @Override
  public Double higherValue(final Double key) {
    return _underlyingData.higherValue(key);
  }

  @Override
  public Double lastKey() {
    return _underlyingData.lastKey();
  }

  @Override
  public Double lastValue() {
    return _underlyingData.lastValue();
  }

  @Override
  public int size() {
    return _underlyingData.size();
  }

}
