/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertSame;

import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.holiday.ChangeHolidaySource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

/**
 * Test {@link EHCachingHolidaySource} with an underlying {@link ChangeHolidaySource}.
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class EHCachingChangeHolidaySourceTest {
  private static final UniqueId UID = UniqueId.of("A", "B", "123");
  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final ExternalId EID_1 = ExternalSchemes.financialRegionId("US");
  private static final ExternalId EID_2 = ExternalSchemes.financialRegionId("AU");
  private static final VersionCorrection VC = VersionCorrection.of(Instant.now(), Instant.now());
  private static final SimpleHolidayWithWeekend HOLIDAY_1 =
      new SimpleHolidayWithWeekend(Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1)), WeekendType.FRIDAY_SATURDAY);
  private static final SimpleHolidayWithWeekend HOLIDAY_2 =
      new SimpleHolidayWithWeekend(Arrays.asList(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 1, 1)), WeekendType.FRIDAY_SATURDAY);
  static {
    HOLIDAY_1.setUniqueId(UID);
    HOLIDAY_1.setRegionExternalId(EID_1);
    HOLIDAY_2.setUniqueId(UniqueId.of("A", "C", "23"));
    HOLIDAY_2.setCurrency(Currency.AUD);
  }
  private ChangeHolidaySource _underlyingSource;
  private EHCachingHolidaySource _cachingSource;
  private CacheManager _cacheManager;

  /**
   * Sets up the cache managers.
   */
  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCachingChangeHolidaySourceTest.class);
  }

  /**
   * Shuts down the caches.
   */
  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  /**
   * Initialises the underlying source.
   */
  @SuppressWarnings({ "unchecked", "deprecation" })
  @BeforeMethod
  public void setUp() {
    _underlyingSource = mock(ChangeHolidaySource.class);
    when(_underlyingSource.changeManager()).thenReturn(new BasicChangeManager());
    _cachingSource = new EHCachingHolidaySource(_underlyingSource, _cacheManager);
    when(_underlyingSource.get(UID)).thenReturn(HOLIDAY_1);
    when(_underlyingSource.get(OID, VC)).thenReturn(HOLIDAY_1);
    when(_underlyingSource.get(HolidayType.BANK, EID_1.toBundle())).thenReturn(Collections.<Holiday>singleton(HOLIDAY_1));
    when(_underlyingSource.get(HolidayType.BANK, EID_2.toBundle())).thenThrow(DataNotFoundException.class);
    when(_underlyingSource.get(Currency.USD)).thenThrow(DataNotFoundException.class);
    when(_underlyingSource.get(Currency.AUD)).thenReturn(Collections.<Holiday>singleton(HOLIDAY_2));
    when(_underlyingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD)).thenReturn(true);
    when(_underlyingSource.isHoliday(LocalDate.of(2018, 1, 1), HolidayType.CURRENCY, EID_1.toBundle())).thenReturn(true);
    when(_underlyingSource.isHoliday(LocalDate.of(2018, 1, 1), HolidayType.CURRENCY, EID_1)).thenReturn(true);
  }

  /**
   * Removes the caches.
   */
  @AfterMethod
  public void tearDown() {
    _cachingSource.shutdown();
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting a holiday by unique id.
   */
  @Test
  public void testGetHolidayUniqueId() {
    for (int i = 0; i < 100; i++) {
      assertSame(_cachingSource.get(UID), HOLIDAY_1);
    }
    verify(_underlyingSource, times(1)).get(UID);
  }

  /**
   * Tests getting a holiday by object id.
   */
  @Test
  public void testGetHolidayObjectId() {
    assertSame(_cachingSource.get(OID, VC), HOLIDAY_1);
    for (int i = 0; i < 100; i++) {
      assertSame(_cachingSource.get(OID, VC), HOLIDAY_1);
    }
    verify(_underlyingSource, times(1)).get(OID, VC);
  }

  /**
   * Tests getting holidays by type and id bundle.
   */
  @Test
  public void testGetHolidayTypeIdBundle() {
    assertEquals(_cachingSource.get(HolidayType.BANK, EID_1.toBundle()), Collections.singleton(HOLIDAY_1));
    for (int i = 0; i < 100; i++) {
      assertEquals(_cachingSource.get(HolidayType.BANK, EID_1.toBundle()), Collections.singleton(HOLIDAY_1));
    }
    verify(_underlyingSource, times(1)).get(HolidayType.BANK, EID_1.toBundle());
  }

  /**
   * Tests getting holidays by type and id bundle.
   */
  @Test
  public void testFailureGetHolidayTypeIdBundle() {
    for (int i = 0; i < 100; i++) {
      try {
        _cachingSource.get(HolidayType.BANK, EID_2.toBundle());
      } catch (final DataNotFoundException e) {
        // expected
      }
    }
    verify(_underlyingSource, times(1)).get(HolidayType.BANK, EID_2.toBundle());
  }

  /**
   * Tests getting holidays by type and id bundle.
   */
  @Test
  public void testFailureGetCurrency() {
    for (int i = 0; i < 100; i++) {
      try {
        _cachingSource.get(Currency.USD);
      } catch (final DataNotFoundException e) {
        // expected
      }
    }
    verify(_underlyingSource, times(1)).get(Currency.USD);
  }

  /**
   * Tests getting holidays by type and id bundle.
   */
  @Test
  public void testGetHolidayCurrency() {
    for (int i = 0; i < 100; i++) {
      assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    }
    verify(_underlyingSource, times(1)).get(Currency.AUD);
  }

  /**
   * Tests caching a holiday date.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsHolidayByCurrency() {
    for (int i = 0; i < 100; i++) {
      assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
      assertFalse(_cachingSource.isHoliday(LocalDate.of(2017, 1, 1), Currency.USD));
    }
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2018, 1, 1), Currency.USD);
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2017, 1, 1), Currency.USD);
  }

  /**
   * Tests caching a holiday date.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsHolidayByTypeId() {
    for (int i = 0; i < 100; i++) {
      assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), HolidayType.CURRENCY, EID_1));
      assertFalse(_cachingSource.isHoliday(LocalDate.of(2017, 1, 1), HolidayType.CURRENCY, EID_1));
    }
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2018, 1, 1), HolidayType.CURRENCY, EID_1);
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2017, 1, 1), HolidayType.CURRENCY, EID_1);
  }

  /**
   * Tests caching a holiday date.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsHolidayByTypeIds() {
    for (int i = 0; i < 100; i++) {
      assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), HolidayType.CURRENCY, EID_1.toBundle()));
      assertFalse(_cachingSource.isHoliday(LocalDate.of(2017, 1, 1), HolidayType.CURRENCY, EID_1.toBundle()));
    }
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2018, 1, 1), HolidayType.CURRENCY, EID_1.toBundle());
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2017, 1, 1), HolidayType.CURRENCY, EID_1.toBundle());
  }

  /**
   * Tests that the cache is flushed after a change event.
   */
  @Test
  public void testChangeChangeEvent() {
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    // values were cached
    verify(_underlyingSource, times(1)).get(OID, VC);
    verify(_underlyingSource, times(1)).get(Currency.AUD);

    // change a value
    _underlyingSource.changeManager().entityChanged(ChangeType.CHANGED, OID,
        VC.getVersionAsOf().plusSeconds(100), VC.getCorrectedTo().plusSeconds(200), VC.getVersionAsOf().plusSeconds(1000));
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    // cache was emptied and then refilled
    verify(_underlyingSource, times(2)).get(OID, VC);
    verify(_underlyingSource, times(2)).get(Currency.AUD);
  }

  /**
   * Tests that the cache is unchanged after an add event.
   */
  @Test
  public void testAddChangeEvent() {
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    // values were cached
    verify(_underlyingSource, times(1)).get(OID, VC);
    verify(_underlyingSource, times(1)).get(Currency.AUD);

    // add a value
    _underlyingSource.changeManager().entityChanged(ChangeType.ADDED, OID,
        VC.getVersionAsOf().plusSeconds(100), VC.getCorrectedTo().plusSeconds(200), VC.getVersionAsOf().plusSeconds(1000));
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    // cache unaffected
    verify(_underlyingSource, times(1)).get(OID, VC);
    verify(_underlyingSource, times(1)).get(Currency.AUD);
  }

  /**
   * Tests that the cache is flushed after a remove event.
   */
  @Test
  public void testRemoveChangeEvent() {
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    // values were cached
    verify(_underlyingSource, times(1)).get(OID, VC);
    verify(_underlyingSource, times(1)).get(Currency.AUD);

    // remove a value
    _underlyingSource.changeManager().entityChanged(ChangeType.REMOVED, OID,
        VC.getVersionAsOf().plusSeconds(100), VC.getCorrectedTo().plusSeconds(200), VC.getVersionAsOf().plusSeconds(1000));
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    assertEquals(_cachingSource.get(OID, VC), HOLIDAY_1);
    assertEquals(_cachingSource.get(Currency.AUD), Collections.singleton(HOLIDAY_2));
    // cache was emptied and then refilled
    verify(_underlyingSource, times(2)).get(OID, VC);
    verify(_underlyingSource, times(2)).get(Currency.AUD);
  }

  /**
   * Tests that the cache is flushed after a change event.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsHolidayChangeChangeEvent() {
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    // values were cached
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2018, 1, 1), Currency.USD);

    // change a value
    _underlyingSource.changeManager().entityChanged(ChangeType.CHANGED, OID,
        VC.getVersionAsOf().plusSeconds(100), VC.getCorrectedTo().plusSeconds(200), VC.getVersionAsOf().plusSeconds(1000));
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    // values were cached
    verify(_underlyingSource, times(2)).isHoliday(LocalDate.of(2018, 1, 1), Currency.USD);
  }

  /**
   * Tests that the cache is unchanged after an add event.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsHolidayAddChangeEvent() {
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    // values were cached
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2018, 1, 1), Currency.USD);

    // add a value
    _underlyingSource.changeManager().entityChanged(ChangeType.ADDED, OID,
        VC.getVersionAsOf().plusSeconds(100), VC.getCorrectedTo().plusSeconds(200), VC.getVersionAsOf().plusSeconds(1000));
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    // values were cached
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2018, 1, 1), Currency.USD);
  }

  /**
   * Tests that the cache is flushed after a remove event.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsHolidayRemoveChangeEvent() {
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    // values were cached
    verify(_underlyingSource, times(1)).isHoliday(LocalDate.of(2018, 1, 1), Currency.USD);

    // remove a value
    _underlyingSource.changeManager().entityChanged(ChangeType.REMOVED, OID,
        VC.getVersionAsOf().plusSeconds(100), VC.getCorrectedTo().plusSeconds(200), VC.getVersionAsOf().plusSeconds(1000));
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    assertTrue(_cachingSource.isHoliday(LocalDate.of(2018, 1, 1), Currency.USD));
    // values were cached
    verify(_underlyingSource, times(2)).isHoliday(LocalDate.of(2018, 1, 1), Currency.USD);
  }
}
