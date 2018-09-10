/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday.impl;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link WeekendHolidaySource}.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class WeekendHolidaySourceTest {
  private static final HolidaySource SOURCE = new WeekendHolidaySource();

  /**
   * Tests that the get method is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetUniqueId() {
    SOURCE.get(UniqueId.of("uid", "1"));
  }

  /**
   * Tests that the get method is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetObjectId() {
    SOURCE.get(ObjectId.of("oid", "1"), VersionCorrection.LATEST);
  }

  /**
   * Tests that the get method is not supported.
   */
  @Test
  public void testGetHolidayType() {
    assertTrue(SOURCE.get(HolidayType.CURRENCY, ExternalIdBundle.of("eid", "1")).isEmpty());
  }

  /**
   * Tests that the get method is not supported.
   */
  @Test
  public void testGetCurrency() {
    assertTrue(SOURCE.get(Currency.USD).isEmpty());
  }

  /**
   * Tests whether a date is a holiday.
   */
  @Test
  public void testIsHolidayByDateCurrency() {
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 7), Currency.AUD));
    assertTrue(SOURCE.isHoliday(LocalDate.of(2018, 9, 8), Currency.AUD));
    assertTrue(SOURCE.isHoliday(LocalDate.of(2018, 9, 9), Currency.AUD));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 10), Currency.AUD));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 11), Currency.AUD));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 12), Currency.AUD));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 13), Currency.AUD));
  }

  /**
   * Tests whether a date is a holiday.
   */
  @Test
  public void testIsHolidayByDateTypeId() {
    final ExternalId id = ExternalId.of("eid", "1");
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, id));
    assertTrue(SOURCE.isHoliday(LocalDate.of(2018, 9, 8), HolidayType.CURRENCY, id));
    assertTrue(SOURCE.isHoliday(LocalDate.of(2018, 9, 9), HolidayType.CURRENCY, id));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 10), HolidayType.CURRENCY, id));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 11), HolidayType.CURRENCY, id));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 12), HolidayType.CURRENCY, id));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 13), HolidayType.CURRENCY, id));
  }

  /**
   * Tests whether a date is a holiday.
   */
  @Test
  public void testIsHolidayByDateTypeIdBundle() {
    final ExternalIdBundle ids = ExternalIdBundle.of("eid", "1");
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, ids));
    assertTrue(SOURCE.isHoliday(LocalDate.of(2018, 9, 8), HolidayType.CURRENCY, ids));
    assertTrue(SOURCE.isHoliday(LocalDate.of(2018, 9, 9), HolidayType.CURRENCY, ids));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 10), HolidayType.CURRENCY, ids));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 11), HolidayType.CURRENCY, ids));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 12), HolidayType.CURRENCY, ids));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 13), HolidayType.CURRENCY, ids));
  }

}
