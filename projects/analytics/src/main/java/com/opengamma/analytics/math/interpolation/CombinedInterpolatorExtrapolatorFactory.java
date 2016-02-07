/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.util.ArgumentChecker;

/**
 * A factory that combines one-dimensional interpolators and extrapolators. Note that the combined interpolators
 * are not cached in this factory.
 * @deprecated  use {@link com.opengamma.analytics.math.interpolation.factory.CombinedInterpolatorExtrapolator1dAdapter}.
 */
@Deprecated
public final class CombinedInterpolatorExtrapolatorFactory {

  /**
   * Restricted constructor.
   */
  private CombinedInterpolatorExtrapolatorFactory() {
  }

  /**
   * Creates a combined interpolator that does not extrapolate on the left or right.
   * @param interpolatorName  the interpolator name, not null
   * @return  the interpolator
   */
  public static CombinedInterpolatorExtrapolator getInterpolator(final String interpolatorName) {
    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    return new CombinedInterpolatorExtrapolator(interpolator);
  }

  /**
   * Creates a combined interpolator that uses the same extrapolator on the left and right if the extrapolator
   * name is not null, or that only interpolated if not.
   * @param interpolatorName  the interpolator name, not null
   * @param extrapolatorName  the extrapolator name, can be null
   * @return  the interpolator
   */
  public static CombinedInterpolatorExtrapolator getInterpolator(final String interpolatorName, final String extrapolatorName) {
    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    if (extrapolatorName == null || extrapolatorName.isEmpty()) {
      return new CombinedInterpolatorExtrapolator(interpolator);
    }
    final Interpolator1D extrapolator = getExtrapolator(extrapolatorName, interpolator);
    return new CombinedInterpolatorExtrapolator(interpolator, extrapolator, extrapolator);
  }

  /**
   * Creates a combined interpolator and extrapolator. If the left and / or right extrapolator name is not provided, extrapolation
   * will not be performed on the left and / or right.
   * @param interpolatorName  the interpolator name, not null
   * @param leftExtrapolatorName  the extrapolator name, can be null
   * @param rightExtrapolatorName  the extrapolator name, can be null
   * @return  the interpolator
   */
  public static CombinedInterpolatorExtrapolator getInterpolator(final String interpolatorName, final String leftExtrapolatorName,
      final String rightExtrapolatorName) {
    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    if (leftExtrapolatorName == null || leftExtrapolatorName.isEmpty()) {
      if (rightExtrapolatorName == null || rightExtrapolatorName.isEmpty()) {
        return new CombinedInterpolatorExtrapolator(interpolator);
      }
      final Interpolator1D extrapolator = getExtrapolator(rightExtrapolatorName, interpolator);
      return new CombinedInterpolatorExtrapolator(interpolator, extrapolator);
    }
    if (rightExtrapolatorName == null || rightExtrapolatorName.isEmpty()) {
      final Interpolator1D extrapolator = getExtrapolator(leftExtrapolatorName, interpolator);
      return new CombinedInterpolatorExtrapolator(interpolator, extrapolator);
    }
    final Interpolator1D leftExtrapolator = getExtrapolator(leftExtrapolatorName, interpolator);
    final Interpolator1D rightExtrapolator = getExtrapolator(rightExtrapolatorName, interpolator);
    return new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);
  }

  /**
   * Gets an extrapolator. Some extrapolators (e.g. linear) depend on the interpolator, so this information must
   * be passed into this method.
   * @param extrapolatorName  the extrapolator name, not null
   * @param interpolator  the interpolator, can be null
   * @return  the extrapolator
   */
  public static Interpolator1D getExtrapolator(final String extrapolatorName, final Interpolator1D interpolator) {
    ArgumentChecker.notNull(extrapolatorName, "extrapolatorName");
    if (extrapolatorName.equals(Interpolator1DFactory.LINEAR_EXTRAPOLATOR)) {
      return new LinearExtrapolator1D(interpolator);
    }
    if (extrapolatorName.equals(Interpolator1DFactory.LOG_LINEAR_EXTRAPOLATOR)) {
      return new LogLinearExtrapolator1D(interpolator);
    }
    if (extrapolatorName.equals(Interpolator1DFactory.QUADRATIC_LEFT_EXTRAPOLATOR)) {
      return new QuadraticPolynomialLeftExtrapolator(interpolator);
    }
    return Interpolator1DFactory.getInterpolator(extrapolatorName);
  }
}
