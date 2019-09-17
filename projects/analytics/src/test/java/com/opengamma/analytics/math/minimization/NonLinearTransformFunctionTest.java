/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import static org.testng.Assert.assertEquals;

import java.util.BitSet;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NonLinearTransformFunctionTest {

  private static final ParameterLimitsTransform[] NULL_TRANSFORMS;
  private static final ParameterLimitsTransform[] TRANSFORMS;

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> FUNCTION = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
    @Override
    public DoubleMatrix1D apply(final DoubleMatrix1D x) {
      Validate.isTrue(x.getNumberOfElements() == 2);
      final double x1 = x.getEntry(0);
      final double x2 = x.getEntry(1);
      final double[] y = new double[3];
      y[0] = Math.sin(x1) * Math.cos(x2);
      y[1] = Math.sin(x1) * Math.sin(x2);
      y[2] = Math.cos(x1);
      return new DoubleMatrix1D(y);
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> JACOBIAN = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
    @Override
    public DoubleMatrix2D apply(final DoubleMatrix1D x) {
      Validate.isTrue(x.getNumberOfElements() == 2);
      final double x1 = x.getEntry(0);
      final double x2 = x.getEntry(1);
      final double[][] y = new double[3][2];
      y[0][0] = Math.cos(x1) * Math.cos(x2);
      y[0][1] = -Math.sin(x1) * Math.sin(x2);
      y[1][0] = Math.cos(x1) * Math.sin(x2);
      y[1][1] = Math.sin(x1) * Math.cos(x2);
      y[2][0] = -Math.sin(x1);
      y[2][1] = 0;
      return new DoubleMatrix2D(y);
    }
  };

  static {
    NULL_TRANSFORMS = new ParameterLimitsTransform[2];
    NULL_TRANSFORMS[0] = new NullTransform();
    NULL_TRANSFORMS[1] = new NullTransform();

    TRANSFORMS = new ParameterLimitsTransform[2];
    TRANSFORMS[0] = new DoubleRangeLimitTransform(0, Math.PI);
    TRANSFORMS[1] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
  }

  @Test
  public void testNullTransform() {
    final BitSet fixed = new BitSet();
    fixed.set(0);
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {Math.PI / 4, 1 });
    final UncoupledParameterTransforms transforms = new UncoupledParameterTransforms(start, NULL_TRANSFORMS, fixed);
    final NonLinearTransformFunction transFunc = new NonLinearTransformFunction(FUNCTION, JACOBIAN, transforms);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = transFunc.getFittingFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = transFunc.getFittingJacobian();

    final DoubleMatrix1D x = new DoubleMatrix1D(new double[] {0.5 });
    final double rootHalf = Math.sqrt(0.5);
    final DoubleMatrix1D y = func.apply(x);
    assertEquals(3, y.getNumberOfElements());
    assertEquals(rootHalf * Math.cos(0.5), y.getEntry(0), 1e-9);
    assertEquals(rootHalf * Math.sin(0.5), y.getEntry(1), 1e-9);
    assertEquals(rootHalf, y.getEntry(2), 1e-9);

    final DoubleMatrix2D jac = jacFunc.apply(x);
    assertEquals(3, jac.getNumberOfRows());
    assertEquals(1, jac.getNumberOfColumns());
    assertEquals(-rootHalf * Math.sin(0.5), jac.getEntry(0, 0), 1e-9);
    assertEquals(rootHalf * Math.cos(0.5), jac.getEntry(1, 0), 1e-9);
    assertEquals(0, jac.getEntry(2, 0), 1e-9);
  }

  @Test
  public void testNonLinearTransform() {
    final BitSet fixed = new BitSet();
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[2]);
    final UncoupledParameterTransforms transforms = new UncoupledParameterTransforms(start, TRANSFORMS, fixed);
    final NonLinearTransformFunction transFunc = new NonLinearTransformFunction(FUNCTION, JACOBIAN, transforms);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = transFunc.getFittingFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = transFunc.getFittingJacobian();

    final VectorFieldFirstOrderDifferentiator diff = new VectorFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFuncFD = diff.differentiate(func);

    final DoubleMatrix1D testPoint = new DoubleMatrix1D(new double[] {4.5, -2.1 });
    final DoubleMatrix2D jac = jacFunc.apply(testPoint);
    final DoubleMatrix2D jacFD = jacFuncFD.apply(testPoint);
    assertEquals(3, jac.getNumberOfRows());
    assertEquals(2, jac.getNumberOfColumns());

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals(jacFD.getEntry(i, j), jac.getEntry(i, j), 1e-6);
      }
    }
  }
}
