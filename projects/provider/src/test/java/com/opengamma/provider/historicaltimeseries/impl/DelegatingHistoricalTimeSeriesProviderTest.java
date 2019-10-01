/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests for {@link DelegatingHistoricalTimeSeriesProvider}.
 */
@Test(groups = TestGroup.UNIT)
public class DelegatingHistoricalTimeSeriesProviderTest {
  private static final String SOURCE_1 = "source1";
  private static final String SOURCE_2 = "source2";
  private static final LocalDateDoubleTimeSeries TS_1 = ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2019, 1, 1), 100);
  private static final LocalDateDoubleTimeSeries TS_2 = ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2019, 1, 1), 200);
  private static final DelegatingHistoricalTimeSeriesProvider PROVIDER;
  static {
    final Map<String, HistoricalTimeSeriesProvider> map = new HashMap<>();
    map.put(SOURCE_1, new TestHistoricalTimeSeriesProvider(SOURCE_1, TS_1));
    map.put(SOURCE_2, new TestHistoricalTimeSeriesProvider(SOURCE_2, TS_2));
    PROVIDER = new DelegatingHistoricalTimeSeriesProvider(map);
  }

  /**
   * Tests that the providers map cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullProviders() {
    new DelegatingHistoricalTimeSeriesProvider(null);
  }

  /**
   * Tests toString.
   */
  public void testToString() {
    if (PROVIDER.toString().contains("source1, ")) {
      assertEquals(PROVIDER.toString(), "DelegatingHistoricalTimeSeriesProvider[source1, source2]");
    } else {
      assertEquals(PROVIDER.toString(), "DelegatingHistoricalTimeSeriesProvider[source2, source1]");
    }
  }

  /**
   * Tests the behaviour when there is no delegate for a data source.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoDelegate() {
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(ExternalIdBundle.of("eid", "1"), "source3",
        "provider", "field");
    PROVIDER.doBulkGet(request);
  }

  /**
   * Tests that the correct delegate is selected.
   */
  public void testDelegation() {
    final ExternalIdBundle eid = ExternalIdBundle.of("eid", "1");
    final HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(eid, SOURCE_1, "provider", "field");
    assertEquals(PROVIDER.doBulkGet(request).getResultMap().get(eid), TS_1);
    request.setDataSource(SOURCE_2);
    assertEquals(PROVIDER.doBulkGet(request).getResultMap().get(eid), TS_2);
  }

  private static class TestHistoricalTimeSeriesProvider implements HistoricalTimeSeriesProvider {
    private final String _dataSource;
    private final LocalDateDoubleTimeSeries _ts;

    TestHistoricalTimeSeriesProvider(final String dataSource, final LocalDateDoubleTimeSeries ts) {
      _dataSource = dataSource;
      _ts = ts;
    }

    @Override
    public LocalDateDoubleTimeSeries getHistoricalTimeSeries(final ExternalIdBundle externalIdBundle, final String dataSource, final String dataProvider,
        final String dataField) {
      if (dataSource.equals(_dataSource)) {
        return _ts;
      }
      throw new UnsupportedOperationException();
    }

    @Override
    public LocalDateDoubleTimeSeries getHistoricalTimeSeries(final ExternalIdBundle externalIdBundle, final String dataSource, final String dataProvider,
        final String dataField, final LocalDateRange dateRange) {
      if (dataSource.equals(_dataSource)) {
        return _ts;
      }
      throw new UnsupportedOperationException();
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle externalIdBundle, final String dataSource, final String dataProvider,
        final String dataField) {
      if (dataSource.equals(_dataSource)) {
        return Pairs.of(_ts.getLatestTime(), _ts.getLatestValue());
      }
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getHistoricalTimeSeries(final Set<ExternalIdBundle> externalIdBundleSet, final String dataSource,
        final String dataProvider, final String dataField, final LocalDateRange dateRange) {
      if (dataSource.equals(_dataSource)) {
        return Collections.singletonMap(externalIdBundleSet.iterator().next(), _ts);
      }
      throw new UnsupportedOperationException();
    }

    @Override
    public HistoricalTimeSeriesProviderGetResult getHistoricalTimeSeries(final HistoricalTimeSeriesProviderGetRequest request) {
      return new HistoricalTimeSeriesProviderGetResult(getHistoricalTimeSeries(request.getExternalIdBundles(), request.getDataSource(),
          request.getDataProvider(), request.getDataField(), request.getDateRange()));
    }

  }
}
