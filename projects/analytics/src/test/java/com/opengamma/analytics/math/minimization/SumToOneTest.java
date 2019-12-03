/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.test.TestGroup;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SumToOneTest {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare(DecompositionFactory.SV_COLT, MA, 1e-9);
  private static final VectorFieldFirstOrderDifferentiator DIFFER = new VectorFieldFirstOrderDifferentiator();
  static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test
  public void setTest() {
    final int n = 7;
    final int[][] sets = SumToOne.getSet(n);
    assertEquals(n, sets.length);
  }

  @Test
  public void setTest2() {
    final int n = 13;
    final int[][] sets = SumToOne.getSet(n);
    assertEquals(n, sets.length);
  }

  @Test
  public void transformTest() {
    for (int n = 2; n < 13; n++) {
      final double[] from = new double[n - 1];
      for (int j = 0; j < n - 1; j++) {
        from[j] = RANDOM.nextDouble() * Math.PI / 2;
      }
      final SumToOne trans = new SumToOne(n);
      final DoubleMatrix1D to = trans.transform(new DoubleMatrix1D(from));
      assertEquals(n, to.getNumberOfElements());
      double sum = 0;
      for (int i = 0; i < n; i++) {
        sum += to.getEntry(i);
      }
      assertEquals("vector length " + n, 1.0, sum, 1e-9);
    }
  }

  @Test
  public void inverseTransformTest() {
    for (int n = 2; n < 13; n++) {
      final double[] theta = new double[n - 1];
      for (int j = 0; j < n - 1; j++) {
        theta[j] = RANDOM.nextDouble() * Math.PI / 2;
      }
      final SumToOne trans = new SumToOne(n);
      final DoubleMatrix1D w = trans.transform(new DoubleMatrix1D(theta));

      final DoubleMatrix1D theta2 = trans.inverseTransform(w);
      for (int j = 0; j < n - 1; j++) {
        assertEquals("element " + j + ", of vector length " + n, theta[j], theta2.getEntry(j), 1e-9);
      }
    }
  }

  @Test
  public void solverTest() {
    final double[] w = new double[] {0.01, 0.5, 0.3, 0.19 };
    final int n = w.length;
    final SumToOne trans = new SumToOne(n);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D theta) {
        return trans.transform(theta);
      }
    };

    final DoubleMatrix1D sigma = new DoubleMatrix1D(n, 1e-4);
    final DoubleMatrix1D start = new DoubleMatrix1D(n - 1, 0.8);
    //  DoubleMatrix1D maxJump = new DoubleMatrix1D(n - 1, Math.PI / 20);

    final LeastSquareResults res = SOLVER.solve(new DoubleMatrix1D(w), sigma, func, start/*, maxJump*/);
    assertEquals("chi sqr", 0.0, res.getChiSq(), 1e-9);
    final double[] fit = res.getFitParameters().getData();
    final double[] expected = trans.inverseTransform(w);
    for (int i = 0; i < n - 1; i++) {
      //put the fit result back in the range 0 - pi/2
      double x = fit[i];
      if (x < 0) {
        x = -x;
      }
      if (x > Math.PI / 2) {
        final int p = (int) (x / Math.PI);
        x -= p * Math.PI;
        if (x > Math.PI / 2) {
          x = -x + Math.PI;
        }
      }

      assertEquals(expected[i], x, 1e-9);
    }

  }

  @Test
  public void solverTest2() {
    final double[] w = new double[] {3.0, 4.0 };
    final int n = w.length;
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double a = x.getEntry(0);
        final double theta = x.getEntry(1);
        final double[] temp = new double[2];
        final double c1 = Math.cos(theta);
        temp[0] = a * c1 * c1;
        temp[1] = a * (1 - c1 * c1);
        return new DoubleMatrix1D(temp);
      }
    };

    final DoubleMatrix1D sigma = new DoubleMatrix1D(n, 1e-4);
    final DoubleMatrix1D start = new DoubleMatrix1D(0.0, 0.8);
    //  DoubleMatrix1D maxJump = new DoubleMatrix1D(1.0, Math.PI / 20);

    final LeastSquareResults res = SOLVER.solve(new DoubleMatrix1D(w), sigma, func, start/*, maxJump*/);
    assertEquals("chi sqr", 0.0, res.getChiSq(), 1e-9);
    final double[] fit = res.getFitParameters().getData();
    assertEquals(7.0, fit[0], 1e-9);
    assertEquals(Math.atan(Math.sqrt(4 / 3.)), fit[1], 1e-9);
  }

  @Test
  public void jacobianTest() {
    final int n = 5;

    final SumToOne trans = new SumToOne(n);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D theta) {
        return trans.transform(theta);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D theta) {
        return trans.jacobian(theta);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> fdJacFunc = DIFFER.differentiate(func);

    final double[] theta = new double[n - 1];
    for (int tries = 0; tries < 10; tries++) {

      for (int i = 0; i < n - 1; i++) {
        theta[i] = RANDOM.nextDouble();
      }
      final DoubleMatrix1D vTheta = new DoubleMatrix1D(theta);
      final DoubleMatrix2D jac = jacFunc.evaluate(vTheta);
      final DoubleMatrix2D fdJac = fdJacFunc.evaluate(vTheta);
      for (int j = 0; j < n - 1; j++) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
          sum += jac.getEntry(i, j);
          assertEquals("element " + i + " " + j, fdJac.getEntry(i, j), jac.getEntry(i, j), 1e-6);
        }
        assertEquals("wrong sum of sensitivities", 0.0, sum, 1e-15);
      }

    }
  }
}
