/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.holiday.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.test.Assert;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SchemeAlteringHolidaySource}.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class SchemeAlteringHolidaySourceTest {
  private static final VersionCorrection VERSION = VersionCorrection.ofVersionAsOf(Instant.now());
  private static final SimpleHolidayWithWeekend HOLIDAY =
      new SimpleHolidayWithWeekend(Arrays.asList(LocalDate.of(2018, 9, 9), LocalDate.of(2018, 9, 10)), WeekendType.FRIDAY_SATURDAY);
  private static final UniqueId UID_1 = UniqueId.of("hol", "1", VERSION.toString());
  private static final ObjectId OID_1 = ObjectId.of("hol", "1");
  private static final ExternalIdBundle EIDS_1 = ExternalIdBundle.of("TEST1", "1");
  private static final Currency CCY_1 = Currency.AUD;
  private static final SimpleHoliday NO_WEEKEND = new SimpleHoliday(Arrays.asList(LocalDate.of(2018, 9, 9), LocalDate.of(2018, 9, 11)));
  private static final UniqueId UID_2 = UniqueId.of("hol", "2", VERSION.toString());
  private static final ObjectId OID_2 = ObjectId.of("hol", "2");
  private static final ExternalIdBundle EIDS_2 = ExternalIdBundle.of("test2", "2");
  private static final Currency CCY_2 = Currency.BRL;
  private static final HolidaySource DELEGATE = Mockito.mock(HolidaySource.class);
  static {
    HOLIDAY.setCurrency(CCY_1);
    Mockito.when(DELEGATE.get(HolidayType.CURRENCY, EIDS_1)).thenReturn(Collections.<Holiday>singleton(HOLIDAY));
    Mockito.when(DELEGATE.get(HolidayType.CURRENCY, EIDS_2)).thenReturn(Collections.<Holiday>singleton(NO_WEEKEND));
    Mockito.when(DELEGATE.get(CCY_1)).thenReturn(Collections.<Holiday>singleton(HOLIDAY));
    Mockito.when(DELEGATE.get(CCY_2)).thenReturn(Collections.<Holiday>singleton(NO_WEEKEND));
    Mockito.when(DELEGATE.get(UID_1)).thenReturn(HOLIDAY);
    Mockito.when(DELEGATE.get(UID_2)).thenReturn(NO_WEEKEND);
    Mockito.when(DELEGATE.get(OID_1, VERSION)).thenReturn(HOLIDAY);
    Mockito.when(DELEGATE.get(OID_2, VERSION)).thenReturn(NO_WEEKEND);
    Mockito.when(DELEGATE.isHoliday(LocalDate.of(2018, 9, 7), Currency.AUD)).thenReturn(true);
    Mockito.when(DELEGATE.isHoliday(LocalDate.of(2018, 9, 7), Currency.BRL)).thenReturn(false);
    Mockito.when(DELEGATE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, ExternalId.of("test1", "1"))).thenReturn(true);
    Mockito.when(DELEGATE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, ExternalId.of("test2", "2"))).thenReturn(false);
    Mockito.when(DELEGATE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, ExternalIdBundle.of("test1", "1"))).thenReturn(true);
    Mockito.when(DELEGATE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, ExternalIdBundle.of("test2", "2"))).thenReturn(false);
    final Map<UniqueId, Holiday> mUid = new HashMap<>();
    mUid.put(UID_1, HOLIDAY);
    mUid.put(UID_2, NO_WEEKEND);
    Mockito.when(DELEGATE.get(Arrays.asList(UID_1, UID_2))).thenReturn(mUid);
    final Map<ObjectId, Holiday> mOid = new HashMap<>();
    mOid.put(OID_1, HOLIDAY);
    mOid.put(OID_2, NO_WEEKEND);
    Mockito.when(DELEGATE.get(Arrays.asList(OID_1, OID_2), VERSION)).thenReturn(mOid);
  }
  private static final SchemeAlteringHolidaySource SOURCE = new SchemeAlteringHolidaySource(DELEGATE);
  static {
    SOURCE.addMapping("TEST1", "test1");
  }

  /**
   * Tests that the underlying source cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new SchemeAlteringHolidaySource(null);
  }

  /**
   * Tests that the source scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNullSource() {
    SOURCE.addMapping(null, "test3");
  }

  /**
   * Tests that the target scheme cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddNulTarget() {
    SOURCE.addMapping("test1", null);
  }

  /**
   * Tests getting holidays by type.
   */
  @Test
  public void testGetByType() {
    Collection<Holiday> result = SOURCE.get(HolidayType.CURRENCY, EIDS_1);
    assertEquals(result.size(), 1);
    assertEquals(result.iterator().next(), HOLIDAY);
    result = SOURCE.get(HolidayType.CURRENCY, EIDS_2);
    assertEquals(result.size(), 1);
    assertEquals(result.iterator().next(), NO_WEEKEND);
  }

  /**
   * Tests getting holidays by currency.
   */
  @Test
  public void testGetByCurrency() {
    assertEquals(SOURCE.get(CCY_1), Collections.singleton(HOLIDAY));
    assertEquals(SOURCE.get(CCY_2), Collections.singleton(NO_WEEKEND));
  }

  /**
   * Tests getting holidays by unique id.
   */
  @Test
  public void testGetByUniqueId() {
    assertEquals(SOURCE.get(UID_1), HOLIDAY);
    assertEquals(SOURCE.get(UID_2), NO_WEEKEND);
  }

  /**
   * Tests getting holidays by object id.
   */
  @Test
  public void testGetByObjectId() {
    assertEquals(SOURCE.get(OID_1, VERSION), HOLIDAY);
    assertEquals(SOURCE.get(OID_2, VERSION), NO_WEEKEND);
  }

  /**
   * Tests getting holidays by a collection of unique ids.
   */
  @Test
  public void testGetByUniqueIds() {
    final Map<UniqueId, Holiday> expected = new HashMap<>();
    expected.put(UID_1, HOLIDAY);
    expected.put(UID_2, NO_WEEKEND);
    Assert.assertEqualsNoOrder(SOURCE.get(Arrays.asList(UID_1, UID_2)), expected);
  }

  /**
   * Tests getting holidays by a collection of object ids.
   */
  @Test
  public void testGetByObjectIds() {
    final Map<ObjectId, Holiday> expected = new HashMap<>();
    expected.put(OID_1, HOLIDAY);
    expected.put(OID_2, NO_WEEKEND);
    Assert.assertEqualsNoOrder(SOURCE.get(Arrays.asList(OID_1, OID_2), VERSION), expected);
  }

  /**
   * Tests scheme translation.
   */
  @Test
  public void testTranslateScheme() {
    assertEquals(SOURCE.translateScheme("TEST1"), "test1");
    assertEquals(SOURCE.translateScheme("test2"), "test2");
  }

  /**
   * Tests external id translation.
   */
  @Test
  public void testExternalIdTranslation() {
    assertEquals(SOURCE.translateExternalId(ExternalId.of("TEST1", "1")), ExternalId.of("test1", "1"));
    assertEquals(SOURCE.translateExternalId(ExternalId.of("test2", "1")), ExternalId.of("test2", "1"));
  }

  /**
   * Tests whether a date is a holiday.
   */
  @Test
  public void testIsHolidayByDateCurrency() {
    assertTrue(SOURCE.isHoliday(LocalDate.of(2018, 9, 7), Currency.AUD));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 7), Currency.BRL));
  }

  /**
   * Tests whether a date is a holiday.
   */
  @Test
  public void testIsHolidayByDateTypeId() {
    final ExternalId id1 = EIDS_1.getExternalId(ExternalScheme.of("TEST1"));
    assertTrue(SOURCE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, id1));
    final ExternalId id2 = EIDS_2.getExternalId(ExternalScheme.of("test2"));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, id2));
  }

  /**
   * Tests whether a date is a holiday.
   */
  @Test
  public void testIsHolidayByDateTypeIdBundle() {
    assertTrue(SOURCE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, EIDS_1));
    assertFalse(SOURCE.isHoliday(LocalDate.of(2018, 9, 7), HolidayType.CURRENCY, EIDS_2));
  }

}
