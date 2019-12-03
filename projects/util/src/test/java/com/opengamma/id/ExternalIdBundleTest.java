/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalIdBundle}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdBundleTest {
  private static final ExternalScheme SCHEME = ExternalScheme.of("Scheme");
  private static final ExternalId ID_11 = ExternalId.of("D1", "V1");
  private static final ExternalId ID_21 = ExternalId.of("D2", "V1");
  private static final ExternalId ID_12 = ExternalId.of("D1", "V2");
  private static final ExternalId ID_22 = ExternalId.of("D2", "V2");

  /**
   * Tests that the number of ids in the empty bundle is zero.
   */
  @Test
  public void singletonEmpty() {
    assertEquals(ExternalIdBundle.EMPTY.size(), 0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the factory constructor that takes an ExternalScheme.
   */
  @Test
  public void testFactoryExternalSchemeString() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11.getScheme(), ID_11.getValue());
    assertEquals(test.size(), 1);
    assertEquals(test.getExternalIds(), Sets.newHashSet(ID_11));
  }

  /**
   * Tests that a scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryExternalSchemeStringNullScheme() {
    ExternalIdBundle.of((ExternalScheme) null, "value");
  }

  /**
   * Tests that a value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryExternalSchemeStringNullValue() {
    ExternalIdBundle.of(SCHEME, (String) null);
  }

  /**
   * Tests that a value cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryExternalSchemeStringEmptyValue() {
    ExternalIdBundle.of(SCHEME, "");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the factory constructor that takes the scheme as a string.
   */
  @Test
  public void testFactoryStringString() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11.getScheme().getName(), ID_11.getValue());
    assertEquals(test.size(), 1);
    assertEquals(test.getExternalIds(), Sets.newHashSet(ID_11));
  }

  /**
   * Tests that a scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testfactoryStringStringNullScheme() {
    ExternalIdBundle.of((String) null, "value");
  }

  /**
   * Tests that a value cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringNullValue() {
    ExternalIdBundle.of("Scheme", (String) null);
  }

  /**
   * Tests that a value cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryStringStringEmptyValue() {
    ExternalIdBundle.of("Scheme", "");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void factoryOfExternalIdNull() {
    ExternalIdBundle.of((ExternalId) null);
  }

  /**
   * Tests the static constructor that takes an external id.
   */
  @Test
  public void testFactoryOfExternalId() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11);
    assertEquals(test.size(), 1);
    assertEquals(test.getExternalIds(), Sets.newHashSet(ID_11));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an empty bundle is created.
   */
  @Test
  public void testFactoryOfVarargsNoExternalIds() {
    final ExternalIdBundle test = ExternalIdBundle.of();
    assertEquals(test.size(), 0);
  }

  /**
   * Tests the static constructor that takes external ids.
   */
  @Test
  public void testFactoryOfVarargsTwoExternalIds() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11, ID_12);
    assertEquals(test.size(), 2);
    assertEquals(Arrays.asList(ID_11, ID_12), test.getExternalIds());
  }

  /**
   * Tests that the array of external ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryOfVarargsNull() {
    ExternalIdBundle.of((ExternalId[]) null);
  }

  /**
   * Tests that nulls are not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryOfVarargsNoNulls() {
    ExternalIdBundle.of(ID_11, null, ID_12);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the static constructor that takes an iterable.
   */
  @Test
  public void testFactoryOfIterableEmpty() {
    final ExternalIdBundle test = ExternalIdBundle.of(new ArrayList<ExternalId>());
    assertEquals(test.size(), 0);
  }

  /**
   * Tests the static constructor that takes an iterable.
   */
  @Test
  public void testFactoryOfIterableTwo() {
    final ExternalIdBundle test = ExternalIdBundle.of(Arrays.asList(ID_11, ID_12));
    assertEquals(test.size(), 2);
    assertEquals(Arrays.asList(ID_11, ID_12), test.getExternalIds());
  }

  /**
   * Tests that the iterable cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryOfIterableNull() {
    ExternalIdBundle.of((Iterable<ExternalId>) null);
  }

  /**
   * Tests that null entries are not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryOfIterableNoNulls() {
    ExternalIdBundle.of(Arrays.asList(ID_11, null, ID_12));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests parsing an empty collection.
   */
  @Test
  public void testFactoryParseIterableEmpty() {
    final ExternalIdBundle test = ExternalIdBundle.parse(new ArrayList<String>());
    assertEquals(test.size(), 0);
  }

  /**
   * Tests that a collection of strings is parsed correctly.
   */
  @Test
  public void testFactoryParseIterableTwo() {
    final ExternalIdBundle test = ExternalIdBundle.parse(Arrays.asList(ID_12.toString(), ID_11.toString()));
    assertEquals(2, test.size());
    assertEquals(Arrays.asList(ID_11, ID_12), test.getExternalIds());
  }

  /**
   * Tests that a null iterable cannot be parsed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryParseIterableNull() {
    ExternalIdBundle.parse((Iterable<String>) null);
  }

  /**
   * Tests that an iterable containing nulls cannot be parsed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryParseIterableNoNulls() {
    ExternalIdBundle.parse(Arrays.asList(ID_11.toString(), null, ID_12.toString()));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that bundles constructed differently are equal.
   */
  @Test
  public void testSingleIdDifferentConstructors() {
    assertEquals(ExternalIdBundle.of(ID_11), ExternalIdBundle.of(Collections.singleton(ID_11)));
  }

  /**
   * Tests that bundles constructed differently are equal.
   */
  @Test
  public void testMultipleIdDifferentConstructors() {
    assertEquals(ExternalIdBundle.of(ID_11, ID_22, ID_21, ID_12),
        ExternalIdBundle.of(Arrays.asList(ID_11, ID_21, ID_12, ID_22)));
  }

  /**
   * Tests that bundles constructed differently with different elements are not equal.
   */
  @Test
  public void testSingleVersusMultipleId() {
    assertNotEquals(ExternalIdBundle.of(ID_11), ExternalIdBundle.of(ID_11, ID_12));
    assertNotEquals(ExternalIdBundle.of(ID_11, ID_12), ExternalIdBundle.of(ID_11));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the getExternalIdBundle() method.
   */
  @Test
  public void testGetExternalIdBundle() {
    final ExternalIdBundle input = ExternalIdBundle.of(ID_11, ID_22);
    assertSame(input.getExternalIdBundle(), input);
    assertEquals(input.getExternalIdBundle(), input);
  }

  /**
   * Tests the toBundle() method.
   */
  @Test
  public void testToBundle() {
    final ExternalIdBundle input = ExternalIdBundle.of(ID_11, ID_22);
    assertSame(input.toBundle(), input);
    assertEquals(input.toBundle(), input);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the getExternalId() method.
   */
  @Test
  public void getExternalId() {
    final ExternalIdBundle input = ExternalIdBundle.of(ID_11, ID_22);
    assertEquals(input.getExternalId(ExternalScheme.of("D1")), ExternalId.of("D1", "V1"));
    assertEquals(input.getExternalId(ExternalScheme.of("D2")), ExternalId.of("D2", "V2"));
    assertNull(input.getExternalId(ExternalScheme.of("Kirk Wylie")));
    assertNull(input.getExternalId(null));
  }

  /**
   * Tests the getValue() method.
   */
  @Test
  public void testGetValue() {
    final ExternalIdBundle input = ExternalIdBundle.of(ID_11, ID_22);
    assertEquals(input.getValue(ExternalScheme.of("D1")), "V1");
    assertEquals(input.getValue(ExternalScheme.of("D2")), "V2");
    assertNull(input.getValue(ExternalScheme.of("Kirk Wylie")));
    assertNull(input.getValue(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that adding an external id returns a new bundle.
   */
  @Test
  public void testWithExternalId() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    final ExternalIdBundle test = base.withExternalId(ID_21);
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 2);
    assertTrue(test.getExternalIds().contains(ID_11));
    assertTrue(test.getExternalIds().contains(ID_21));
  }

  /**
   * Tests that adding an id that is already present has no effect and returns the original object.
   */
  @Test
  public void testWithExternalIdSame() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    final ExternalIdBundle test = base.withExternalId(ID_11);
    assertSame(base, test);
    assertEquals(base, test);
  }

  /**
   * Tests that a null id cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithExternalIdNull() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    base.withExternalId((ExternalId) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests adding multiple ids.
   */
  @Test
  public void testWithExternalIds() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    final ExternalIdBundle test = base.withExternalIds(ImmutableList.of(ID_12, ID_21));
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 3);
    assertTrue(test.getExternalIds().contains(ID_11));
    assertTrue(test.getExternalIds().contains(ID_12));
    assertTrue(test.getExternalIds().contains(ID_21));
  }

  /**
   * Tests that adding ids that are already present has no effect and returns the original object.
   */
  @Test
  public void testWithExternalIdsSame() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11, ID_22);
    final ExternalIdBundle test = base.withExternalIds(ImmutableList.of(ID_11, ID_11, ID_22));
    assertSame(base, test);
    assertEquals(base, test);
  }

  /**
   * Tests that a null id cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithExternalIdsNull() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    base.withExternalIds((Iterable<ExternalId>) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the removal of an id.
   */
  @Test
  public void testWithoutExternalIdMatch() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    final ExternalIdBundle test = base.withoutExternalId(ID_11);
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 0);
  }

  /**
   * Tests that trying to remove an id that is not present returns the same object.
   */
  @Test
  public void testWithoutExternalIdNoMatch() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    final ExternalIdBundle test = base.withoutExternalId(ID_12);
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 1);
    assertSame(base, test);
    assertTrue(test.getExternalIds().contains(ID_11));
  }

  /**
   * Tests that it is not possible to try to remove a null id.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithoutExternalIdNull() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    base.withoutExternalId(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that all ids with matching schemes are removed.
   */
  @Test
  public void testWithoutSchemeExternalSchemeMatch() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11, ID_12, ID_22);
    final ExternalIdBundle test = base.withoutScheme(ID_11.getScheme());
    assertEquals(base.size(), 3);
    assertEquals(test.size(), 1);
    assertEquals(test, ExternalIdBundle.of(ID_22));
  }

  /**
   * Tests that removing a non-matching scheme has no effect and returns the same object.
   */
  @Test
  public void testWithoutSchemeExternalSchemeNoMatch() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    final ExternalIdBundle test = base.withoutScheme(ID_21.getScheme());
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 1);
    assertNotSame(base, test);
    assertEquals(base, test);
  }

  /**
   * Tests that trying to remove all ids with null scheme has no effect and returns the same object.
   */
  @Test
  public void testWithoutSchemeExternalSchemeNull() {
    final ExternalIdBundle base = ExternalIdBundle.of(ID_11);
    final ExternalIdBundle test = base.withoutScheme(null);
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 1);
    assertNotSame(base, test);
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the size() method.
   */
  @Test
  public void testSize() {
    assertEquals(ExternalIdBundle.EMPTY.size(), 0);
    assertEquals(ExternalIdBundle.of(ID_11).size(), 1);
    assertEquals(ExternalIdBundle.of(ID_11, ID_12).size(), 2);
  }

  /**
   * Tests the isEmpty() method.
   */
  @Test
  public void testIsEmpty() {
    assertTrue(ExternalIdBundle.EMPTY.isEmpty());
    assertFalse(ExternalIdBundle.of(ID_11).isEmpty());
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
    final Iterable<ExternalId> base = ExternalIdBundle.of(ID_11, ID_12);
    final Iterator<ExternalId> test = base.iterator();
    assertTrue(test.hasNext());
    assertTrue(expected.remove(test.next()));
    assertTrue(test.hasNext());
    assertTrue(expected.remove(test.next()));
    assertFalse(test.hasNext());
    assertEquals(expected.size(), 0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the containsAll() method.
   */
  @Test
  public void testContainsAll1() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11);
    assertFalse(test.containsAll(ExternalIdBundle.of(ID_11, ID_12)));
    assertTrue(test.containsAll(ExternalIdBundle.of(ID_11)));
    assertFalse(test.containsAll(ExternalIdBundle.of(ID_12)));
    assertFalse(test.containsAll(ExternalIdBundle.of(ID_21)));
    assertTrue(test.containsAll(ExternalIdBundle.EMPTY));
  }

  /**
   * Tests the containsAll() method.
   */
  @Test
  public void testContainsAll2() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11, ID_12);
    assertTrue(test.containsAll(ExternalIdBundle.of(ID_11, ID_12)));
    assertTrue(test.containsAll(ExternalIdBundle.of(ID_11)));
    assertTrue(test.containsAll(ExternalIdBundle.of(ID_12)));
    assertFalse(test.containsAll(ExternalIdBundle.of(ID_21)));
    assertTrue(test.containsAll(ExternalIdBundle.EMPTY));
  }

  /**
   * Tests that the containsAll() method does not accept null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testContainsAllNull() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11, ID_12);
    test.containsAll(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the containsAny() method.
   */
  @Test
  public void testContainsAny() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11, ID_12);
    assertTrue(test.containsAny(ExternalIdBundle.of(ID_11, ID_12)));
    assertTrue(test.containsAny(ExternalIdBundle.of(ID_11)));
    assertTrue(test.containsAny(ExternalIdBundle.of(ID_12)));
    assertFalse(test.containsAny(ExternalIdBundle.of(ID_21)));
    assertFalse(test.containsAny(ExternalIdBundle.EMPTY));
  }

  /**
   * Tests that the containsAny() method does not accept null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testContainsAnyNull() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11, ID_12);
    test.containsAny(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the contains() method.
   */
  @Test
  public void testContains() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11, ID_12);
    assertTrue(test.contains(ID_11));
    assertTrue(test.contains(ID_11));
    assertFalse(test.contains(ID_21));
  }

  /**
   * Tests that the contains() method can accept null.
   */
  @Test
  public void testContainsNull() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_11, ID_12);
    assertFalse(test.contains(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toStringList() method.
   */
  @Test
  public void testToStringList() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_12, ID_11);
    assertEquals(test.toStringList(), Arrays.asList(ID_11.toString(), ID_12.toString()));
  }

  /**
   * Tests the toStringList() method.
   */
  @Test
  public void testToStringListEmpty() {
    final ExternalIdBundle test = ExternalIdBundle.EMPTY;
    assertEquals(test.toStringList(), new ArrayList<String>());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the compareTo() method on different sized bundles.
   */
  @Test
  public void testCompareToDifferentSizes() {
    final ExternalIdBundle a1 = ExternalIdBundle.EMPTY;
    final ExternalIdBundle a2 = ExternalIdBundle.of(ID_11);

    assertEquals(a1.compareTo(a1), 0);
    assertTrue(a1.compareTo(a2) < 0);
    assertTrue(a2.compareTo(a1) > 0);
    assertEquals(a2.compareTo(a2), 0);
  }

  /**
   * Tests the compareTo() method on the same sized bundles.
   */
  @Test
  public void testCompareToSameSizes() {
    final ExternalIdBundle a1 = ExternalIdBundle.of(ID_11);
    final ExternalIdBundle a2 = ExternalIdBundle.of(ID_12);

    assertEquals(a1.compareTo(a1), 0);
    assertTrue(a1.compareTo(a2) < 0);
    assertTrue(a2.compareTo(a1) > 0);
    assertEquals(a2.compareTo(a2), 0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests equality for empty bundles.
   */
  @Test
  public void testEqualsSameEmpty() {
    final ExternalIdBundle a1 = ExternalIdBundle.EMPTY;
    final ExternalIdBundle a2 = ExternalIdBundle.of(ID_11).withoutScheme(ID_11.getScheme());

    assertEquals(a1, a1);
    assertEquals(a2, a2);
    assertEquals(a2, a1);
    assertEquals(a2, a2);
  }

  /**
   * Tests equality for bundles.
   */
  @Test
  public void testEqualsSameNonEmpty() {
    final ExternalIdBundle a1 = ExternalIdBundle.of(ID_11, ID_12);
    final ExternalIdBundle a2 = ExternalIdBundle.of(ID_11, ID_12);

    assertEquals(a1, a1);
    assertEquals(a1, a2);
    assertEquals(a2, a1);
    assertEquals(a2, a2);
  }

  /**
   * Tests equality for different bundles.
   */
  @Test
  public void testEqualsDifferent() {
    final ExternalIdBundle a = ExternalIdBundle.EMPTY;
    final ExternalIdBundle b = ExternalIdBundle.of(ID_11, ID_12);

    assertEquals(a, a);
    assertNotEquals(a, b);
    assertNotEquals(b, a);
    assertEquals(b, b);

    assertNotEquals("Rubbish", a);
    assertNotEquals(null, a);
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final ExternalIdBundle a = ExternalIdBundle.of(ID_11, ID_12);
    final ExternalIdBundle b = ExternalIdBundle.of(ID_11, ID_12);

    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(a.hashCode(), a.hashCode());
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToStringEmpty() {
    final ExternalIdBundle test = ExternalIdBundle.EMPTY;
    assertEquals("Bundle[]", test.toString());
  }

  /**
   * Tests the toString() method.
   */
  @Test
  public void testToStringNonEmpty() {
    final ExternalIdBundle test = ExternalIdBundle.of(ID_12, ID_11);
    assertEquals("Bundle[" + ID_11.toString() + ", " + ID_12.toString() + "]", test.toString());
  }

  /**
   * Tests the getExternalIds() method.
   */
  @Test
  public void testGetExternalIds() {
    final ExternalIdBundle bundle = ExternalIdBundle.of(ID_11, ID_12, ID_21, ID_22);
    final Set<ExternalId> expected = Sets.newHashSet(ID_11, ID_12);
    assertEquals(expected, bundle.getExternalIds(ExternalScheme.of("D1")));
  }

  /**
   * Tests the getValues() method.
   */
  @Test
  public void testGetValues() {
    final ExternalIdBundle bundle = ExternalIdBundle.of(ID_11, ID_12, ID_21, ID_22);
    final Set<String> expected = Sets.newHashSet(ID_11.getValue(), ID_12.getValue());
    assertEquals(bundle.getValues(ExternalScheme.of("D1")), expected);
  }

}
