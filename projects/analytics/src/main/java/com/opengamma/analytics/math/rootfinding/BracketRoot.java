/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Class that brackets single root of a function. For a 1-D function ({@link com.opengamma.analytics.math.function.Function1D}) $f(x)$, initial values for the
 * interval, $x_1$ and $x_2$, are supplied.
 * <p>
 * A root is assumed to be bracketed if $f(x_1)f(x_2) &lt; 0$. If this condition is not satisfied, then either $|f(x_1)| &lt; |f(x_2)|$, in which case the lower
 * value $x_1$ is shifted in the negative $x$ direction, or the upper value $x_2$ is shifted in the positive $x$ direction. The amount by which to shift is the
 * difference between the two $x$ values multiplied by a constant ratio (1.6). If a root is not bracketed after 50 attempts, an exception is thrown.
 */
public class BracketRoot {
  private static final double RATIO = 1.6;
  private static final int MAX_STEPS = 50;

  /**
   * @param f The function, not null
   * @param xLower Initial value of lower bracket
   * @param xUpper Initial value of upper bracket
   * @return The bracketed points as an array, where the first element is the lower bracket and the second the upper bracket.
   * @throws MathException If a root is not bracketed in 50 attempts.
   */
  public double[] getBracketedPoints(final Function1D<Double, Double> f, final double xLower, final double xUpper) {
    Validate.notNull(f, "f");
    double x1 = xLower;
    double x2 = xUpper;
    double f1 = 0;
    double f2 = 0;
    f1 = f.evaluate(x1);
    f2 = f.evaluate(x2);
    if (Double.isNaN(f1)) {
      throw new MathException("Failed to bracket root: function invalid at x = " + x1 + " f(x) = " + f1);
    }
    if (Double.isNaN(f2)) {
      throw new MathException("Failed to bracket root: function invalid at x = " + x2 + " f(x) = " + f2);
    }

    for (int count = 0; count < MAX_STEPS; count++) {
      if (f1 * f2 < 0) {
        return new double[] {x1, x2 };
      }
      if (Math.abs(f1) < Math.abs(f2)) {
        x1 += RATIO * (x1 - x2);
        f1 = f.evaluate(x1);
        if (Double.isNaN(f1)) {
          throw new MathException("Failed to bracket root: function invalid at x = " + x1 + " f(x) = " + f1);
        }
      } else {
        x2 += RATIO * (x2 - x1);
        f2 = f.evaluate(x2);
        if (Double.isNaN(f2)) {
          throw new MathException("Failed to bracket root: function invalid at x = " + x2 + " f(x) = " + f2);
        }
      }
    }
    throw new MathException("Failed to bracket root");
  }

  public double[] getBracketedPoints(final Function1D<Double, Double> f, final double xLower, final double xUpper, final double minX, final double maxX) {
    Validate.notNull(f, "f");
    Validate.isTrue(xLower >= minX, "xLower < minX");
    Validate.isTrue(xUpper <= maxX, "xUpper < maxX");
    double x1 = xLower;
    double x2 = xUpper;
    double f1 = 0;
    double f2 = 0;
    boolean lowerLimitReached = false;
    boolean upperLimitReached = false;
    f1 = f.evaluate(x1);
    f2 = f.evaluate(x2);
    if (Double.isNaN(f1)) {
      throw new MathException("Failed to bracket root: function invalid at x = " + x1 + " f(x) = " + f1);
    }
    if (Double.isNaN(f2)) {
      throw new MathException("Failed to bracket root: function invalid at x = " + x2 + " f(x) = " + f2);
    }
    for (int count = 0; count < MAX_STEPS; count++) {
      if (f1 * f2 <= 0) {
        return new double[] {x1, x2 };
      }
      if (lowerLimitReached && upperLimitReached) {
        throw new MathException("Failed to bracket root: no root found between minX and maxX");
      }
      if (Math.abs(f1) < Math.abs(f2) && !lowerLimitReached) {
        x1 += RATIO * (x1 - x2);
        if (x1 < minX) {
          x1 = minX;
          lowerLimitReached = true;
        }
        f1 = f.evaluate(x1);
        if (Double.isNaN(f1)) {
          throw new MathException("Failed to bracket root: function invalid at x = " + x1 + " f(x) = " + f1);
        }
      } else {
        x2 += RATIO * (x2 - x1);
        if (x2 > maxX) {
          x2 = maxX;
          upperLimitReached = true;
        }
        f2 = f.evaluate(x2);
        if (Double.isNaN(f2)) {
          throw new MathException("Failed to bracket root: function invalid at x = " + x2 + " f(x) = " + f2);
        }
      }
    }
    f1 = f.evaluate(xLower);
    f2 = f.evaluate(xUpper);
    throw new MathException("Failed to bracket root: max iterations");
  }

}
