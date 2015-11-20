/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Finds a single root of a function using the bisection method.
 * <p>
 * If a root of a function $f(x)$ is bounded by two values $x_1$ and $x_2$,
 * then $f(x_1)f(x_2) < 0$.  The function is evaluated at the midpoint of these
 * values and the bound that gives the same sign in the function evaluation is
 * replaced. The bisection is stopped when the change in the value of $x$ is
 * below the accuracy, or the evaluation of the function at $x$ is zero.
 */
public class BisectionSingleRootFinder extends RealSingleRootFinder {
  private final double _accuracy;
  private static final int MAX_ITER = 100;
  private static final double ZERO = 1e-16;

  /**
   * Sets the accuracy to 10<sup>-15</sup>
   */
  public BisectionSingleRootFinder() {
    this(1e-15);
  }

  /**
   * @param accuracy The required accuracy of the $x$-position of the root
   */
  public BisectionSingleRootFinder(final double accuracy) {
    _accuracy = Math.abs(accuracy);
  }

  /**
   * {@inheritDoc}
   * @throws MathException If the root is not found to the required accuracy in 100 attempts
   */
  @Override
  public Double getRoot(final Function1D<Double, Double> function, final Double x1, final Double x2) {
    checkInputs(function, x1, x2);
    final double y1 = function.evaluate(x1);
    double y = function.evaluate(x2);
    if (Math.abs(y) < _accuracy) {
      return x2;
    }
    if (Math.abs(y1) < _accuracy) {
      return x1;
    }
    double dx, xRoot, xMid;
    if (y1 < 0) {
      dx = x2 - x1;
      xRoot = x1;
    } else {
      dx = x1 - x2;
      xRoot = x2;
    }
    for (int i = 0; i < MAX_ITER; i++) {
      dx *= 0.5;
      xMid = xRoot + dx;
      y = function.evaluate(xMid);
      if (y <= 0) {
        xRoot = xMid;
      }
      if (Math.abs(dx) < _accuracy || Math.abs(y) < ZERO) {
        return xRoot;
      }
    }
    throw new MathException("Could not find root in " + MAX_ITER + " attempts");
  }
}
