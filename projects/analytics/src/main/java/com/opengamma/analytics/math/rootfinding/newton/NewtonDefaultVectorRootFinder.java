/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;

/**
 * Attempts to find the multi-dimensional root of a series of N equations with N variables, i.e. a square problem. If the analytic Jacobian is not known, it
 * will be calculated using central difference
 */
@VectorRootFinderType(name = "Default Newton")
public class NewtonDefaultVectorRootFinder extends NewtonVectorRootFinder {
  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;

  /**
   * Constructs the calculator using the default tolerances (1e-7), number of steps (100) and matrix decomposition method (LU).
   */
  public NewtonDefaultVectorRootFinder() {
    this(DEF_TOL, DEF_TOL, MAX_STEPS);
  }

  /**
   * Constructs the calculator using the default matrix decomposition method (LU).
   *
   * @param absoluteTol
   *          tolerance below which the root is deemed to have been found, greater than zero
   * @param relativeTol
   *          tolerance below which the root is deemed to have been found, greater than zero
   * @param maxSteps
   *          the maximum number of steps used in the root-finding, greater than zero
   */
  public NewtonDefaultVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps) {
    this(absoluteTol, relativeTol, maxSteps, new LUDecompositionCommons());
  }

  /**
   * Constructs the calculator.
   *
   * @param absoluteTol
   *          tolerance below which the root is deemed to have been found, greater than zero
   * @param relativeTol
   *          tolerance below which the root is deemed to have been found, greater than zero
   * @param maxSteps
   *          the maximum number of steps used in the root-finding, greater than zero
   * @param decomp
   *          the matrix decomposition calculator, not null
   */
  public NewtonDefaultVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, final Decomposition<?> decomp) {
    super(absoluteTol, relativeTol, maxSteps, new JacobianDirectionFunction(decomp), new JacobianEstimateInitializationFunction(),
        new NewtonDefaultUpdateFunction());
  }

  @Override
  public String getName() {
    return "Default Newton";
  }
}
