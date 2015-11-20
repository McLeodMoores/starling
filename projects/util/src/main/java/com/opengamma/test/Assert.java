/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

/**
 * Provides methods that supplement the tests found in {@link org.testng.Assert}.
 */
public final class Assert {

  /**
   * Restricted constructor.
   */
  private Assert() {
  }

  /**
   * Asserts that two collections are equal if:
   * <ul>
   *  <li> Both are null;</li>
   *  <li> Both are empty;</li>
   *  <li> They each contain the same number of elements, and each element in one collection is equal to an element in the other.</li>
   * </ul>
   * @param actual  the actual collection
   * @param expected  the expected collection
   */
  public static void assertEqualsNoOrder(final Collection<?> actual, final Collection<?> expected) {
    assertEqualsNoOrder(actual, expected, null);
  }

  /**
   * Asserts that two collections are equal if:
   * <ul>
   *  <li> Both are null;</li>
   *  <li> Both are empty;</li>
   *  <li> They each contain the same number of elements, and each element in one collection is equal to an element in the other.</li>
   * </ul>
   * @param actual  the actual collection
   * @param expected  the expected collection
   * @param message  the message if the test is false
   */
  public static void assertEqualsNoOrder(final Collection<?> actual, final Collection<?> expected, final String message) {
    if (actual == null) {
      if (expected == null) {
        return;
      }
      if (message != null) {
        throw new AssertionError(message);
      }
      throw new AssertionError("Collections not equal: expected: " + expected.toString() + " and actual: null");
    }
    if (expected == null) {
      if (message != null) {
        throw new AssertionError(message);
      }
      throw new AssertionError("Collections not equal: expected: null and actual: " + actual.toString());
    }
    assertEquals(actual.size(), expected.size(), message);
    assertTrue(actual.containsAll(expected), message);
  }

  /**
   * Asserts that two maps are equal if:
   * <ul>
   *  <li> Both are null;</li>
   *  <li> Both are empty;</li>
   *  <li> They each contain the same number of entries, and each key/value pair in one map is equal to a pair in the other.</li>
   * </ul>
   * @param actual  the actual collection
   * @param expected  the expected collection
   */
  public static void assertEqualsNoOrder(final Map<?, ?> actual, final Map<?, ?> expected) {
    assertEqualsNoOrder(actual, expected, null);
  }

  /**
   * Asserts that two maps are equal if:
   * <ul>
   *  <li> Both are null;</li>
   *  <li> Both are empty;</li>
   *  <li> They each contain the same number of entries, and each key/value pair in one map is equal to a pair in the other.</li>
   * </ul>
   * @param actual  the actual collection
   * @param expected  the expected collection
   * @param message  the message if the test is false
   */
  public static void assertEqualsNoOrder(final Map<?, ?> actual, final Map<?, ?> expected, final String message) {
    if (actual == null) {
      if (expected == null) {
        return;
      }
      if (message != null) {
        throw new AssertionError(message);
      }
      throw new AssertionError("Maps not equal: expected: " + expected.toString() + " and actual: null");
    }
    if (expected == null) {
      if (message != null) {
        throw new AssertionError(message);
      }
      throw new AssertionError("Maps not equal: expected: null and actual: " + actual.toString());
    }
    assertEquals(actual.size(), expected.size(), message);
    assertTrue(actual.entrySet().containsAll(expected.entrySet()), message);
  }
}
