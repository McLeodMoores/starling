/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public interface NewtonRootFinderMatrixUpdateFunction {

  // TODO might be better to pass in NewtonVectorRootFinder.DataBundle as many of these arguments are not used.
  DoubleMatrix2D getUpdatedMatrix(Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFunction, DoubleMatrix1D x, DoubleMatrix1D deltaX, DoubleMatrix1D deltaY, DoubleMatrix2D matrix);
}
