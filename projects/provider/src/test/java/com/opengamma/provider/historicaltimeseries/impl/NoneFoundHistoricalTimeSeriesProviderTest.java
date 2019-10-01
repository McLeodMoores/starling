/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.LocalDateRange;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NoneFoundHistoricalTimeSeriesProviderTest {

  /**
   * Tests getting a single time series.
   */
  @Test
  public void testGetSingle() {
    final NoneFoundHistoricalTimeSeriesProvider test = new NoneFoundHistoricalTimeSeriesProvider();
    assertEquals(null, test.getHistoricalTimeSeries(ExternalIdBundle.of("A", "B"), "FOO", "BAR", "BAZ", LocalDateRange.ALL));
  }

  /**
   * Tests getting multiple time series.
   */
  @Test
  public void testGetBulk() {
    final NoneFoundHistoricalTimeSeriesProvider test = new NoneFoundHistoricalTimeSeriesProvider();
    final HashMap<ExternalIdBundle, LocalDateDoubleTimeSeries> expected = new HashMap<>();
    assertEquals(expected, test.getHistoricalTimeSeries(ImmutableSet.of(ExternalIdBundle.of("A", "B")), "FOO", "BAR", "BAZ", LocalDateRange.ALL));
  }

  /**
   * Tests getting a single time series.
   */
  @Test
  public void testGetRequest() {
    final NoneFoundHistoricalTimeSeriesProvider test = new NoneFoundHistoricalTimeSeriesProvider();
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(ExternalIdBundle.of("A", "B"), "FOO", "BAR", "BAZ",
        LocalDateRange.ALL);
    final HistoricalTimeSeriesProviderGetResult expected = new HistoricalTimeSeriesProviderGetResult();
    assertEquals(expected, test.getHistoricalTimeSeries(request));
  }

}
