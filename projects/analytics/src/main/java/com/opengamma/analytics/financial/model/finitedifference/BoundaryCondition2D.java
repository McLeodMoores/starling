/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

/**
 *
 */
public interface BoundaryCondition2D {

  double[] getLeftMatrixCondition(double t, double boundaryPosition);

  double[] getRightMatrixCondition(double t, double boundaryPosition);

  double getConstant(double t, double boundaryPosition, double gridSpacing);

  double getLevel();

}
