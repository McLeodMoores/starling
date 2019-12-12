/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.math.function;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class Function1dAdapter<ARG_TYPE, RESULT_TYPE> extends Function1D<ARG_TYPE, RESULT_TYPE> {

  public static <ARG_TYPE, RESULT_TYPE> Function1D<ARG_TYPE, RESULT_TYPE> of(
      final java.util.function.Function<ARG_TYPE, RESULT_TYPE> function) {
    return new Function1dAdapter<>(function);
  }

  private final java.util.function.Function<ARG_TYPE, RESULT_TYPE> _function;

  private Function1dAdapter(final java.util.function.Function<ARG_TYPE, RESULT_TYPE> function) {
    _function = ArgumentChecker.notNull(function, "function");
  }

  @Override
  public RESULT_TYPE evaluate(final ARG_TYPE x) {
    return _function.apply(x);
  }

}
