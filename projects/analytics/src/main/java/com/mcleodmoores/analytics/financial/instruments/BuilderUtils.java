/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.instruments;

import java.util.Collection;

/**
 * Utility methods for the builder pattern.
 */
public final class BuilderUtils {

  /**
   * Tests that a field that cannot be null is set.
   *
   * @param o  the value of the field
   * @param name  the name of the field
   * @return  the value
   */
  public static <T> T isSet(final T o, final String name) {
    if (o == null) {
      throw new IllegalStateException("The field " + name + " must be set");
    }
    return o;
  }

  /**
   * Tests that a collection is not empty.
   *
   * @param c  the collection, not null
   * @param name  the name of the collection field
   * @return  the collection
   */
  public static <T> Collection<T> notEmpty(final Collection<T> c, final String name) {
    isSet(c, name);
    if (c.isEmpty()) {
      throw new IllegalStateException("The collection " + name + " cannot be empty");
    }
    return c;
  }

  /**
   * Tests that an array is not empty.
   *
   * @param a  the array, not null
   * @param name  the name of the array field
   * @return  the array
   */
  public static <T> T[] notEmpty(final T[] a, final String name) {
    isSet(a, name);
    if (a.length == 0) {
      throw new IllegalStateException("The array " + name + " cannot be empty");
    }
    return a;
  }

  private BuilderUtils() {
  }
}
