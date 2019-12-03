/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

/**
 * Represents a boundary condition for a PDE solver that has time dependent characteristics but is fixed at a space level.
 */
public interface BoundaryCondition {

  double[] getLeftMatrixCondition(ConvectionDiffusionPDE1DStandardCoefficients data, PDEGrid1D grid, double t);

  double[] getRightMatrixCondition(ConvectionDiffusionPDE1DStandardCoefficients data, PDEGrid1D grid, double t);

  double getConstant(ConvectionDiffusionPDE1DStandardCoefficients data, double t);

  double getLevel();

}
