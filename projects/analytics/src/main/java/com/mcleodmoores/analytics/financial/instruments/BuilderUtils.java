/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.instruments;

import java.util.Collection;

/**
 *
 */
public class BuilderUtils {

  public static <T> T isSet(final T o, final String name) {
    if (o == null) {
      throw new IllegalStateException("The field " + name + " must be set");
    }
    return o;
  }

  public static <T> Collection<T> notEmpty(final Collection<T> c, final String name) {
    if (c.isEmpty()) {
      throw new IllegalStateException("The collection " + name + " cannot be empty");
    }
    return c;
  }

  public static <T> T[] notEmpty(final T[] a, final String name) {
    if (a.length == 0) {
      throw new IllegalStateException("The array " + name + " cannot be empty");
    }
    return a;
  }
}
