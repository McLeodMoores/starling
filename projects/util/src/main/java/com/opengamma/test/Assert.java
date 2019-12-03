/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.test;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
  public static void assertEqualsNoOrder(final Iterable<?> actual, final Iterable<?> expected) {
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
  public static void assertEqualsNoOrder(final Iterable<?> actual, final Iterable<?> expected, final String message) {
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
    Iterator<?> actualI = actual.iterator();
    Iterator<?> expectedI = expected.iterator();
    List<Object> actualL = new ArrayList<>();
    List<Object> expectedL = new ArrayList<>();
    while (actualI.hasNext()) {
      actualL.add(actualI.next());
    }
    while (expectedI.hasNext()) {
      expectedL.add(expectedI.next());
    }
    assertEquals(actualL.size(), expectedL.size(), message);
    if (!actualL.containsAll(expectedL)) {
      throw new AssertionError("Collections not equal: expected: " + expected.toString() + " and actual: " + actual.toString());
    }
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
    for (final Map.Entry<?, ?> entries : actual.entrySet()) {
      if (entries.getValue() instanceof Collection) {
        final Collection<?> value = (Collection<?>) expected.get(entries.getKey());
        if (value == null) {
          throw new AssertionError("No value found in expected map for key " + entries.getKey());
        }
        if (entries.getValue() instanceof Collection) {
          assertEqualsNoOrder((Collection<?>) entries.getValue(), value, message);
        }
      } else {
        assertEquals(entries.getValue(), expected.get(entries.getKey()));
      }
    }
  }

}
