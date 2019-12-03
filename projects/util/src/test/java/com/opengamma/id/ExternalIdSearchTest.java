/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalIdSearch}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdSearchTest {
  private static final ExternalId ID_11 = ExternalId.of("D1", "V1");
  private static final ExternalId ID_21 = ExternalId.of("D2", "V1");
  private static final ExternalId ID_12 = ExternalId.of("D1", "V2");

  //-------------------------------------------------------------------------
  /**
   * Tests the no-args constructor.
   */
  @Test
  public void testConstructorNoArgs() {
    final ExternalIdSearch test = ExternalIdSearch.of();
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorExternalIdNull() {
    ExternalIdSearch.of((ExternalId) null);
  }

  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructorExternalId() {
    final ExternalIdSearch test = ExternalIdSearch.of(ID_11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(ID_11), test.getExternalIds());
    assertEquals(ExternalIdSearchType.ANY, test.getSearchType());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructorVarargsNoExternalIds() {
    final ExternalId[] args = new ExternalId[0];
    final ExternalIdSearch test = ExternalIdSearch.of(args);
    assertEquals(0, test.size());
  }

  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructorVarargsOneExternalId() {
    final ExternalId[] args = new ExternalId[] {ID_11};
    final ExternalIdSearch test = ExternalIdSearch.of(args);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(ID_11), test.getExternalIds());
  }

  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructorVarargsTwoExternalIds() {
    final ExternalId[] args = new ExternalId[] {ID_11, ID_12};
    final ExternalIdSearch test = ExternalIdSearch.of(args);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(ID_11, ID_12), test.getExternalIds());
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorVarargsNull() {
    final ExternalId[] args = null;
    ExternalIdSearch.of(args);
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorVarargsNoNulls() {
    final ExternalId[] args = new ExternalId[] {ID_11, null, ID_12};
    ExternalIdSearch.of(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructorIterableEmpty() {
    final ExternalIdSearch test = ExternalIdSearch.of(new ArrayList<ExternalId>());
    assertEquals(0, test.size());
  }

  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructorIterableTwo() {
    final ExternalIdSearch test = ExternalIdSearch.of(Arrays.asList(ID_11, ID_12));
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(ID_11, ID_12), test.getExternalIds());
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorIterableNull() {
    ExternalIdSearch.of((Iterable<ExternalId>) null);
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorIterableNoNulls() {
    ExternalIdSearch.of(Arrays.asList(ID_11, null, ID_12));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructorIterableTypeEmpty() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.EXACT, new ArrayList<ExternalId>());
    assertEquals(0, test.size());
    assertEquals(ExternalIdSearchType.EXACT, test.getSearchType());
  }

  /**
   * Tests the constructor.
   */
  @Test
  public void testConstructorIterableTypeTwo() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.EXACT, Arrays.asList(ID_11, ID_12));
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(ID_11, ID_12), test.getExternalIds());
    assertEquals(ExternalIdSearchType.EXACT, test.getSearchType());
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorIterableTypeNull() {
    ExternalIdSearch.of(ExternalIdSearchType.EXACT, (Iterable<ExternalId>) null);
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorIterableTypeNoNulls() {
    ExternalIdSearch.of(ExternalIdSearchType.EXACT, Arrays.asList(ID_11, null, ID_12));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the constructors are equivalent.
   */
  @Test
  public void testSingleExternalIdDifferentConstructors() {
    assertTrue(ExternalIdSearch.of(ID_11).equals(ExternalIdSearch.of(Collections.singleton(ID_11))));
  }

  /**
   * Tests that the constructors are equivalent.
   */
  @Test
  public void testSingleVersusMultipleExternalId() {
    assertFalse(ExternalIdSearch.of(ID_11).equals(ExternalIdSearch.of(ID_11, ID_12)));
    assertFalse(ExternalIdSearch.of(ID_11, ID_12).equals(ExternalIdSearch.of(ID_11)));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the withExternalIdAdded() method.
   */
  @Test
  public void testWithExternalIdAdded() {
    final ExternalIdSearch base = ExternalIdSearch.of(ExternalId.of("A", "B"));
    assertEquals(1, base.size());
    final ExternalIdSearch test = base.withExternalIdAdded(ExternalId.of("A", "C"));
    assertEquals(1, base.size());
    assertEquals(2, test.size());
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "B")));
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "C")));
  }

  /**
   * Tests that null cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithExternalIdAddedNull() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalId.of("A", "B"));
    test.withExternalIdAdded(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the withExternalIdsAdded() method.
   */
  @Test
  public void testWithExternalIdsVarargsAdded() {
    final ExternalIdSearch base = ExternalIdSearch.of(ExternalId.of("A", "B"));
    assertEquals(1, base.size());
    final ExternalIdSearch test = base.withExternalIdsAdded(ExternalId.of("A", "C"), ExternalId.of("A", "D"));
    assertEquals(1, base.size());
    assertEquals(3, test.size());
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "B")));
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "C")));
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "D")));
  }

  /**
   * Tests that null cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithExternalIdsVarargsAddedNull() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalId.of("A", "B"));
    test.withExternalIdsAdded((ExternalId[]) null);
  }

  /**
   * Tests the withExternalIdsAdded() method.
   */
  @Test
  public void testWithExternalIdsIterableAdded() {
    final ExternalIdSearch base = ExternalIdSearch.of(ExternalId.of("A", "B"));
    assertEquals(1, base.size());
    final ExternalIdSearch test = base.withExternalIdsAdded(Arrays.asList(ExternalId.of("A", "C"), ExternalId.of("A", "D")));
    assertEquals(1, base.size());
    assertEquals(3, test.size());
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "B")));
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "C")));
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "D")));
  }

  /**
   * Tests that null cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithExternalIdsIterableAddedNull() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalId.of("A", "B"));
    test.withExternalIdsAdded((List<ExternalId>) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests after an id is removed.
   */
  @Test
  public void testWithExternalIdRemovedMatch() {
    final ExternalIdSearch base = ExternalIdSearch.of(ExternalId.of("A", "B"));
    assertEquals(1, base.size());
    final ExternalIdSearch test = base.withExternalIdRemoved(ExternalId.of("A", "B"));
    assertEquals(1, base.size());
    assertEquals(0, test.size());
  }

  /**
   * Tests after an id is not removed.
   */
  @Test
  public void testWithExternalIdRemovedNoMatch() {
    final ExternalIdSearch base = ExternalIdSearch.of(ExternalId.of("A", "B"));
    assertEquals(1, base.size());
    final ExternalIdSearch test = base.withExternalIdRemoved(ExternalId.of("A", "C"));
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "B")));
  }

  /**
   * Tests after null is removed.
   */
  @Test
  public void testWithExternalIdRemovedNull() {
    final ExternalIdSearch base = ExternalIdSearch.of(ExternalId.of("A", "B"));
    assertEquals(1, base.size());
    final ExternalIdSearch test = base.withExternalIdRemoved(null);
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "B")));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the withSearchType() method.
   */
  @Test
  public void testWithSearchType() {
    final ExternalIdSearch base = ExternalIdSearch.of(ExternalId.of("A", "B"));
    assertEquals(ExternalIdSearchType.ANY, base.getSearchType());
    final ExternalIdSearch test = base.withSearchType(ExternalIdSearchType.EXACT);
    assertEquals(ExternalIdSearchType.EXACT, test.getSearchType());
    assertEquals(test, test.withSearchType(ExternalIdSearchType.EXACT));
    assertSame(test, test.withSearchType(ExternalIdSearchType.EXACT));
  }

  /**
   * Tests that the withSearchType() method cannot accept null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithSearchTypeNull() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalId.of("A", "B"));
    test.withSearchType(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the size() method.
   */
  @Test
  public void testSize() {
    assertEquals(0, ExternalIdSearch.of().size());
    assertEquals(1, ExternalIdSearch.of(ID_11).size());
    assertEquals(2, ExternalIdSearch.of(ID_11, ID_12).size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the iterator.
   */
  @Test
  public void testIterator() {
    final Set<ExternalId> expected = new HashSet<>();
    expected.add(ID_11);
    expected.add(ID_12);
    final Iterable<ExternalId> base = ExternalIdSearch.of(ID_11, ID_12);
    final Iterator<ExternalId> test = base.iterator();
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(false, test.hasNext());
    assertEquals(0, expected.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests EXACT match with one id.
   */
  @Test
  public void testMatchesOneExact() {
    final ExternalIdSearch test1 = ExternalIdSearch.of(ExternalIdSearchType.EXACT, ID_11);
    assertTrue(test1.matches(ID_11));
    assertFalse(test1.matches(ID_21));

    final ExternalIdSearch test2 = ExternalIdSearch.of(ExternalIdSearchType.EXACT, ID_11, ID_21);
    assertFalse(test2.matches(ID_11));
    assertFalse(test2.matches(ID_12));
    assertFalse(test2.matches(ID_21));
  }

  /**
   * Tests ALL match with one id.
   */
  @Test
  public void testMatchesOneAll() {
    final ExternalIdSearch test1 = ExternalIdSearch.of(ExternalIdSearchType.ALL, ID_11);
    assertTrue(test1.matches(ID_11));
    assertFalse(test1.matches(ID_12));

    final ExternalIdSearch test2 = ExternalIdSearch.of(ExternalIdSearchType.ALL, ID_11, ID_21);
    assertFalse(test2.matches(ID_11));
    assertFalse(test2.matches(ID_12));
    assertFalse(test2.matches(ID_21));
  }

  /**
   * Tests ANY match with one id.
   */
  @Test
  public void testMatchesOneAny() {
    final ExternalIdSearch test1 = ExternalIdSearch.of(ExternalIdSearchType.ANY, ID_11);
    assertTrue(test1.matches(ID_11));
    assertFalse(test1.matches(ID_12));

    final ExternalIdSearch test2 = ExternalIdSearch.of(ExternalIdSearchType.ANY, ID_11, ID_21);
    assertTrue(test2.matches(ID_11));
    assertFalse(test2.matches(ID_12));
    assertTrue(test2.matches(ID_21));
  }

  /**
   * Tests NONE match with one id.
   */
  @Test
  public void testMatchesOneNone() {
    final ExternalIdSearch test1 = ExternalIdSearch.of(ExternalIdSearchType.NONE, ID_11);
    assertFalse(test1.matches(ID_11));
    assertTrue(test1.matches(ID_12));

    final ExternalIdSearch test2 = ExternalIdSearch.of(ExternalIdSearchType.NONE, ID_11, ID_21);
    assertFalse(test2.matches(ID_11));
    assertTrue(test2.matches(ID_12));
    assertFalse(test2.matches(ID_21));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests EXACT.
   */
  @Test
  public void testMatchesExact() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.EXACT, ID_11, ID_12);
    assertTrue(test.matches(ExternalIdSearch.of(ID_11, ID_12)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_11, ID_12, ID_21)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_11)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_12)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_21)));
    assertFalse(test.matches(ExternalIdSearch.of()));
  }

  /**
   * Tests ALL.
   */
  @Test
  public void testMatchesAll() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.ALL, ID_11, ID_12);
    assertTrue(test.matches(ExternalIdSearch.of(ID_11, ID_12)));
    assertTrue(test.matches(ExternalIdSearch.of(ID_11, ID_12, ID_21)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_11)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_12)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_21)));
    assertFalse(test.matches(ExternalIdSearch.of()));
  }

  /**
   * Tests ANY.
   */
  @Test
  public void testMatchesAny() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.ANY, ID_11, ID_12);
    assertTrue(test.matches(ExternalIdSearch.of(ID_11, ID_12)));
    assertTrue(test.matches(ExternalIdSearch.of(ID_11, ID_12, ID_21)));
    assertTrue(test.matches(ExternalIdSearch.of(ID_11)));
    assertTrue(test.matches(ExternalIdSearch.of(ID_12)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_21)));
    assertFalse(test.matches(ExternalIdSearch.of()));
  }

  /**
   * Tests NONE.
   */
  @Test
  public void testMatchesNone() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.NONE, ID_11, ID_12);
    assertFalse(test.matches(ExternalIdSearch.of(ID_11, ID_12)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_11, ID_12, ID_21)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_11)));
    assertFalse(test.matches(ExternalIdSearch.of(ID_12)));
    assertTrue(test.matches(ExternalIdSearch.of(ID_21)));
    assertTrue(test.matches(ExternalIdSearch.of()));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the containsAll() method.
   */
  @Test
  public void testContainsAll1() {
    final ExternalIdSearch test = ExternalIdSearch.of(ID_11);
    assertFalse(test.containsAll(ExternalIdSearch.of(ID_11, ID_12)));
    assertTrue(test.containsAll(ExternalIdSearch.of(ID_11)));
    assertFalse(test.containsAll(ExternalIdSearch.of(ID_12)));
    assertFalse(test.containsAll(ExternalIdSearch.of(ID_21)));
    assertTrue(test.containsAll(ExternalIdSearch.of()));
  }

  /**
   * Tests the containsAll() method.
   */
  @Test
  public void testcontainsAll2() {
    final ExternalIdSearch test = ExternalIdSearch.of(ID_11, ID_12);
    assertTrue(test.containsAll(ExternalIdSearch.of(ID_11, ID_12)));
    assertTrue(test.containsAll(ExternalIdSearch.of(ID_11)));
    assertTrue(test.containsAll(ExternalIdSearch.of(ID_12)));
    assertFalse(test.containsAll(ExternalIdSearch.of(ID_21)));
    assertTrue(test.containsAll(ExternalIdSearch.of()));
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testContainsAllNull() {
    final ExternalIdSearch test = ExternalIdSearch.of(ID_11, ID_12);
    test.containsAll(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests containsAny().
   */
  @Test
  public void testContainsAny() {
    final ExternalIdSearch test = ExternalIdSearch.of(ID_11, ID_12);
    assertTrue(test.containsAny(ExternalIdSearch.of(ID_11, ID_12)));
    assertTrue(test.containsAny(ExternalIdSearch.of(ID_11)));
    assertTrue(test.containsAny(ExternalIdSearch.of(ID_12)));
    assertFalse(test.containsAny(ExternalIdSearch.of(ID_21)));
    assertFalse(test.containsAny(ExternalIdSearch.of()));
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testContainsAnyNull() {
    final ExternalIdSearch test = ExternalIdSearch.of(ID_11, ID_12);
    test.containsAny(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the contains() method.
   */
  @Test
  public void testContains() {
    final ExternalIdSearch test = ExternalIdSearch.of(ID_11, ID_12);
    assertTrue(test.contains(ID_11));
    assertTrue(test.contains(ID_11));
    assertFalse(test.contains(ID_21));
  }

  /**
   * Tests that null is allowed as an input.
   */
  @Test
  public void testContainsNull() {
    final ExternalIdSearch test = ExternalIdSearch.of(ID_11, ID_12);
    assertFalse(test.contains(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests an EXACT match does not match with no ids and does with ids.
   */
  @Test
  public void testCanMatchExact() {
    ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.EXACT);
    assertFalse(ExternalIdSearch.canMatch(test));
    test = test.withExternalIdAdded(ID_11);
    assertTrue(ExternalIdSearch.canMatch(test));
  }

  /**
   * Tests an ALL match does not match with no ids and does with ids.
   */
  @Test
  public void testCanMatchAll() {
    ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.ALL);
    assertFalse(ExternalIdSearch.canMatch(test));
    test = test.withExternalIdAdded(ID_11);
    assertTrue(ExternalIdSearch.canMatch(test));
  }

  /**
   * Tests an ANY match does not match with no ids and does with ids.
   */
  @Test
  public void testCanMatchAny() {
    ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.ANY);
    assertFalse(ExternalIdSearch.canMatch(test));
    test = test.withExternalIdAdded(ID_11).withExternalIdAdded(ID_12);
    assertTrue(ExternalIdSearch.canMatch(test));
  }

  /**
   * Tests a NONE type can match anything.
   */
  @Test
  public void testCanMatchNone() {
    ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.NONE);
    assertTrue(ExternalIdSearch.canMatch(test));
    test = test.withExternalIdAdded(ID_11);
    assertTrue(ExternalIdSearch.canMatch(test));
  }

  /**
   * Test that null can match anything.
   */
  @Test
  public void testCanMatchNull() {
    assertTrue(ExternalIdSearch.canMatch(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that EXACT cannot always match.
   */
  @Test
  public void testAlwaysMatchExact() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.EXACT);
    assertFalse(test.alwaysMatches());
    assertFalse(test.withExternalIdAdded(ID_11).alwaysMatches());
  }

  /**
   * Tests that ALL cannot always match.
   */
  @Test
  public void testAlwaysMatchAll() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.ALL);
    assertFalse(test.alwaysMatches());
    assertFalse(test.withExternalIdAdded(ID_11).alwaysMatches());
  }

  /**
   * Tests that ANY cannot always match.
   */
  @Test
  public void testAlwaysMatchAny() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.ANY);
    assertFalse(test.alwaysMatches());
    assertFalse(test.withExternalIdAdded(ID_11).alwaysMatches());
  }

  /**
   * Tests that NONE can always match only without ids.
   */
  @Test
  public void testAlwaysMatchNull() {
    final ExternalIdSearch test = ExternalIdSearch.of(ExternalIdSearchType.NONE);
    assertTrue(test.alwaysMatches());
    assertFalse(test.withExternalIdAdded(ID_11).alwaysMatches());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsSameEmpty() {
    final ExternalIdSearch a = ExternalIdSearch.of();
    final ExternalIdSearch b = ExternalIdSearch.of();

    assertEquals(a, b);
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsSameNonEmpty() {
    final ExternalIdSearch a = ExternalIdSearch.of(ID_11, ID_12);
    final ExternalIdSearch b = ExternalIdSearch.of(ID_11, ID_12);

    assertEquals(true, a.equals(a));
    assertEquals(a, b);
  }

  /**
   * Tests the equals() method.
   */
  @Test
  public void testEqualsDifferent() {
    final ExternalIdSearch a = ExternalIdSearch.of();
    final ExternalIdSearch b = ExternalIdSearch.of(ID_11, ID_12);
    final ExternalIdSearch c = ExternalIdSearch.of(ID_11, ID_12).withSearchType(ExternalIdSearchType.EXACT);

    assertNotEquals(a, b);
    assertNotEquals(b, a);
    assertNotEquals(b, c);
    assertNotEquals("A", b);
    assertNotEquals(null, b);

  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final ExternalIdSearch a = ExternalIdSearch.of(ID_11, ID_12);
    final ExternalIdSearch b = ExternalIdSearch.of(ID_11, ID_12);

    assertEquals(a.hashCode(), b.hashCode());
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToStringEmpty() {
    final ExternalIdSearch test = ExternalIdSearch.of();
    assertTrue(test.toString().contains("[]"));
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToStringNonEmpty() {
    final ExternalIdSearch test = ExternalIdSearch.of(ID_11, ID_12);
    assertTrue(test.toString().contains(ID_11.toString()));
    assertTrue(test.toString().contains(ID_12.toString()));
  }

}
