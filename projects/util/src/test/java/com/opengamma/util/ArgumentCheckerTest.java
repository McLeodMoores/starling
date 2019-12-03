/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Test {@link ArgumentChecker}.
 */
@Test(groups = TestGroup.UNIT)
public class ArgumentCheckerTest {

  //-------------------------------------------------------------------------
  /**
   * Tests that a value is true.
   */
  @Test
  public void testIsTrueOk() {
     ArgumentChecker.isTrue(true, "Message");
  }

  /**
   * Tests the exception when a value is false.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Message")
  public void testIsTrueFalse() {
    ArgumentChecker.isTrue(false, "Message");
  }

  /**
   * Tests that a value is true.
   */
  @Test
  public void testIsTrueOkArgs() {
    ArgumentChecker.isTrue(true, "Message {} {} {}", "A", 2, 3.);
  }

  /**
   * Tests the exception and message when a value is false.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Message A 2 3.0")
  public void testIsTrueFalseArgs() {
    ArgumentChecker.isTrue(false, "Message {} {} {}", "A", 2, 3.);
  }

  /**
   * Tests that a value is false.
   */
  public void testIsFalseOk() {
    ArgumentChecker.isFalse(false, "Message");
  }

  /**
   * Tests the exception when the value is true.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Message")
  public void testIsFalseTrue() {
    ArgumentChecker.isFalse(true, "Message");
  }

  /**
   * Tests that a value is false.
   */
  @Test
  public void testIsFalseOkArgs() {
    ArgumentChecker.isFalse(false, "Message {} {} {}", "A", 2., 3, true);
  }

  /**
   * Tests the exception and message when a value is true.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Message A 2.0 3 true")
  public void testIsFalseTrueArgs() {
    ArgumentChecker.isFalse(true, "Message {} {} {} {}", "A", 2., 3, true);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a value is not null.
   */
  @Test
  public void testNotNullOk() {
    assertEquals(ArgumentChecker.notNull("Kirk", "name"), "Kirk");
    assertEquals(ArgumentChecker.notNull(Integer.valueOf(1), "name"), Integer.valueOf(1));
  }

  /**
   * Tests the exception and message when a value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotNullNull() {
    ArgumentChecker.notNull(null, "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a value is not null.
   */
  @Test
  public void testNotNullInjectedOk() {
    assertEquals(ArgumentChecker.notNullInjected("Kirk", "name"), "Kirk");
  }

  /**
   * Tests the exception and message when a value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Injected input parameter 'name' must not be null")
  public void testNotNullInjectedNull() {
    ArgumentChecker.notNullInjected(null, "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a string is not empty.
   */
  @Test
  public void testNotBlankStringOk() {
    assertEquals("Kirk", ArgumentChecker.notBlank("Kirk", "name"));
  }

