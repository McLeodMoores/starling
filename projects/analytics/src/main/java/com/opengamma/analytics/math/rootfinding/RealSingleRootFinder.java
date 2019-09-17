/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Parent class for root-finders that find a single real root $x$ for a function $f(x)$.
 */
public abstract class RealSingleRootFinder implements SingleRootFinder<Double, Double> {

  @Override
  public Double getRoot(final Function1D<Double, Double> function, final Double... startingPoints) {
    ArgumentChecker.notNull(startingPoints, "startingPoints");
    ArgumentChecker.isTrue(startingPoints.length == 2, "Number of starting points must be two: have {}", startingPoints.length);
    return getRoot(function, startingPoints[0], startingPoints[1]);
  }

  public abstract Double getRoot(Function1D<Double, Double> function, Double x1, Double x2);

  /**
   * Tests that the inputs to the root-finder are not null, and that a root is bracketed by the bounding values.
   * @param function The function, not null
   * @param x1 The first bound, not null
   * @param x2 The second bound, not null, must be greater than x1
   * @throws IllegalArgumentException if x1 and x2 do not bracket a root
   */
  protected void checkInputs(final Function1D<Double, Double> function, final Double x1, final Double x2) {
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(x1, "x1");
    ArgumentChecker.notNull(x2, "x2");
    ArgumentChecker.isTrue(x1 <= x2, "x1 {} must be less or equal to  x2 {}", x1, x2);
    ArgumentChecker.isTrue(function.apply(x1) * function.apply(x2) <= 0, "x1 {} and x2 {} do not bracket a root", x1, x2);
  }
}
