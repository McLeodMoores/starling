/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

import net.sf.ehcache.CacheManager;

/**
 * Test.
 */
@Test(groups = {TestGroup.UNIT, "ehcache"})
public class EHCachingHistoricalTimeSeriesProviderTest {

  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");
  private static final LocalDateDoubleTimeSeries BIG_HTS;
  private static final LocalDateDoubleTimeSeries SMALL_HTS;
  static {
    final LocalDate[] dates1 = {LocalDate.of(2011, 6, 30), LocalDate.of(2011, 7, 1), LocalDate.of(2011, 7, 3)};
    final double[] values1 = {12.34d, 12.45d, 12.79d};
    BIG_HTS = ImmutableLocalDateDoubleTimeSeries.of(dates1, values1);

    final LocalDate[] dates2 = {LocalDate.of(2011, 7, 3)};
    final double[] values2 = {12.79d};
    SMALL_HTS = ImmutableLocalDateDoubleTimeSeries.of(dates2, values2);
  }

  private HistoricalTimeSeriesProvider _underlyingProvider;
  private EHCachingHistoricalTimeSeriesProvider _cachingProvider;
  private CacheManager _cacheManager;

  /**
   * Sets up the cache manager.
   */
  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  /**
   * Tears down the cache manager.
   */
  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  /**
   * Sets up the providers and clears the cache.
   */
  @BeforeMethod
  public void setUp() {
    EHCacheUtils.clear(_cacheManager);
    _underlyingProvider = mock(HistoricalTimeSeriesProvider.class);
    _cachingProvider = new EHCachingHistoricalTimeSeriesProvider(_underlyingProvider, _cacheManager);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting all points for multiple time series.
   */
  public void testGetAll() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(BUNDLE, "S", "P", "F");
    final HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
    result.getResultMap().put(BUNDLE, BIG_HTS);

    when(_underlyingProvider.getHistoricalTimeSeries(request)).thenReturn(result);

    // Fetching same series twice should return same result
    final HistoricalTimeSeriesProviderGetResult test1 = _cachingProvider.getHistoricalTimeSeries(request);
    final HistoricalTimeSeriesProviderGetResult test2 = _cachingProvider.getHistoricalTimeSeries(request);
    assertEquals(test1, test2);
    assertEquals(BIG_HTS, test1.getResultMap().get(BUNDLE));

    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingProvider, times(1)).getHistoricalTimeSeries(request);
  }

  /**
   * Tests getting subsets of multiple time series.
   */
  public void testGetSubset() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(BUNDLE, "S", "P", "F");
    request.setMaxPoints(-1);
    final HistoricalTimeSeriesProviderGetRequest allRequest = HistoricalTimeSeriesProviderGetRequest.createGet(BUNDLE, "S", "P", "F");
    final HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
    result.getResultMap().put(BUNDLE, BIG_HTS);

    when(_underlyingProvider.getHistoricalTimeSeries(allRequest)).thenReturn(result);

    // Fetching same series twice should return same result
    final HistoricalTimeSeriesProviderGetResult test1 = _cachingProvider.getHistoricalTimeSeries(request);
    final HistoricalTimeSeriesProviderGetResult test2 = _cachingProvider.getHistoricalTimeSeries(request);
    assertEquals(test1, test2);
    assertEquals(SMALL_HTS, test1.getResultMap().get(BUNDLE));

    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingProvider, times(1)).getHistoricalTimeSeries(allRequest);
    verify(_underlyingProvider, times(0)).getHistoricalTimeSeries(request);

    // no further underlying hits
    final HistoricalTimeSeriesProviderGetResult test3 = _cachingProvider.getHistoricalTimeSeries(allRequest);
    assertEquals(BIG_HTS, test3.getResultMap().get(BUNDLE));

    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingProvider, times(1)).getHistoricalTimeSeries(allRequest);
    verify(_underlyingProvider, times(0)).getHistoricalTimeSeries(request);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests getting multiple time series when some are not available.
   */
  public void testGetAllNotFound() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(BUNDLE, "S", "P", "F");
    final HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
    result.getResultMap().put(BUNDLE, null);

    when(_underlyingProvider.getHistoricalTimeSeries(request)).thenReturn(result);

    // Fetching same series twice should return same result
    final HistoricalTimeSeriesProviderGetResult test1 = _cachingProvider.getHistoricalTimeSeries(request);
    final HistoricalTimeSeriesProviderGetResult test2 = _cachingProvider.getHistoricalTimeSeries(request);
    assertEquals(test1, test2);
    assertEquals(null, test1.getResultMap().get(BUNDLE));

    // underlying source should only have been called once if cache worked as expected
    verify(_underlyingProvider, times(1)).getHistoricalTimeSeries(request);
  }

}
