/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.ComplexMathUtils;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;

/**
 * A calculator to determine the upper limit of the Fourier integral for a
 * characteristic function $\phi$.
 * <p>
 * The upper limit is found by determining the root of the function:
 * $$
 * \begin{align*}
 * f(x) = \ln\left(\left|\phi(x - i(1 + \alpha))\right|\right)
 * \end{align*}
 * $$
 * where $\alpha$ is the contour (which is parallel to the real axis and
 * shifted down by $1 + \alpha$) over which to integrate.
 *
 */
public class IntegralLimitCalculator {
  private static final Logger LOGGER = LoggerFactory.getLogger(IntegralLimitCalculator.class);
  private static final BracketRoot BRACKET_ROOT = new BracketRoot();
  private static final RealSingleRootFinder ROOT = new BrentSingleRootFinder(1e-1);

  /**
   *
   * @param psi The characteristic function, not null
   * @param alpha The value of $\alpha$, not 0 or -1
   * @param tol The tolerance for the root
   * @return The root
   */
  public double solve(final Function1D<ComplexNumber, ComplexNumber> psi, final double alpha, final double tol) {
    Validate.notNull(psi, "psi null");
    Validate.isTrue(alpha != 0.0 && alpha != -1.0, "alpha cannot be -1 or 0");
    Validate.isTrue(tol > 0.0, "need tol > 0");

    final double k = Math.log(tol) + Math.log(ComplexMathUtils.mod(psi.apply(new ComplexNumber(0.0, -(1 + alpha)))));
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {
      @Override
      public Double apply(final Double x) {
        final ComplexNumber z = new ComplexNumber(x, -(1 + alpha));
        return Math.log(ComplexMathUtils.mod(psi.apply(z))) - k;
      }
    };
    double[] range = null;
    try {
      range = BRACKET_ROOT.getBracketedPoints(f, 0.0, 200.0);
    } catch (final MathException e) {
      LOGGER.warn("Could not find integral limit. Using default of 500");
      return 500.0;
    }
    return ROOT.getRoot(f, range[0], range[1]);
  }

}