  /**
   * Tests that the returned string is trimmed of whitespace.
   */
  @Test
  public void testNotBlankStringOkTrimmed() {
    assertEquals("Kirk", ArgumentChecker.notBlank(" Kirk ", "name"));
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotBlankStringNull() {
    ArgumentChecker.notBlank((String) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be empty")
  public void testNotBlankStringEmpty() {
    ArgumentChecker.notBlank("", "name");
  }

  /**
   * Tests the exception and message when the value contains only whitespace.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be empty")
  public void testNotBlankStringSpaces() {
    ArgumentChecker.notBlank("  ", "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a string is not empty.
   */
  @Test
  public void testNotEmptyStringOk() {
    assertEquals(ArgumentChecker.notEmpty("Kirk", "name"), "Kirk");
    assertEquals(ArgumentChecker.notEmpty(" ", "name"), " ");
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotEmptyStringNull() {
    ArgumentChecker.notEmpty((String) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be empty")
  public void testNotEmptyStringEmpty() {
    ArgumentChecker.notEmpty("", "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an array is not empty.
   */
  @Test
  public void testNotEmptyArrayOk() {
    final Object[] array = new Object[] {"Element"};
    final Object[] result = ArgumentChecker.notEmpty(array, "name");
    assertEquals(result, array);
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotEmptyArrayNull() {
    ArgumentChecker.notEmpty((Object[]) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter array 'name' must not be empty")
  public void testNotEmptyArrayEmpty() {
    ArgumentChecker.notEmpty(new Object[0], "name");
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotEmpty2dArrayNull() {
    ArgumentChecker.notEmpty((Object[][]) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter array 'name' must not be empty")
  public void testNotEmpty2dArrayEmpty() {
    ArgumentChecker.notEmpty(new Object[0][], "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an array is not empty.
   */
  @Test
  public void testNotEmptyIntArrayOk() {
    final int[] array = new int[] {6};
    final int[] result = ArgumentChecker.notEmpty(array, "name");
    assertEquals(result, array);
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotEmptyIntArrayNull() {
    ArgumentChecker.notEmpty((int[]) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter array 'name' must not be empty")
  public void testNotEmptyIntArrayEmpty() {
    ArgumentChecker.notEmpty(new int[0], "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an array is not empty.
   */
  @Test
  public void testNotEmptyLongArrayOk() {
    final long[] array = new long[] {6L};
    final long[] result = ArgumentChecker.notEmpty(array, "name");
    assertEquals(result, array);
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotEmptyLongArrayNull() {
    ArgumentChecker.notEmpty((long[]) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter array 'name' must not be empty")
  public void testNotEmptyLongArrayEmpty() {
    ArgumentChecker.notEmpty(new long[0], "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an array is not empty.
   */
  @Test
  public void testNotEmptyDoubleArrayOk() {
    final double[] array = new double[] {6.0d};
    final double[] result = ArgumentChecker.notEmpty(array, "name");
    assertEquals(result, array);
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotEmptyDoubleArrayNull() {
    ArgumentChecker.notEmpty((double[]) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter array 'name' must not be empty")
  public void testNotEmptyDoubleArrayEmpty() {
    ArgumentChecker.notEmpty(new double[0], "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an iterable is not empty.
   */
  @Test
  public void testNotEmptyIterableOk() {
    final Iterable<String> coll = Arrays.asList("Element");
    final Iterable<String> result = ArgumentChecker.notEmpty(coll, "name");
    assertEquals(result, coll);
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotEmptyIterableNull() {
    ArgumentChecker.notEmpty((Iterable<?>) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter iterable 'name' must not be empty")
  public void testNotEmptyIterableEmpty() {
    ArgumentChecker.notEmpty((Iterable<?>) Collections.emptyList(), "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a collection is not empty.
   */
  @Test
  public void testNotEmptyCollectionOk() {
    final List<String> coll = Arrays.asList("Element");
    final List<String> result = ArgumentChecker.notEmpty(coll, "name");
    assertEquals(result, coll);
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotEmptyCollectionNull() {
    ArgumentChecker.notEmpty((Collection<?>) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter collection 'name' must not be empty")
  public void testNotEmptyCollectionEmpty() {
    ArgumentChecker.notEmpty(Collections.emptySet(), "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a map is not empty.
   */
  @Test
  public void testNotEmptyMapOk() {
    final SortedMap<String, String> map = ImmutableSortedMap.of("Element", "Element");
    final SortedMap<String, String> result = ArgumentChecker.notEmpty(map, "name");
    assertEquals(result, map);
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNotEmptyMapNull() {
    ArgumentChecker.notEmpty((Map<?, ?>) null, "name");
  }

  /**
   * Tests the exception and message when the value is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter map 'name' must not be empty")
  public void testNotEmptyMapEmpty() {
    ArgumentChecker.notEmpty(Collections.emptyMap(), "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an array contains no nulls.
   */
  @Test
  public void testNoNullsArrayOk() {
    final String[] array = new String[] {"Element"};
    final String[] result = ArgumentChecker.noNulls(array, "name");
    assertEquals(result, array);
  }

  /**
   * Tests that an empty array is allowed.
   */
  @Test
  public void testNoNullsArrayOkEmpty() {
    final Object[] array = new Object[] {};
    assertEquals(ArgumentChecker.noNulls(array, "name"), array);
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNoNullsArrayNull() {
    ArgumentChecker.noNulls((Object[]) null, "name");
  }

  /**
   * Tests the exception and message when a value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter array 'name' must not contain null at index 1")
  public void testNoNullsArrayNullElement() {
    ArgumentChecker.noNulls(new Object[] { 1, null }, "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a collection contains no nulls.
   */
  @Test
  public void testNoNullsIterableOk() {
    final List<String> coll = Arrays.asList("Element");
    final List<String> result = ArgumentChecker.noNulls(coll, "name");
    assertEquals(result, coll);
  }

  /**
   * Tests that an empty collection is allowed.
   */
  @Test
  public void testNoNullsIterableOkEmpty() {
    final Iterable<?> coll = Arrays.asList();
    ArgumentChecker.noNulls(coll, "name");
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNoNullsIterableNull() {
    ArgumentChecker.noNulls((Iterable<?>) null, "name");
  }

  /**
   * Tests the exception and message when a value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter iterable 'name' must not contain null")
  public void testNoNullsIterableNullElement() {
    ArgumentChecker.noNulls(Arrays.asList((Object) null), "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a map contains no nulls.
   */
  @Test
  public void testNoNullsMapOk() {
    final ImmutableSortedMap<String, String> map = ImmutableSortedMap.of("A", "B");
    final ImmutableSortedMap<String, String> result = ArgumentChecker.noNulls(map, "name");
    assertEquals(result, map);
  }

  /**
   * Tests that an empty map is allowed.
   */
  @Test
  public void testNoNullsMapOkEmpty() {
    final Map<Object, Object> map = new HashMap<>();
    ArgumentChecker.noNulls(map, "name");
  }

  /**
   * Tests the exception and message when the value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be null")
  public void testNoNullsMapNull() {
    ArgumentChecker.noNulls((Map<?, ?>) null, "name");
  }

  /**
   * Tests the exception and message when a key is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter map 'name' must not contain a null key")
  public void testNoNullsMapNullKey() {
    final Map<Object, Object> map = new HashMap<>();
    map.put("A", "B");
    map.put(null, "Z");
    ArgumentChecker.noNulls(map, "name");
  }

  /**
   * Tests the exception and message when a value is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter map 'name' must not contain a null value")
  public void testNoNullsMapNullValue() {
    final Map<Object, Object> map = new HashMap<>();
    map.put("A", "B");
    map.put("Z", null);
    ArgumentChecker.noNulls(map, "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an integer is not negative.
   */
  @Test
  public void testNotNegativeIntOk() {
    assertEquals(ArgumentChecker.notNegative(0, "name"), 0);
    assertEquals(ArgumentChecker.notNegative(1, "name"), 1);
  }

  /**
   * Tests the exception and message when an integer is negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be negative")
  public void testNotNegativeIntNegative() {
    ArgumentChecker.notNegative(-1, "name");
  }

  /**
   * Tests that a long is not negative.
   */
  @Test
  public void testNotNegativeLongOk() {
    assertEquals(ArgumentChecker.notNegative(0L, "name"), 0L);
    assertEquals(ArgumentChecker.notNegative(1L, "name"), 1L);
  }

  /**
   * Tests the exception and message when a long is negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be negative")
  public void testNotNegativeLongNegative() {
    ArgumentChecker.notNegative(-1L, "name");
  }

  /**
   * Tests that a double is not negative.
   */
  @Test
  public void testNotNegativeDoubleOk() {
    assertEquals(ArgumentChecker.notNegative(0d, "name"), 0d, 0.0001d);
    assertEquals(ArgumentChecker.notNegative(0.1d, "name"), 0.1d, 0.0001d);
  }

  /**
   * Tests the exception and message when a double is negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be negative")
  public void testNotNegativeDoubleNegative() {
    ArgumentChecker.notNegative(-1.0d, "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an integer is greater than zero.
   */
  @Test
  public void testNotNegativeOrZeroIntOk() {
    assertEquals(ArgumentChecker.notNegativeOrZero(1, "name"), 1);
  }

  /**
   * Tests the exception and message when an integer is 0.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be negative or zero")
  public void testNotNegativeOrZeroIntZero() {
    ArgumentChecker.notNegativeOrZero(0, "name");
  }

  /**
   * Tests the exception and message when an integer is negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be negative or zero")
  public void testNotNegativeOrZeroIntNegative() {
    ArgumentChecker.notNegativeOrZero(-1, "name");
  }

  /**
   * Tests that a long is greater than zero.
   */
  @Test
  public void testNotNegativeOrZeroLongOk() {
    assertEquals(ArgumentChecker.notNegativeOrZero(1L, "name"), 1);
  }

  /**
   * Tests the exception and message when a long is 0.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be negative or zero")
  public void testNotNegativeOrZeroLongZero() {
    ArgumentChecker.notNegativeOrZero(0L, "name");
  }

  /**
   * Tests the exception and message when a long is negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be negative or zero")
  public void testNotNegativeOrZeroLongNegative() {
    ArgumentChecker.notNegativeOrZero(-1L, "name");
  }

  /**
   * Tests that a double is greater than zero.
   */
  @Test
  public void testNotNegativeOrZeroDoubleOk() {
    assertEquals(ArgumentChecker.notNegativeOrZero(1d, "name"), 1d, 0.0001d);
  }

  /**
   * Tests the exception and message when a double is 0.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be negative or zero")
  public void testNotNegativeOrZeroDoubleZero() {
    ArgumentChecker.notNegativeOrZero(0.0d, "name");
  }

  /**
   * Tests the exception and message when a double is negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be negative or zero")
  public void testNotNegativeOrZeroDoubleNegative() {
    ArgumentChecker.notNegativeOrZero(-1.0d, "name");
  }

  /**
   * Tests that a double is greater than zero.
   */
  @Test
  public void testNotNegativeOrZeroDoubleEpsOk() {
    assertEquals(ArgumentChecker.notNegativeOrZero(1d, 0.0001d, "name"), 1d, 0.0001d);
    assertEquals(ArgumentChecker.notNegativeOrZero(0.1d, 0.0001d, "name"), 0.1d, 0.0001d);
  }

  /**
   * Tests the exception and message when a double is 0.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be zero")
  public void testNotNegativeOrZeroDoubleEpsZero() {
    ArgumentChecker.notNegativeOrZero(0.0000001d, 0.0001d, "name");
  }

  /**
   * Tests the exception and message when a double is negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must be greater than zero")
  public void testNotNegativeOrZeroDoubleEpsNegative() {
    ArgumentChecker.notNegativeOrZero(-1.0d, 0.0001d, "name");
  }

  /**
   * Tests that a double is greater than zero.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testNotNegativeOrZeroDoubleEpsOkDep() {
    assertEquals(ArgumentChecker.notNegativeOrZero(1d, 0.0001d, "name", "a", "b"), 1d, 0.0001d);
    assertEquals(ArgumentChecker.notNegativeOrZero(0.1d, 0.0001d, "name", "a", "b"), 0.1d, 0.0001d);
  }

  /**
   * Tests the exception and message when a double is 0.
   */
  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "name")
  public void testNotNegativeOrZeroDoubleEpsZeroDep() {
    ArgumentChecker.notNegativeOrZero(0.0000001d, 0.0001d, "name", "a", "b");
  }

  /**
   * Tests the exception and message when a double is negative.
   */
  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "name")
  public void testNotNegativeOrZeroDoubleEpsNegativeDep() {
    ArgumentChecker.notNegativeOrZero(-1.0d, 0.0001d, "name", "a", "b");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a double is not zero.
   */
  @Test
  public void testNotZeroDoubleOk() {
    assertEquals(ArgumentChecker.notZero(1d, 0.1d, "name"), 1d, 0.0001d);
  }

  /**
   * Tests the exception and message when a double is 0.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be zero")
  public void testNotZeroDoubleZero() {
    ArgumentChecker.notZero(0d, 0.1d, "name");
  }

  /**
   * Tests the exception and message when a double is 0.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'name' must not be zero")
  public void testNotZeroDoubleNegative() {
    ArgumentChecker.notZero(-0d, 0.1d, "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a collection has a null element.
   */
  @Test
  public void testHasNullElement() {
    Collection<?> c = Sets.newHashSet(null, new Object(), new Object());
    assertTrue(ArgumentChecker.hasNullElement(c));
    c = Sets.newHashSet(new Object(), new Object());
    assertFalse(ArgumentChecker.hasNullElement(c));
  }

  /**
   * Tests that a collection has a negative element.
   */
  @Test
  public void testHasNegativeElement() {
    Collection<Double> c = Sets.newHashSet(4., -5., -6.);
    assertTrue(ArgumentChecker.hasNegativeElement(c));
    c = Sets.newHashSet(1., 2., 3.);
    assertFalse(ArgumentChecker.hasNegativeElement(c));
  }

  /**
   * Tests that a value is within a range.
   */
  @Test
  public void testIsInRange() {
    final double low = 0;
    final double high = 1;
    assertTrue(ArgumentChecker.isInRangeExclusive(low, high, 0.5));
    assertFalse(ArgumentChecker.isInRangeExclusive(low, high, -high));
    assertFalse(ArgumentChecker.isInRangeExclusive(low, high, 2 * high));
    assertFalse(ArgumentChecker.isInRangeExclusive(low, high, low));
    assertFalse(ArgumentChecker.isInRangeExclusive(low, high, high));
    assertTrue(ArgumentChecker.isInRangeInclusive(low, high, 0.5));
    assertFalse(ArgumentChecker.isInRangeInclusive(low, high, -high));
    assertFalse(ArgumentChecker.isInRangeInclusive(low, high, 2 * high));
    assertTrue(ArgumentChecker.isInRangeInclusive(low, high, low));
    assertTrue(ArgumentChecker.isInRangeInclusive(low, high, high));
    assertTrue(ArgumentChecker.isInRangeExcludingLow(low, high, 0.5));
    assertFalse(ArgumentChecker.isInRangeExcludingLow(low, high, -high));
    assertFalse(ArgumentChecker.isInRangeExcludingLow(low, high, 2 * high));
    assertFalse(ArgumentChecker.isInRangeExcludingLow(low, high, low));
    assertTrue(ArgumentChecker.isInRangeExcludingLow(low, high, high));
    assertTrue(ArgumentChecker.isInRangeExcludingHigh(low, high, 0.5));
    assertFalse(ArgumentChecker.isInRangeExcludingHigh(low, high, -high));
    assertFalse(ArgumentChecker.isInRangeExcludingHigh(low, high, 2 * high));
    assertTrue(ArgumentChecker.isInRangeExcludingHigh(low, high, low));
    assertFalse(ArgumentChecker.isInRangeExcludingHigh(low, high, high));
  }

  /**
   * Tests the exception and message when an array is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter array 'name' must not be empty")
  public void testNotEmptyDoubleArray() {
    ArgumentChecker.notEmpty(new double[0], "name");
  }

  /**
   * Tests the exception and message when an array is empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter array 'name' must not be empty")
  public void testNotEmptyLongArray() {
    ArgumentChecker.notEmpty(new long[0], "name");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that dates are in order or equal i.e. not decreasing in time.
   */
  @Test
  public void testInOrderOrEqualTrue() {
    final LocalDate a = LocalDate.of(2011, 7, 2);
    final LocalDate b = LocalDate.of(2011, 7, 3);
    ArgumentChecker.inOrderOrEqual(a, b, "a", "b");
    ArgumentChecker.inOrderOrEqual(a, a, "a", "b");
    ArgumentChecker.inOrderOrEqual(b, b, "a", "b");
  }

  /**
   * Tests that comparable values are in order or equal i.e. not decreasing.
   */
  @Test
  public void testInOrderOrEqualGenerics() {
    final Pair<String, String> a = ObjectsPair.of("c", "d");
    final Pair<String, String> b = ObjectsPair.of("e", "f");
    final FirstThenSecondPairComparator<String, String> comparator = new FirstThenSecondPairComparator<>();
    final Comparable<? super Pair<String, String>> ca = new Comparable<Pair<String, String>>() {
      @Override
      public int compareTo(final Pair<String, String> other) {
        return comparator.compare(a, other);
      }
    };
    final Comparable<? super Pair<String, String>> cb = new Comparable<Pair<String, String>>() {
      @Override
      public int compareTo(final Pair<String, String> other) {
        return comparator.compare(b, other);
      }
    };
    ArgumentChecker.inOrderOrEqual(ca, b, "a", "b");
    ArgumentChecker.inOrderOrEqual(ca, a, "a", "b");
    ArgumentChecker.inOrderOrEqual(cb, b, "a", "b");
  }

  /**
   * Tests the exception and message when the values are decreasing.
   */
  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Input parameter 'a' must be before 'b'")
  public void testInOrderOrEqualFalse() {
    final LocalDate a = LocalDate.of(2011, 7, 3);
    final LocalDate b = LocalDate.of(2011, 7, 2);
    ArgumentChecker.inOrderOrEqual(a, b, "a", "b");
  }

}
