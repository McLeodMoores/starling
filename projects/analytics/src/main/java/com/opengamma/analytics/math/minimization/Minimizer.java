/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Interface that finds the minimum value of a function. The function must be one-dimensional but the input type is not constrained
 * @param <F> The type of the function
 * @param <S> The type of the start position for the minimization
 */
public interface Minimizer<F extends Function1D<S, ?>, S> {

  /**
   * @param function The function to be minimized, not null
   * @param startPosition The start position
   * @return The minimum
   */
  S minimize(final F function, final S startPosition);

}
