/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link WeekendType}.
 */
@Test(groups = TestGroup.UNIT)
public class WeekendTypeTest {

  /**
   * Tests that no days are holidays.
   */
  @Test
  public void testNone() {
    assertFalse(WeekendType.NONE.isWeekend(LocalDate.of(2018, 9, 6)));
    assertFalse(WeekendType.NONE.isWeekend(LocalDate.of(2018, 9, 7)));
    assertFalse(WeekendType.NONE.isWeekend(LocalDate.of(2018, 9, 8)));
    assertFalse(WeekendType.NONE.isWeekend(LocalDate.of(2018, 9, 9)));
    assertFalse(WeekendType.NONE.isWeekend(LocalDate.of(2018, 9, 10)));
    assertFalse(WeekendType.NONE.isWeekend(LocalDate.of(2018, 9, 11)));
    assertFalse(WeekendType.NONE.isWeekend(LocalDate.of(2018, 9, 12)));
  }

  /**
   * Tests that Thursday and Friday are holidays.
   */
  @Test
  public void testThursdayFriday() {
    assertTrue(WeekendType.THURSDAY_FRIDAY.isWeekend(LocalDate.of(2018, 9, 6)));
    assertTrue(WeekendType.THURSDAY_FRIDAY.isWeekend(LocalDate.of(2018, 9, 7)));
    assertFalse(WeekendType.THURSDAY_FRIDAY.isWeekend(LocalDate.of(2018, 9, 8)));
    assertFalse(WeekendType.THURSDAY_FRIDAY.isWeekend(LocalDate.of(2018, 9, 9)));
    assertFalse(WeekendType.THURSDAY_FRIDAY.isWeekend(LocalDate.of(2018, 9, 10)));
    assertFalse(WeekendType.THURSDAY_FRIDAY.isWeekend(LocalDate.of(2018, 9, 11)));
    assertFalse(WeekendType.THURSDAY_FRIDAY.isWeekend(LocalDate.of(2018, 9, 12)));
  }

  /**
   * Tests that Friday and Saturday are holidays.
   */
  @Test
  public void testFridaySaturday() {
    assertFalse(WeekendType.FRIDAY_SATURDAY.isWeekend(LocalDate.of(2018, 9, 6)));
    assertTrue(WeekendType.FRIDAY_SATURDAY.isWeekend(LocalDate.of(2018, 9, 7)));
    assertTrue(WeekendType.FRIDAY_SATURDAY.isWeekend(LocalDate.of(2018, 9, 8)));
    assertFalse(WeekendType.FRIDAY_SATURDAY.isWeekend(LocalDate.of(2018, 9, 9)));
    assertFalse(WeekendType.FRIDAY_SATURDAY.isWeekend(LocalDate.of(2018, 9, 10)));
    assertFalse(WeekendType.FRIDAY_SATURDAY.isWeekend(LocalDate.of(2018, 9, 11)));
    assertFalse(WeekendType.FRIDAY_SATURDAY.isWeekend(LocalDate.of(2018, 9, 12)));
  }

  /**
   * Tests that Saturday and Sunday are holidays.
   */
  @Test
  public void testSaturdaySunday() {
    assertFalse(WeekendType.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2018, 9, 6)));
    assertFalse(WeekendType.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2018, 9, 7)));
    assertTrue(WeekendType.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2018, 9, 8)));
    assertTrue(WeekendType.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2018, 9, 9)));
    assertFalse(WeekendType.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2018, 9, 10)));
    assertFalse(WeekendType.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2018, 9, 11)));
    assertFalse(WeekendType.SATURDAY_SUNDAY.isWeekend(LocalDate.of(2018, 9, 12)));
  }

  /**
   * Tests that the date cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    WeekendType.FRIDAY_SATURDAY.isWeekend(null);
  }
}
