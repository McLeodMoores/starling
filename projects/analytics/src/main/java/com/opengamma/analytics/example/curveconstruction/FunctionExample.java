/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.curveconstruction;

// @export "imports"
import java.io.PrintStream;
import java.util.Arrays;

import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.CubicRealRootFinder;

/**
 * Example curve construction class.
 */
// @export "classDefinition"
public class FunctionExample {

  // @export "polyDerivativeDemo"
  public static RealPolynomialFunction1D getFunction() {
    final double[] coefficients = {-125, 75, -15, 1 };
    return new RealPolynomialFunction1D(coefficients);
  }

  public static void polyDerivativeDemo(final PrintStream out) {
    final RealPolynomialFunction1D f = getFunction();

    assert f.evaluate(5.0) == 0.0;

    final RealPolynomialFunction1D d = f.derivative();
    final double[] coefficients = d.getCoefficients();
    out.println(Arrays.toString(coefficients));
  }

  // @export "cubicRealRootFindingDemo"
  public static void cubicRealRootFindingDemo(final PrintStream out) {
    final RealPolynomialFunction1D f = getFunction();
    final CubicRealRootFinder cubic = new CubicRealRootFinder();
    final java.lang.Double[] roots = cubic.getRoots(f);
    out.println(Arrays.toString(roots));
  }

  // @export "brentSingleRootFinderDemo"
  public static void brentSingleRootFinderDemo(final PrintStream out) {
    final RealPolynomialFunction1D f = getFunction();
    final BrentSingleRootFinder brent = new BrentSingleRootFinder();
    final java.lang.Double root = brent.getRoot(f, -10.0, 10.0);
    out.println(root);
  }

  // @export "brentSingleRootFinderNotBracketingDemo"
  public static void brentSingleRootFinderNotBracketingDemo(final PrintStream out) {
    final RealPolynomialFunction1D f = getFunction();
    final BrentSingleRootFinder brent = new BrentSingleRootFinder();
    try {
      out.println("Trying to call getRoot with arguments that don't bracket the root...");
      brent.getRoot(f, -1.0, 1.0);
    } catch (final java.lang.IllegalArgumentException e) {
      out.println("IllegalArgumentException called");
    }
  }

}
