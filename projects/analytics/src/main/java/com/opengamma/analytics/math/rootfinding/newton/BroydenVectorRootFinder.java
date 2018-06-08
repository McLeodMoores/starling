/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;

/**
 *  Uses Broyden's Jacobian update formula.
 */
@VectorRootFinderType(name = "Broyden")
public class BroydenVectorRootFinder extends NewtonVectorRootFinder {
  private static final double DEF_TOL = 1e-7;
  private static final int MAX_STEPS = 100;

  /**
   * Constructs the calculator using the default tolerances (1e-7), number of steps (100) and
   * matrix decomposition method (LU).
   */
  public BroydenVectorRootFinder() {
    this(DEF_TOL, DEF_TOL, MAX_STEPS);
  }

  /**
   * Constructs the calculator using the default matrix decomposition method (LU).
   *
   * @param absoluteTol  tolerance below which the root is deemed to have been found, greater than zero
   * @param relativeTol  tolerance below which the root is deemed to have been found, greater than zero
   * @param maxSteps  the maximum number of steps used in the root-finding, greater than zero
   */
  public BroydenVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps) {
    this(absoluteTol, relativeTol, maxSteps, new LUDecompositionCommons());
  }

  /**
   * Constructs the calculator.
   *
   * @param absoluteTol  tolerance below which the root is deemed to have been found, greater than zero
   * @param relativeTol  tolerance below which the root is deemed to have been found, greater than zero
   * @param maxSteps  the maximum number of steps used in the root-finding, greater than zero
   * @param decomp  the matrix decomposition calculator, not null
   */
  public BroydenVectorRootFinder(final double absoluteTol, final double relativeTol, final int maxSteps, final Decomposition<?> decomp) {
    super(absoluteTol, relativeTol, maxSteps, new JacobianDirectionFunction(decomp), new JacobianEstimateInitializationFunction(),
        new BroydenMatrixUpdateFunction());
  }

  @Override
  public String getName() {
    return "Broyden";
  }
}
