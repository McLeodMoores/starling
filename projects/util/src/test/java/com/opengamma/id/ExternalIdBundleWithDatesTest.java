/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.threeten.bp.Month.DECEMBER;
import static org.threeten.bp.Month.JANUARY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalIdBundleWithDates}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdBundleWithDatesTest {
  private static final ExternalId ID_11 = ExternalId.of("D1", "V1");
  private static final ExternalIdWithDates IDWD_11 = ExternalIdWithDates.of(ID_11, LocalDate.of(2000, JANUARY, 1), LocalDate.of(2001, JANUARY, 1));
  private static final ExternalId ID_12 = ExternalId.of("D2", "V1");
  private static final ExternalIdWithDates IDWD_12 = ExternalIdWithDates.of(ID_12, null, null);
  private static final ExternalId ID_21 = ExternalId.of("D1", "V2");
  private static final ExternalIdWithDates IDWD_21 = ExternalIdWithDates.of(ID_21, LocalDate.of(2001, JANUARY, 2), null);
  private static final ExternalId ID_22 = ExternalId.of("D2", "V2");
  private static final ExternalIdWithDates IDWD_22 = ExternalIdWithDates.of(ID_22, null, LocalDate.of(2010, DECEMBER, 30));

  /**
   * Tests creation of an empty bundle.
   */
  @Test
  public void testSingletonEmpty() {
    assertEquals(0, ExternalIdBundleWithDates.EMPTY.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests creation of an empty bundle.
   */
  @Test
  public void testFactoryOfVarargsNoExternalIds() {
    final ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of();
    assertEquals(0, test.size());
  }

  /**
   * Tests creation of a bundle containing one id using the varargs constructor.
   */
  @Test
  public void testFactoryOfVarargsOneExternalId() {
    final ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of(IDWD_11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(IDWD_11), test.getExternalIds());
  }

  /**
   * Tests creation of a bundle using the varargs constructor.
   */
  @Test
  public void testFactoryOfVarargsTwoExternalIds() {
    final ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of(IDWD_11, IDWD_21);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(IDWD_11, IDWD_21), test.getExternalIds());
  }

  /**
   * Tests that the varargs constructor does not accept null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryOfVarargsNull() {
    ExternalIdBundleWithDates.of((ExternalIdWithDates[]) null);
  }

  /**
   * Tests that the varargs constructor does not accept null entries.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryOfVarargsNoNulls() {
    ExternalIdBundleWithDates.of(IDWD_11, null, IDWD_21);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the no-args constructor.
   */
  @Test
  public void testConstructorNoargs() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates();
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the constructor that takes an external id.
   */
  @Test
  public void testConstructorExternalId() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_11);
    assertEquals(test.size(), 1);
    assertEquals(test.getExternalIds(), Sets.newHashSet(IDWD_11));
  }

  /**
   * Tests that the constructor does not accept null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorExternalIdNull() {
    new ExternalIdBundleWithDates((ExternalIdWithDates) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the bundle constructor.
   */
  @Test
  public void testConstructorBundleEmpty() {
    final ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of(ExternalIdBundle.EMPTY);
    assertEquals(test.size(), 0);
  }

  /**
   * Tests the bundle constructor.
   */
  @Test
  public void testConstructorBundleTwo() {
    final ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of(ExternalIdBundle.of(ID_21, ID_11));
    assertEquals(test.size(), 2);
    assertEquals(test.getExternalIds(), Arrays.asList(ExternalIdWithDates.of(ID_11), ExternalIdWithDates.of(ID_21)));
  }

  /**
   * Tests the bundle constructor with null input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorBundleNull() {
    ExternalIdBundleWithDates.of((ExternalIdBundle) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the varargs constructor.
   */
  @Test
  public void testConstructorVarargsEmpty() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(new ExternalIdWithDates[0]);
    assertEquals(test.size(), 0);
  }

  /**
   * Tests the varargs constructor.
   */
  @Test
  public void testConstructorVarargsTwo() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_21, IDWD_11);
    assertEquals(test.size(), 2);
    assertEquals(test.getExternalIds(), Arrays.asList(IDWD_11, IDWD_21));
  }

  /**
   * Tests the varargs constructor with null input.
   */
  @Test
  public void testConstructorVarargsNull() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates((ExternalIdWithDates[]) null);
    assertEquals(test.size(), 0);
  }

  /**
   * Tests that the varargs constructor does not allow null elements.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorVarargsNoNulls() {
    new ExternalIdBundleWithDates(IDWD_11, null, IDWD_21);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests construction of an empty bundle.
   */
  @Test
  public void testConstructorCollectionEmpty() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(new ArrayList<ExternalIdWithDates>());
    assertEquals(test.size(), 0);
  }

  /**
   * Tests construction of an empty bundle.
   */
  @Test
  public void testFactoryCollectionEmpty() {
    final ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of(new ArrayList<ExternalIdWithDates>());
    assertEquals(test.size(), 0);
  }

  /**
   * Tests the construction of a bundle.
   */
  @Test
  public void testConstructorCollectionTwo() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(Arrays.asList(IDWD_22, IDWD_21, IDWD_11));
    assertEquals(test.size(), 3);
    assertEquals(test.getExternalIds(), Arrays.asList(IDWD_11, IDWD_21, IDWD_22));
  }

  /**
   * Tests the construction of a bundle.
   */
  @Test
  public void testFactoryCollectionTwo() {
    final ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of(Arrays.asList(IDWD_22, IDWD_21, IDWD_11));
    assertEquals(test.size(), 3);
    assertEquals(test.getExternalIds(), Arrays.asList(IDWD_11, IDWD_21, IDWD_22));
  }

  /**
   * Tests construction with a null collection.
   */
  @Test
  public void testConstructorCollectionNull() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates((Collection<ExternalIdWithDates>) null);
    assertEquals(test.size(), 0);
  }

  /**
   * Tests construction with a null collection.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFactoryCollectionNull() {
    ExternalIdBundleWithDates.of((Collection<ExternalIdWithDates>) null);
  }

  /**
   * Tests that nulls are not allowed in the entries.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorCollectionNoNulls() {
    new ExternalIdBundleWithDates(Arrays.asList(IDWD_11, null, IDWD_21));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests construction from a single id.
   */
  @Test
  public void testSingleExternalIdDifferentConstructors() {
    assertEquals(new ExternalIdBundleWithDates(IDWD_11), new ExternalIdBundleWithDates(Collections.singleton(IDWD_11)));
  }

  /**
   * Tests construction from multiple ids.
   */
  @Test
  public void testSingleVersusMultipleExternalId() {
    assertNotEquals(new ExternalIdBundleWithDates(IDWD_11), new ExternalIdBundleWithDates(IDWD_11, IDWD_21));
    assertNotEquals(new ExternalIdBundleWithDates(IDWD_11, IDWD_21), new ExternalIdBundleWithDates(IDWD_11));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toBundle() method.
   */
  @Test
  public void testToBundle() {
    final ExternalIdBundleWithDates bundleWithDates = new ExternalIdBundleWithDates(IDWD_11, IDWD_22);
    assertEquals(ExternalIdBundle.of(ID_11, ID_22), bundleWithDates.toBundle());
  }

  /**
   * Tests the toBundle() method.
   */
  @Test
  public void testToBundleLocalDate() {
    final ExternalIdBundleWithDates bundleWithDates = new ExternalIdBundleWithDates(IDWD_11, IDWD_22);
    assertEquals(ExternalIdBundle.of(ID_11, ID_22), bundleWithDates.toBundle(LocalDate.of(2000, 6, 1)));
    assertEquals(ExternalIdBundle.of(ID_22), bundleWithDates.toBundle(LocalDate.of(2002, 6, 1)));
    assertEquals(ExternalIdBundle.EMPTY, bundleWithDates.toBundle(LocalDate.of(2011, 6, 1)));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the withExternalId() method.
   */
  @Test
  public void withExternalId() {
    final ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(IDWD_11);
    final ExternalIdBundleWithDates test = base.withExternalId(IDWD_12);
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 2);
    assertTrue(test.getExternalIds().contains(IDWD_11));
    assertTrue(test.getExternalIds().contains(IDWD_12));
  }

  /**
   * Tests the withExternalId() method.
   */
  @Test
  public void withExternalIdAlreadyPresent() {
    final ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(IDWD_11);
    final ExternalIdBundleWithDates test = base.withExternalId(IDWD_11);
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 1);
    assertSame(test, base);
    assertEquals(test, base);
  }

  /**
   * Tests the withExternalId() method with null input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithExternalIdNull() {
    final ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(IDWD_11);
    base.withExternalId(null);
  }

  /**
   * Tests the withoutExternalId() method that removes an id.
   */
  @Test
  public void testWithoutExternalIdMatch() {
    final ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(IDWD_11);
    final ExternalIdBundleWithDates test = base.withoutExternalId(IDWD_11);
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 0);
  }

  /**
   * Tests the withoutExternalId() method that does not remove an id.
   */
  @Test
  public void testWithoutExternalIdNoMatch() {
    final ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(IDWD_11);
    final ExternalIdBundleWithDates test = base.withoutExternalId(IDWD_21);
    assertEquals(base.size(), 1);
    assertEquals(test.size(), 1);
    assertSame(base, test);
    assertTrue(test.getExternalIds().contains(IDWD_11));
  }

  /**
   * Tests the withoutExternalId() method with null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithoutExternalIdNull() {
    final ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(IDWD_11);
    base.withoutExternalId(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the size() method.
   */
  @Test
  public void testSize() {
    assertEquals(new ExternalIdBundleWithDates().size(), 0);
    assertEquals(new ExternalIdBundleWithDates(IDWD_11).size(), 1);
    assertEquals(new ExternalIdBundleWithDates(IDWD_11, IDWD_21).size(), 2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the iterator.
   */
  @Test
  public void testIterator() {
    final Set<ExternalIdWithDates> expected = new HashSet<>();
    expected.add(IDWD_11);
    expected.add(IDWD_21);
    final Iterable<ExternalIdWithDates> base = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    final Iterator<ExternalIdWithDates> test = base.iterator();
    assertTrue(test.hasNext());
    assertTrue(expected.remove(test.next()));
    assertTrue(test.hasNext());
    assertTrue(expected.remove(test.next()));
    assertFalse(test.hasNext());
    assertEquals(expected.size(), 0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the containsAny() method.
   */
  @Test
  public void testContainsAny() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    assertTrue(test.containsAny(new ExternalIdBundleWithDates(IDWD_11, IDWD_21)));
    assertTrue(test.containsAny(new ExternalIdBundleWithDates(IDWD_11)));
    assertTrue(test.containsAny(new ExternalIdBundleWithDates(IDWD_21)));
    assertFalse(test.containsAny(new ExternalIdBundleWithDates(IDWD_12)));
    assertFalse(test.containsAny(new ExternalIdBundleWithDates()));
  }

  /**
   * Tests the containsAny() method with null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testContainsAnyNull() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    test.containsAny(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the containsAll() method.
   */
  @Test
  public void testContainsAll() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    assertFalse(test.containsAll(new ExternalIdBundleWithDates(IDWD_11, IDWD_21, IDWD_22)));
    assertTrue(test.containsAll(new ExternalIdBundleWithDates(IDWD_11, IDWD_21)));
    assertTrue(test.containsAll(new ExternalIdBundleWithDates(IDWD_11)));
    assertTrue(test.containsAll(new ExternalIdBundleWithDates(IDWD_21)));
    assertFalse(test.containsAll(new ExternalIdBundleWithDates(IDWD_12)));
    assertTrue(test.containsAll(new ExternalIdBundleWithDates()));
  }

  /**
   * Tests the containsAll() method with null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testContainsAllNull() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    test.containsAll(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the contains() method.
   */
  @Test
  public void testContains() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    assertEquals(true, test.contains(IDWD_11));
    assertEquals(true, test.contains(IDWD_11));
    assertEquals(false, test.contains(IDWD_12));
  }

  /**
   * Tests the contains() method with null.
   */
  @Test
  public void testContainsNull() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    assertFalse(test.contains(null));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the toStringList() method.
   */
  @Test
  public void testToStringList() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    assertEquals(Arrays.asList(IDWD_11.toString(), IDWD_21.toString()), test.toStringList());
  }

  /**
   * Tests the toStringList() method on an empty bundle.
   */
  @Test
  public void testToStringListEmpty() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates();
    assertEquals(new ArrayList<String>(), test.toStringList());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the compareTo() method on bundles with a different number of entries.
   */
  @Test
  public void testCompareToDifferentSizes() {
    final ExternalIdBundleWithDates a1 = new ExternalIdBundleWithDates();
    final ExternalIdBundleWithDates a2 = new ExternalIdBundleWithDates(IDWD_11);

    assertEquals(a1.compareTo(a1), 0);
    assertTrue(a1.compareTo(a2) < 0);
    assertTrue(a2.compareTo(a1) > 0);
    assertEquals(a2.compareTo(a2), 0);
  }

  /**
   * Tests the compareTo() method on bundles with the same number of entries.
   */
  @Test
  public void testCompareToSameSizes() {
    final ExternalIdBundleWithDates a1 = new ExternalIdBundleWithDates(IDWD_11);
    final ExternalIdBundleWithDates a2 = new ExternalIdBundleWithDates(IDWD_21);

    assertEquals(a1.compareTo(a1), 0);
    assertTrue(a1.compareTo(a2) < 0);
    assertTrue(a2.compareTo(a1) > 0);
    assertEquals(a2.compareTo(a2), 0);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals() method on empty bundles.
   */
  @Test
  public void testEqualsSameEmpty() {
    final ExternalIdBundleWithDates a1 = new ExternalIdBundleWithDates();
    final ExternalIdBundleWithDates a2 = new ExternalIdBundleWithDates();
    assertEquals(a1, a2);
    assertEquals(a2, a1);
  }

  /**
   * Tests the equals() method on bundles with the same data.
   */
  @Test
  public void testEqualsSameNonEmpty() {
    final ExternalIdBundleWithDates a1 = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    final ExternalIdBundleWithDates a2 = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    assertTrue(a1.equals(a1));
    assertEquals(a1, a2);
    assertEquals(a2, a1);
  }

  /**
   * Tests the equals() method on different bundles.
   */
  @Test
  public void testEqualsDifferent() {
    final ExternalIdBundleWithDates a = new ExternalIdBundleWithDates();
    final ExternalIdBundleWithDates b = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    assertNotEquals(a, b);
    assertNotEquals(b, a);
    assertNotEquals("a", b);
    assertNotEquals(null, b);
  }

  /**
   * Tests the hashCode() method.
   */
  @Test
  public void testHashCode() {
    final ExternalIdBundleWithDates a = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    final ExternalIdBundleWithDates b = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);

    assertEquals(a.hashCode(), b.hashCode());
  }

  /**
   * Tests the toString() method for an empty bundle.
   */
  @Test
  public void testToStringEmpty() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates();
    assertEquals("BundleWithDates[]", test.toString());
  }

  /**
   * Tests the toString() method for a non-empty bundle.
   */
  @Test
  public void testToStringNonEmpty() {
    final ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(IDWD_11, IDWD_21);
    assertEquals("BundleWithDates[" + IDWD_11.toString() + ", " + IDWD_21.toString() + "]", test.toString());
  }

}
