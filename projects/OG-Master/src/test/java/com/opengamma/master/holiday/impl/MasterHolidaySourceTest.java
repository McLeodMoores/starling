/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.holiday.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.holiday.ManageableHolidayWithWeekend;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link MasterHolidaySource}.
 */
@Test(groups = TestGroup.UNIT)
public class MasterHolidaySourceTest {
  private static final LocalDate DATE_FRIDAY = LocalDate.of(2010, 10, 22);
  private static final LocalDate DATE_SUNDAY = LocalDate.of(2010, 10, 24);
  private static final Currency GBP = Currency.GBP;
  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final ExternalId ID = ExternalId.of("C", "D");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  @BeforeMethod
  public static void setUp() {
    ThreadLocalServiceContext.init(ServiceContext.of(VersionCorrectionProvider.class, new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    }));
  }

  @AfterMethod
  public static void tearDown() {
    ThreadLocalServiceContext.init(ServiceContext.of(ImmutableMap.<Class<?>, Object>of()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullMaster() throws Exception {
    new MasterHolidaySource(null);
  }

  //-------------------------------------------------------------------------
  public void test_getHoliday_UniqueId_noOverride_found() throws Exception {
    final HolidayMaster mock = mock(HolidayMaster.class);

    final HolidayDocument doc = new HolidayDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    final MasterHolidaySource test = new MasterHolidaySource(mock);
    final Holiday testResult = test.get(UID);
    verify(mock, times(1)).get(UID);

    assertEquals(testResult, example());
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_UniqueId_notFound() throws Exception {
    final HolidayMaster mock = mock(HolidayMaster.class);

    when(mock.get(UID)).thenThrow(new DataNotFoundException(""));
    final MasterHolidaySource test = new MasterHolidaySource(mock);
    try {
      test.get(UID);
    } finally {
      verify(mock, times(1)).get(UID);
    }
  }

  //-------------------------------------------------------------------------
  public void test_getHoliday_ObjectId_found() throws Exception {
    final HolidayMaster mock = mock(HolidayMaster.class);

    final HolidayDocument doc = new HolidayDocument(example());
    when(mock.get(OID, VC)).thenReturn(doc);
    final MasterHolidaySource test = new MasterHolidaySource(mock);
    final Holiday testResult = test.get(OID, VC);
    verify(mock, times(1)).get(OID, VC);

    assertEquals(testResult, example());
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_ObjectId_notFound() throws Exception {
    final HolidayMaster mock = mock(HolidayMaster.class);

    when(mock.get(OID, VC)).thenThrow(new DataNotFoundException(""));
    final MasterHolidaySource test = new MasterHolidaySource(mock);
    try {
      test.get(OID, VC);
    } finally {
      verify(mock, times(1)).get(OID, VC);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the behaviour for holidays and holidays with explicit weekend information when the
   * date is a non-weekend holiday.
   * @throws Exception  if there is a problem
   */
  public void testIsHolidayLocalDateCurrencyHoliday() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    final HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_FRIDAY);
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_FRIDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    boolean testResult = test.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
    // reset the invocation count and test holidays with weekends
    mock = mock(HolidayMaster.class);
    holiday = new ManageableHolidayWithWeekend(holiday, WeekendType.SATURDAY_SUNDAY);
    result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    test = new MasterHolidaySource(mock);
    testResult = test.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
  }

  /**
   * Tests the behaviour for holidays and holidays with explicit weekends when the
   * date is a Friday.
   * @throws Exception  if there is a problem
   */
  public void testIsHolidayLocalDateCurrencyWorkday() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    final HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_FRIDAY);
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.<LocalDate>emptyList());
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    boolean testResult = test.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertFalse(testResult);
    // reset the invocation count and test holidays with weekends
    mock = mock(HolidayMaster.class);
    holiday = new ManageableHolidayWithWeekend(holiday, WeekendType.SATURDAY_SUNDAY);
    result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    test = new MasterHolidaySource(mock);
    testResult = test.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertFalse(testResult);
    // reset the invocation count and test holidays where the weekend days are not the default
    // values previously used in the source
    mock = mock(HolidayMaster.class);
    holiday = new ManageableHolidayWithWeekend(holiday, WeekendType.THURSDAY_FRIDAY);
    result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    test = new MasterHolidaySource(mock);
    testResult = test.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
  }

  /**
   * Tests the behaviour for holidays and holidays with explicit weekends when the date
   * is a Sunday.
   * @throws Exception  if there is a problem
   */
  public void testIsHolidayLocalDateCurrencySunday() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_SUNDAY);
    request.setVersionCorrection(VC);
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.<LocalDate>emptyList());
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    boolean testResult = test.isHoliday(DATE_SUNDAY, GBP);
    // new request with latest version correction is created, so expect no invocations with the request object
    verify(mock, times(0)).search(request);
    assertTrue(testResult);
    mock = mock(HolidayMaster.class);

    // reset the invocation count and test holidays with no version correction
    mock = mock(HolidayMaster.class);
    request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_SUNDAY);
    holiday = new ManageableHolidayWithWeekend(holiday, WeekendType.SATURDAY_SUNDAY);
    result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    test = new MasterHolidaySource(mock);
    // one search is made as the code doesn't immediately short-circuit Saturday and Sunday
    testResult = test.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);

    // reset the invocation count and test holidays with weekends and a version correction
    mock = mock(HolidayMaster.class);
    request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_SUNDAY);
    request.setVersionCorrection(VC);
    holiday = new ManageableHolidayWithWeekend(holiday, WeekendType.SATURDAY_SUNDAY);
    result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    test = new MasterHolidaySource(mock);
    // version correction is ignored when using this method, so the search will return null and the code falls
    // through to the previous Saturday / Sunday behaviour
    testResult = test.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(0)).search(request);
    assertTrue(testResult);

    // reset the invocation count and test holidays with weekends and no version correction
    mock = mock(HolidayMaster.class);
    request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_SUNDAY);
    holiday = new ManageableHolidayWithWeekend(holiday, WeekendType.SATURDAY_SUNDAY);
    result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    test = new MasterHolidaySource(mock);
    testResult = test.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);

    // reset the invocation count and test holidays with with Friday and Saturday as the weekend days and a
    // version correction
    mock = mock(HolidayMaster.class);
    request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_SUNDAY);
    request.setVersionCorrection(VC);
    holiday = new ManageableHolidayWithWeekend(holiday, WeekendType.FRIDAY_SATURDAY);
    result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    test = new MasterHolidaySource(mock);
    // version correction is ignored when using this method, so the search will return null and the code falls
    // through to the previous Saturday / Sunday behaviour
    testResult = test.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(0)).search(request);
    assertTrue(testResult);

    // reset the invocation count and test holidays with with Friday and Saturday as the weekend days
    mock = mock(HolidayMaster.class);
    request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_SUNDAY);
    holiday = new ManageableHolidayWithWeekend(holiday, WeekendType.FRIDAY_SATURDAY);
    result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    test = new MasterHolidaySource(mock);
    testResult = test.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(1)).search(request);
    assertFalse(testResult);
}

  //-------------------------------------------------------------------------
  public void test_isHoliday_LocalDateTypeExternalId_holiday() throws Exception {
    final HolidayMaster mock = mock(HolidayMaster.class);
    final HolidaySearchRequest request = new HolidaySearchRequest(HolidayType.BANK, ExternalIdBundle.of(ID));
    request.setDateToCheck(DATE_FRIDAY);
    final ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_FRIDAY));
    final HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));

    when(mock.search(request)).thenReturn(result);
    final MasterHolidaySource test = new MasterHolidaySource(mock);
    final boolean testResult = test.isHoliday(DATE_FRIDAY, HolidayType.BANK, ID);
    verify(mock, times(1)).search(request);

    assertTrue(testResult);
  }

  //-------------------------------------------------------------------------
  public void test_isHoliday_LocalDateTypeExternalIdBundle_holiday() throws Exception {
    final HolidayMaster mock = mock(HolidayMaster.class);
    final HolidaySearchRequest request = new HolidaySearchRequest(HolidayType.BANK, BUNDLE);
    request.setDateToCheck(DATE_FRIDAY);
    final ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_FRIDAY));
    final HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));

    when(mock.search(request)).thenReturn(result);
    final MasterHolidaySource test = new MasterHolidaySource(mock);
    final boolean testResult = test.isHoliday(DATE_FRIDAY, HolidayType.BANK, BUNDLE);
    verify(mock, times(1)).search(request);

    assertTrue(testResult);
  }

  //-------------------------------------------------------------------------
  protected Holiday example() {
    return new ManageableHoliday(GBP, Collections.singletonList(DATE_FRIDAY));
  }

  /**
   * Tests that an empty list of dates is cached if no holiday is found and that Saturday and
   * Sunday are counted as holidays in all cases.
   * @throws Exception  if there is a problem
   */
  public void testCachingNoHolidaysFound() throws Exception {
    final HolidayMaster mock = mock(HolidayMaster.class);
    final HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    final MasterHolidaySource cachingSource = new MasterHolidaySource(mock, true);
    boolean testResult = cachingSource.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertFalse(testResult);
    testResult = cachingSource.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertFalse(testResult);
  }

  /**
   * Tests holiday date caching.
   * @throws Exception  if there is a problem
   */
  public void testCachedHolidays() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_FRIDAY);
    final ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_FRIDAY));
    final HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    final MasterHolidaySource nonCachingSource = new MasterHolidaySource(mock, false);
    boolean testResult = nonCachingSource.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
    testResult = nonCachingSource.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(2)).search(request);
    assertTrue(testResult);
    // reset invocation count and use caching
    mock = mock(HolidayMaster.class);
    // the date to check is not set in the cache key
    request = new HolidaySearchRequest(GBP);
    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource cachingSource = new MasterHolidaySource(mock, true);
    testResult = cachingSource.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
    testResult = cachingSource.isHoliday(DATE_FRIDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
    // reset invocation count and check previous behaviour
    mock = mock(HolidayMaster.class);
    // the date to check is not set in the cache key
    request = new HolidaySearchRequest(GBP);
    when(mock.search(request)).thenReturn(result);
    cachingSource = new MasterHolidaySource(mock, true);
    testResult = cachingSource.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
    testResult = cachingSource.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
  }

  /**
   * Tests that the caching considers explicit weekends if that is the holiday type.
   * @throws Exception  if there is a problem
   */
  public void testCachingHolidaysWithWeekend() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    ManageableHoliday holiday =
        new ManageableHolidayWithWeekend(new ManageableHoliday(GBP, Collections.<LocalDate>emptyList()), WeekendType.SATURDAY_SUNDAY);
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource cachingSource = new MasterHolidaySource(mock, true);
    boolean testResult = cachingSource.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
    testResult = cachingSource.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(1)).search(request);
    assertTrue(testResult);
    // reset invocation count
    mock = mock(HolidayMaster.class);
    request = new HolidaySearchRequest(GBP);
    holiday = new ManageableHolidayWithWeekend(new ManageableHoliday(GBP, Collections.<LocalDate>emptyList()), WeekendType.FRIDAY_SATURDAY);
    result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));
    when(mock.search(request)).thenReturn(result);
    cachingSource = new MasterHolidaySource(mock, true);
    testResult = cachingSource.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(1)).search(request);
    assertFalse(testResult);
    testResult = cachingSource.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(1)).search(request);
    assertFalse(testResult);
  }
}
