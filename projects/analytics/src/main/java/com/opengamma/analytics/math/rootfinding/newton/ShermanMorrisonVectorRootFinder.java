/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;

/**
 * Uses the Sherman-Morrison formula to invert Broyden's Jacobian update formula, thus providing a direct update formula for the inverse Jacobian.
 */
@VectorRootFinderType(name = "Sherman-Morrison")
public class ShermanMorrisonVectorRootFinder extends NewtonVectorRootFinder {
  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;

  /**
   * Constructs the calculator using the default tolerances (1e-7), number of steps (100) and matrix decomposition method (LU).
   */
  public ShermanMorrisonVectorRootFinder() {
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
  public ShermanMorrisonVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps) {
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
  public ShermanMorrisonVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, final Decomposition<?> decomp) {
    this(absoluteTol, relativeTol, maxSteps, decomp, new OGMatrixAlgebra());
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
   * @param algebra
   *          the matrix algebra calculator, not null
   */
  public ShermanMorrisonVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, final Decomposition<?> decomp,
      final MatrixAlgebra algebra) {
    super(absoluteTol, relativeTol, maxSteps, new InverseJacobianDirectionFunction(algebra), new InverseJacobianEstimateInitializationFunction(decomp),
        new ShermanMorrisonMatrixUpdateFunction(algebra));
  }

  @Override
  public String getName() {
    return "Sherman-Morrison";
  }

}
