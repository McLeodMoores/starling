/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.test;

import static com.opengamma.test.Assert.assertEqualsNoOrder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link Assert}.
 */
public class AssertTest {

  /**
   * Tests that two null collections are equal.
   */
  @Test
  public void testNullCollectionsNoOrder() {
    assertEqualsNoOrder((Set<Object>) null, (Set<Object>) null);
    assertEqualsNoOrder((Set<Object>) null, (Set<Object>) null, "message");
  }

  /**
   * Tests that two null maps are equal.
   */
  @Test
  public void testNullMapsNoOrder() {
    assertEqualsNoOrder((Map<Object, Object>) null, (Map<Object, Object>) null);
    assertEqualsNoOrder((Map<Object, Object>) null, (Map<Object, Object>) null, "message");
  }

  /**
   * Tests that two empty collections are equal.
   */
  @Test
  public void testEmptyCollectionsNoOrder() {
    assertEqualsNoOrder(Collections.emptySet(), Collections.emptySet());
    assertEqualsNoOrder(Collections.emptySet(), Collections.emptySet(), "message");
  }

  /**
   * Tests that two empty maps are equal.
   */
  @Test
  public void testEmptyMapsNoOrder() {
    assertEqualsNoOrder(Collections.emptyMap(), Collections.emptyMap());
    assertEqualsNoOrder(Collections.emptyMap(), Collections.emptyMap(), "message");
  }

