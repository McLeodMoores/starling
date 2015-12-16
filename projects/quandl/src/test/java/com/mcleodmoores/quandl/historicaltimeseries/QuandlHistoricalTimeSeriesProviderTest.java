/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.historicaltimeseries;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.time.LocalDateRange;

/**
 * Unit tests for {@link QuandlHistoricalTimeSeriesProvider}.
 */
@Test//(groups = TestGroup.UNIT)
public class QuandlHistoricalTimeSeriesProviderTest {
  /** The data source */
  private static final String DATA_SOURCE = "DEFAULT";
  /** The data provider */
  private static final String DATA_PROVIDER = "DEFAULT";

  /**
   * Tests the provider when the date range is set.
   */
  @Test
  public void testProviderSetDateRange() {
    final QuandlHistoricalTimeSeriesProvider provider = new QuandlHistoricalTimeSeriesProvider("U4c8PuHYsa61ECEorSGC");
    provider.start();
    final ExternalIdBundle id = QuandlConstants.ofCode("FRED/DSWP10").toBundle();
    // start date is before start of time series and end is after
    HistoricalTimeSeriesProviderGetRequest request =
        HistoricalTimeSeriesProviderGetRequest.createGet(
            id, DATA_SOURCE, DATA_PROVIDER, QuandlConstants.VALUE_FIELD_NAME, LocalDateRange.of(LocalDate.of(1900, 1, 1), LocalDate.of(2015, 11, 14), false));
    HistoricalTimeSeriesProviderGetResult result = provider.getHistoricalTimeSeries(request);
    assertEquals(result.getResultMap().size(), 1);
    LocalDateDoubleTimeSeries ts = result.getResultMap().get(id);
    assertEquals(ts.getEarliestTime(), LocalDate.of(2000, 7, 3)); // first date available
    // start date and end dates have data available
    request = HistoricalTimeSeriesProviderGetRequest.createGet(
        id, DATA_SOURCE, DATA_PROVIDER, QuandlConstants.VALUE_FIELD_NAME, LocalDateRange.of(LocalDate.of(2010, 1, 4), LocalDate.of(2014, 11, 14), false));
    result = provider.getHistoricalTimeSeries(request);
    assertEquals(result.getResultMap().size(), 1);
    ts = result.getResultMap().get(id);
    assertEquals(ts.getEarliestTime(), LocalDate.of(2010, 1, 4));
    assertEquals(ts.getLatestTime(), LocalDate.of(2014, 11, 14));
  }

  /**
   * Tests the provider when the date range is not set.
   */
  @Test
  public void testProviderAllDateRange() {
    final QuandlHistoricalTimeSeriesProvider provider = new QuandlHistoricalTimeSeriesProvider("U4c8PuHYsa61ECEorSGC");
    provider.start();
    final ExternalIdBundle id = QuandlConstants.ofCode("FRED/DSWP10").toBundle();
    final HistoricalTimeSeriesProviderGetRequest request =
        HistoricalTimeSeriesProviderGetRequest.createGet(id, DATA_SOURCE, DATA_PROVIDER, QuandlConstants.VALUE_FIELD_NAME, LocalDateRange.ALL);
    final HistoricalTimeSeriesProviderGetResult result = provider.getHistoricalTimeSeries(request);
    assertEquals(result.getResultMap().size(), 1);
    final LocalDateDoubleTimeSeries ts = result.getResultMap().get(id);
    assertEquals(ts.getEarliestTime(), LocalDate.of(2000, 7, 3)); // first date available
  }
}
