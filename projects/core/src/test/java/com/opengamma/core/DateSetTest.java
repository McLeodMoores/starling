/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Unit tests for {@link DateSet}.
 */
public class DateSetTest {
  private static final List<LocalDate> DATES =
      Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1),
          LocalDate.of(2018, 3, 1), LocalDate.of(2016, 3, 1), LocalDate.of(2017, 3, 1));
  private static final DateSet DS = DateSet.of(new HashSet<>(DATES));

  /**
   * Tests that the set cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSet() {
    DateSet.of(null);
  }

  /**
   * Tests that the dates are sorted.
   */
  @Test
  public void testSorted() {
    final List<LocalDate> sorted = new ArrayList<>(DATES);
    Collections.sort(sorted);
    assertEquals(DS.getDates(), sorted);
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate1() {
    DS.getNextDate(null);
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate2() {
    DS.getNextDate(null, 1);
  }

  /**
   * Tests that the index cannot be negative.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    DS.getNextDate(LocalDate.of(2018, 1, 1), -1);
  }

  /**
   * Tests the getNextDate() method.
   */
  @Test
  public void testGetNextDate() {
    assertNull(DS.getNextDate(LocalDate.of(2019, 4, 1)));
    assertNull(DS.getNextDate(LocalDate.of(2019, 4, 1), 1));
    assertEquals(DS.getNextDate(LocalDate.of(2016, 4, 1)), LocalDate.of(2017, 1, 1));
    assertEquals(DS.getNextDate(LocalDate.of(2016, 4, 1), 1), LocalDate.of(2017, 1, 1));
    assertEquals(DS.getNextDate(LocalDate.of(2016, 4, 1), 3), LocalDate.of(2018, 1, 1));
    assertNull(DS.getNextDate(LocalDate.of(2017, 4, 1), 3));
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate3() {
    DS.getPreviousDate(null);
  }

  /**
   * Tests the getPreviousDate() method.
   */
  @Test
  public void testGetPreviousDate() {
    assertNull(DS.getPreviousDate(LocalDate.of(2015, 4, 1)));
    assertEquals(DS.getPreviousDate(LocalDate.of(2016, 4, 1)), LocalDate.of(2016, 3, 1));
  }
}
