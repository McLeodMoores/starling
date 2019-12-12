/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import java.util.function.Function;

import org.apache.commons.lang.Validate;

/**
 * Dirichlet boundary condition, i.e. u(A,t) = f(t), where A is the boundary level, and f(t) is some specified function of time
 */
public class DirichletBoundaryCondition implements BoundaryCondition {

  private final Function<Double, Double> _timeValue;
  private final double _level;

  /**
   * Dirichlet boundary condition, i.e. u(A,t) = f(t), where A is the boundary level, and f(t) is some specified function of time
   * 
   * @param timeValue
   *          The value of u at the boundary, i.e. u(A,t) = f(t)
   * @param level
   *          The boundary level (A)
   */
  public DirichletBoundaryCondition(final Function<Double, Double> timeValue, final double level) {
    Validate.notNull(timeValue, "null timeValue");
    _timeValue = timeValue;
    _level = level;
  }

  /**
   * Special case of Dirichlet boundary condition, i.e. u(A,t) = constant, where A is the boundary level
   * 
   * @param fixedValue
   *          The constant value at the boundary
   * @param level
   *          The boundary level (A)
   */
  public DirichletBoundaryCondition(final double fixedValue, final double level) {
    _timeValue = x -> fixedValue;
    _level = level;
  }

  @Override
  public double getConstant(final ConvectionDiffusionPDE1DStandardCoefficients data, final double t) {
    return _timeValue.apply(t);
  }

  @Override
  public double getLevel() {
    return _level;
  }

  @Override
  public double[] getLeftMatrixCondition(final ConvectionDiffusionPDE1DStandardCoefficients data, final PDEGrid1D grid, final double t) {
    return new double[] { 1.0 };
  }

  @Override
  public double[] getRightMatrixCondition(final ConvectionDiffusionPDE1DStandardCoefficients data, final PDEGrid1D grid, final double t) {
    return new double[0];
  }

}
