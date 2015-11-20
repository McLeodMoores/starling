/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Class for defining the integration of 1-D functions.
 *  
 * @param <T> Type of the function output and result
 * @param <U> Type of the function inputs and integration bounds
 */
public abstract class Integrator1D<T, U> implements Integrator<T, U, Function1D<U, T>> {
  private static final Logger s_logger = LoggerFactory.getLogger(Integrator1D.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public T integrate(final Function1D<U, T> f, final U[] lower, final U[] upper) {
    Validate.notNull(f, "function was null");
    Validate.notNull(lower, "lower bound array was null");
    Validate.notNull(upper, "upper bound array was null");
    Validate.notEmpty(lower, "lower bound array was empty");
    Validate.notEmpty(upper, "upper bound array was empty");
    Validate.notNull(lower[0], "lower bound was null");
    Validate.notNull(upper[0], "upper bound was null");
    if (lower.length > 1) {
      s_logger.info("Lower bound array had more than one element; only using the first");
    }
    if (upper.length > 1) {
      s_logger.info("Upper bound array had more than one element; only using the first");
    }
    return integrate(f, lower[0], upper[0]);
  }

  /**
   * 1-D integration method
   * @param f The function to integrate, not null
   * @param lower The lower bound, not null
   * @param upper The upper bound, not null
   * @return The result of the integration
   */
  public abstract T integrate(Function1D<U, T> f, U lower, U upper);

}
