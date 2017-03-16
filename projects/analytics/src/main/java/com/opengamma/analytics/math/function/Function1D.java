/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import com.opengamma.util.ArgumentChecker;

/**
 * 1-D function implementation.
 * @param <ARG_TYPE> Type of the arguments
 * @param <RESULT_TYPE> Return type of the function
 */
public abstract class Function1D<ARG_TYPE, RESULT_TYPE> implements Function<ARG_TYPE, RESULT_TYPE> {

  /**
   * Implementation of the interface. This method only uses the first argument.
   * @param x The list of inputs into the function, not null and no null elements
   * @return The value of the function
   */
  @SuppressWarnings("unchecked")
  @Override
  public RESULT_TYPE evaluate(final ARG_TYPE... x) {
    ArgumentChecker.noNulls(x, "parameter list");
    ArgumentChecker.isTrue(x.length == 1, "parameter list must have one element");
    return evaluate(x[0]);
  }

  /**
   * 1-D function method.
   * @param x  the argument of the function, not null
   * @return  the value of the function
   */
  public abstract RESULT_TYPE evaluate(ARG_TYPE x);

}
