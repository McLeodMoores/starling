/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.util;

import com.google.common.base.Predicate;

/**
 *
 */
public class BuilderChecker {

  public static <T> T notNull(final T object, final String parameterName) {
    if (object == null) {
      throw new IllegalStateException("The " + parameterName + " must be set");
    }
    return object;
  }

  public static <T, U> T satisfies(final Predicate<T> test, final T object, final String errorMessage) {
    if (!test.apply(object)) {
      throw new IllegalStateException(errorMessage);
    }
    return object;
  }
}