  /**
   * Tests the behaviour for collections when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Collections .* expected: null.*")
  public void testCollectionsNoOrderOneNull1() {
    assertEqualsNoOrder(Collections.singleton("a"), null);
  }

  /**
   * Tests the behaviour for collections when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Collections .* actual: null.*")
  public void testCollectionsNoOrderOneNull2() {
    assertEqualsNoOrder(null, Collections.singleton("a"));
  }

  /**
   * Tests the behaviour for collections when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Collections .* expected: null.*")
  public void testCollectionsNoOrderOneNull3() {
    assertEqualsNoOrder(Collections.singleton("a"), null, null);
  }

  /**
   * Tests the behaviour for collections when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Collections .* actual: null.*")
  public void testCollectionsNoOrderOneNull4() {
    assertEqualsNoOrder(null, Collections.singleton("a"), null);
  }

  /**
   * Tests the behaviour for collections when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "message")
  public void testCollectionsNoOrderOneNull5() {
    assertEqualsNoOrder(Collections.singleton("a"), null, "message");
  }

  /**
   * Tests the behaviour for collections when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "message")
  public void testCollectionsNoOrderOneNull6() {
    assertEqualsNoOrder(null, Collections.singleton("a"), "message");
  }

  /**
   * Tests the behaviour for maps when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Maps .* expected: null.*")
  public void testMapsNoOrderOneNull1() {
    assertEqualsNoOrder(Collections.singletonMap("a", "b"), null);
  }

  /**
   * Tests the behaviour for maps when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Maps .* actual: null.*")
  public void testMapsNoOrderOneNull2() {
    assertEqualsNoOrder(null, Collections.singletonMap("a", "b"));
  }

  /**
   * Tests the behaviour for maps when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Maps .* expected: null.*")
  public void testMapsNoOrderOneNull3() {
    assertEqualsNoOrder(Collections.singletonMap("a", "b"), null, null);
  }

  /**
   * Tests the behaviour for maps when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Maps .* actual: null.*")
  public void testMapsNoOrderOneNull4() {
    assertEqualsNoOrder(null, Collections.singletonMap("a", "b"), null);
  }

  /**
   * Tests the behaviour for maps when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "message")
  public void testMapsNoOrderOneNull5() {
    assertEqualsNoOrder(Collections.singletonMap("a", "b"), null, "message");
  }

  /**
   * Tests the behaviour for maps when one is null.
   */
  @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "message")
  public void testMapsNoOrderOneNull6() {
    assertEqualsNoOrder(null, Collections.singletonMap("a", "b"), "message");
  }

  /**
   * Tests that collections that have the same elements are equal.
   */
  @Test
  public void testEqualCollections() {
    // sets in same order
    final Set<Integer> expected = new LinkedHashSet<>();
    expected.add(1);
    expected.add(2);
    expected.add(-1);
    Set<Integer> actual = new LinkedHashSet<>();
    actual.add(1);
    actual.add(2);
    actual.add(-1);
    assertEqualsNoOrder(expected, actual);
    // sets in different order
    actual = new LinkedHashSet<>();
    actual.add(-1);
    actual.add(2);
    actual.add(1);
    assertEqualsNoOrder(expected, actual);
  }

  /**
   * Tests that collections with different numbers of elements are not equal.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testDifferentSizeCollections1() {
    final Set<Integer> expected = new LinkedHashSet<>();
    expected.add(1);
    expected.add(2);
    expected.add(-1);
    final Set<Integer> actual = new LinkedHashSet<>();
    actual.add(1);
    actual.add(2);
    assertEqualsNoOrder(expected, actual);
  }

  /**
   * Tests that collections with different numbers of elements are not equal.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testDifferentSizeCollections2() {
    final Set<Integer> expected = new LinkedHashSet<>();
    expected.add(1);
    expected.add(2);
    final Set<Integer> actual = new LinkedHashSet<>();
    actual.add(1);
    actual.add(2);
    actual.add(-1);
    assertEqualsNoOrder(expected, actual);
  }

  /**
   * Tests that collections with different elements are not equal.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testDifferentCollections() {
    final Set<Integer> expected = new LinkedHashSet<>();
    expected.add(1);
    expected.add(2);
    expected.add(3);
    final Set<Integer> actual = new LinkedHashSet<>();
    actual.add(1);
    actual.add(2);
    actual.add(-1);
    assertEqualsNoOrder(expected, actual);
  }

  /**
   * Tests that maps that have the same entries are equal.
   */
  @Test
  public void testEqualMaps() {
    // maps in same order
    final Map<Integer, Integer> expected = new LinkedHashMap<>();
    expected.put(100, 1);
    expected.put(200, 2);
    expected.put(300, -1);
    Map<Integer, Integer> actual = new LinkedHashMap<>();
    actual.put(100, 1);
    actual.put(200, 2);
    actual.put(300, -1);
    assertEqualsNoOrder(expected, actual);
    // maps in different order
    actual = new LinkedHashMap<>();
    actual.put(300, -1);
    actual.put(200, 2);
    actual.put(100, 1);
    assertEqualsNoOrder(expected, actual);
  }

  /**
   * Tests that maps with different numbers of entries are not equal.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testDifferentSizeMaps1() {
    final Map<Integer, Integer> expected = new LinkedHashMap<>();
    expected.put(100, 1);
    expected.put(200, 2);
    expected.put(300, -1);
    final Map<Integer, Integer> actual = new LinkedHashMap<>();
    actual.put(100, 1);
    actual.put(200, 2);
    assertEqualsNoOrder(expected, actual);
  }

  /**
   * Tests that maps with different numbers of entries are not equal.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testDifferentSizeMaps2() {
    final Map<Integer, Integer> expected = new LinkedHashMap<>();
    expected.put(100, 1);
    expected.put(200, 2);
    final Map<Integer, Integer> actual = new LinkedHashMap<>();
    actual.put(100, 1);
    actual.put(200, 2);
    actual.put(300, -1);
    assertEqualsNoOrder(expected, actual);
  }

  /**
   * Tests that maps with different entries are not equal.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testDifferentMaps() {
    final Map<Integer, Integer> expected = new LinkedHashMap<>();
    expected.put(100, 1);
    expected.put(200, 2);
    expected.put(300, 3);
    final Map<Integer, Integer> actual = new LinkedHashMap<>();
    actual.put(100, 1);
    actual.put(200, 2);
    actual.put(300, -1);
    assertEqualsNoOrder(expected, actual);
  }
}
