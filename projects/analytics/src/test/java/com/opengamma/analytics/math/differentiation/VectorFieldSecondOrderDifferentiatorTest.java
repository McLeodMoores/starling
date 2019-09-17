/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.differentiation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VectorFieldSecondOrderDifferentiatorTest {

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> FUNC = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D apply(final DoubleMatrix1D x) {
      final double a = x.getEntry(0);
      final double theta = x.getEntry(1);
      final double[] temp = new double[2];
      final double c1 = Math.cos(theta);
      temp[0] = a * c1 * c1;
      temp[1] = a * (1 - c1 * c1);
      return new DoubleMatrix1D(temp);
    }
  };

  private static Function1D<DoubleMatrix1D, Boolean> DOMAIN = new Function1D<DoubleMatrix1D, Boolean>() {

    @Override
    public Boolean apply(final DoubleMatrix1D x) {
      final double a = x.getEntry(0);
      final double theta = x.getEntry(1);
      if (a <= 0) {
        return false;
      }
      if (theta < 0.0 || theta > Math.PI) {
        return false;
      }
      return true;
    }
  };

  private static Function1D<DoubleMatrix1D, DoubleMatrix2D> DW1 = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
    @Override
    public DoubleMatrix2D apply(final DoubleMatrix1D x) {
      final double a = x.getEntry(0);
      final double theta = x.getEntry(1);
      final double[][] temp = new double[2][2];
      final double c1 = Math.cos(theta);
      final double s1 = Math.sin(theta);
      temp[0][0] = 0.0;
      temp[1][1] = 2 * a * (1 - 2 * c1 * c1);
      temp[0][1] = -2 * s1 * c1;
      temp[1][0] = temp[0][1];
      return new DoubleMatrix2D(temp);
    }
  };

  private static Function1D<DoubleMatrix1D, DoubleMatrix2D> DW2 = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
    @Override
    public DoubleMatrix2D apply(final DoubleMatrix1D x) {
      final double a = x.getEntry(0);
      final double theta = x.getEntry(1);
      final double[][] temp = new double[2][2];
      final double c1 = Math.cos(theta);
      final double s1 = Math.sin(theta);
      temp[0][0] = 0.0;
      temp[1][1] = 2 * a * (2 * c1 * c1 - 1);
      temp[0][1] = 2 * s1 * c1;
      temp[1][0] = temp[0][1];
      return new DoubleMatrix2D(temp);
    }
  };

  @Test
  public void test() {
    final double a = 2.3;
    final double theta = 0.34;
    final DoubleMatrix1D x = new DoubleMatrix1D(new double[] {a, theta });

    final VectorFieldSecondOrderDifferentiator fd = new VectorFieldSecondOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D[]> fdFuncs = fd.differentiate(FUNC);
    final DoubleMatrix2D[] fdValues = fdFuncs.apply(x);

    final DoubleMatrix2D t1 = DW1.apply(x);
    final DoubleMatrix2D t2 = DW2.apply(x);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals("first observation " + i + " " + j, t1.getEntry(i, j), fdValues[0].getEntry(i, j), 1e-6);
        assertEquals("second observation " + i + " " + j, t2.getEntry(i, j), fdValues[1].getEntry(i, j), 1e-6);
      }
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outsideDomainTest() {
    final VectorFieldSecondOrderDifferentiator fd = new VectorFieldSecondOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D[]> fdFuncs = fd.differentiate(FUNC, DOMAIN);
    fdFuncs.apply(new DoubleMatrix1D(-1.0, 0.3));
  }

  @Test
  public void domainTest() {

    final DoubleMatrix1D[] x = new DoubleMatrix1D[4];
    x[0] = new DoubleMatrix1D(2.3, 0.34);
    x[1] = new DoubleMatrix1D(1e-8, 1.45);
    x[2] = new DoubleMatrix1D(1.2, 0.0);
    x[3] = new DoubleMatrix1D(1.2, Math.PI);

    final VectorFieldSecondOrderDifferentiator fd = new VectorFieldSecondOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D[]> fdFuncs = fd.differentiate(FUNC, DOMAIN);

    for (int k = 0; k < 4; k++) {
      final DoubleMatrix2D[] fdValues = fdFuncs.apply(x[k]);
      final DoubleMatrix2D t1 = DW1.apply(x[k]);
      final DoubleMatrix2D t2 = DW2.apply(x[k]);
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          assertEquals("first observation " + i + " " + j, t1.getEntry(i, j), fdValues[0].getEntry(i, j), 1e-6);
          assertEquals("second observation " + i + " " + j, t2.getEntry(i, j), fdValues[1].getEntry(i, j), 1e-6);
        }
      }
    }
  }

}
