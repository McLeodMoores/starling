/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
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
 * Tests for {@link MarketDataProviderHistoricalTimeSeriesSource}.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataProviderHistoricalTimeSeriesSourceTest {
  private static final String SCHEME_1 = "hts1";
  private static final String SCHEME_2 = "hts2";
  private static final LocalDateDoubleTimeSeries TS_1 = ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2019, 1, 1), 100);
  private static final LocalDateDoubleTimeSeries TS_2 = ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2019, 1, 1), 200);
  private static final HistoricalTimeSeriesProvider PROVIDER;
  static {
    final Map<String, LocalDateDoubleTimeSeries> map = new HashMap<>();
    map.put(SCHEME_1, TS_1);
    map.put(SCHEME_2, TS_2);
    PROVIDER = new TestHistoricalTimeSeriesProvider(map);
  }
  private static final String PROVIDER_NAME = "providerName";
  private static final UniqueIdSupplier UID_SUPPLIER = new UniqueIdSupplier("hts");
  private static final MarketDataProviderHistoricalTimeSeriesSource SOURCE = new NoImpl(PROVIDER_NAME, UID_SUPPLIER, PROVIDER);

  /**
   * Tests that the provider name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProviderName() {
    new NoImpl(null, UID_SUPPLIER, PROVIDER);
  }

  /**
   * Tests that the UID supplier cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUidSupplier() {
    new NoImpl(PROVIDER_NAME, null, PROVIDER);
  }

  /**
   * Tests that the provider cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProvider() {
    new NoImpl(PROVIDER_NAME, UID_SUPPLIER, null);
  }

  /**
   * Tests that change management is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testChangeManagement() {
    SOURCE.changeManager();
  }

  /**
   * Tests that getting by unique id is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetHtsByUid() {
    SOURCE.getHistoricalTimeSeries(UniqueId.of("hts", "1"));
  }

  /**
   * Tests that getting by unique id is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetHtsByUidDates() {
    SOURCE.getHistoricalTimeSeries(UniqueId.of("hts", "1"), LocalDate.of(2018, 1, 1), true, LocalDate.of(2018, 12, 1), true);
  }

  /**
   * Tests that getting by unique id is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetHtsByUidDatesMaxPoints() {
    SOURCE.getHistoricalTimeSeries(UniqueId.of("hts", "1"), LocalDate.of(2018, 1, 1), true, LocalDate.of(2018, 12, 1), true, 10);
  }

  /**
   * Tests that getting by unique id is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetLatestByUid() {
    SOURCE.getLatestDataPoint(UniqueId.of("hts", "1"));
  }

  /**
   * Tests that getting by unique id is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLatestByUidDates() {
    SOURCE.getLatestDataPoint(UniqueId.of("hts", "1"), LocalDate.of(2018, 1, 1), true, LocalDate.of(2018, 12, 1), true);
  }

  /**
   * Tests getting by external id bundle.
   */
  public void testGetByExternalIdBundle() {
    final ExternalIdBundle eid = ExternalIdBundle.of("hts1", "1");
    assertEquals(SOURCE.getHistoricalTimeSeries(eid, "source", "provider", "field").getTimeSeries(), TS_1);
    assertEquals(SOURCE.getHistoricalTimeSeries(eid, "source", "provider", "field", LocalDate.MIN, true, LocalDate.MAX, true).getTimeSeries(), TS_1);
    assertEquals(SOURCE.getHistoricalTimeSeries(eid, "source", "provider", "field", LocalDate.MIN, false, LocalDate.MAX, true, 10).getTimeSeries(), TS_1);
  }

  /**
   * Tests that getting by external identifiers and validity date is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetByExternalIdBundleValidityDate() {
    SOURCE.getHistoricalTimeSeries(ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "source", "provider", "field");
  }

  /**
   * Tests that getting by external identifiers and validity date is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetByExternalIdBundleValidityDateDates() {
    SOURCE.getHistoricalTimeSeries(ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "source", "provider", "field", LocalDate.of(2018, 1, 1), true,
        LocalDate.of(2019, 1, 1), false);
  }

  /**
   * Tests that getting by external identifiers and validity date is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetByExternalIdBundleValidityDateDatesMaxPoints() {
    SOURCE.getHistoricalTimeSeries(ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "source", "provider", "field", LocalDate.of(2018, 1, 1), true,
        LocalDate.of(2019, 1, 1), false, 14);
  }

  /**
   * Tests that getting by external identifiers and validity date is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetLatestByExternalIdBundleValidityDate() {
    SOURCE.getLatestDataPoint(ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "source", "provider", "field");
  }

  /**
   * Tests that getting by external identifiers and validity date is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetLatestByExternalIdBundleValidityDateDates() {
    SOURCE.getLatestDataPoint(ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "source", "provider", "field", LocalDate.of(2018, 1, 1), true,
        LocalDate.of(2019, 1, 1), false);
  }

  /**
   * Tests that getting by external identifiers is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetLatestByExternalIdBundle() {
    SOURCE.getLatestDataPoint(ExternalIdBundle.of("hts1", "1"), "source", "provider", "field");
  }

  /**
   * Tests that getting by external identifiers is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetLatestByExternalIdBundleDates() {
    SOURCE.getLatestDataPoint(ExternalIdBundle.of("hts1", "1"), "source", "provider", "field", LocalDate.of(2018, 1, 1), true, LocalDate.of(2019, 1, 1), false);
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetByExternalIdBundleResolutionKey() {
    SOURCE.getHistoricalTimeSeries("field", ExternalIdBundle.of("hts1", "1"), "resolutionKey");
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetByExternalIdBundleResolutionKeyDates() {
    SOURCE.getHistoricalTimeSeries("field", ExternalIdBundle.of("hts1", "1"), "resolutionKey", LocalDate.of(2018, 1, 1), true, LocalDate.of(2019, 1, 1), false);
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetByExternalIdBundleResolutionKeyValidityDate() {
    SOURCE.getHistoricalTimeSeries("field", ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "resolutionKey");
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetByExternalIdBundleResolutionKeyDatesMaxPoints() {
    SOURCE.getHistoricalTimeSeries("field", ExternalIdBundle.of("hts1", "1"), "resolutionKey", LocalDate.of(2018, 1, 1), true, LocalDate.of(2019, 1, 1), false,
        6);
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetByExternalIdBundleResolutionKeyValidityDateDatesMaxPoints() {
    SOURCE.getHistoricalTimeSeries("field", ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "resolutionKey", LocalDate.of(2018, 1, 1), true,
        LocalDate.of(2019, 1, 1), false, 8);
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetByExternalIdBundleResolutionKeyValidityDateDates() {
    SOURCE.getHistoricalTimeSeries("field", ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "resolutionKey", LocalDate.of(2018, 1, 1), true,
        LocalDate.of(2019, 1, 1), false);
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetLatestByExternalIdBundleResolutionKey() {
    SOURCE.getLatestDataPoint("field", ExternalIdBundle.of("hts1", "1"), "resolutionKey");
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetLatestByExternalIdBundleResolutionKeyDates() {
    SOURCE.getLatestDataPoint("field", ExternalIdBundle.of("hts1", "1"), "resolutionKey", LocalDate.of(2018, 1, 1), true, LocalDate.of(2019, 1, 1), false);
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetLatestByExternalIdBundleResolutionKeyValidityDate() {
    SOURCE.getLatestDataPoint("field", ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "resolutionKey");
  }

  /**
   * Tests getting by external identifiers and resolution keys is not supported.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetLatestByExternalIdBundleResolutionKeyValidityDateDates() {
    SOURCE.getLatestDataPoint("field", ExternalIdBundle.of("hts1", "1"), LocalDate.now(), "resolutionKey", LocalDate.of(2018, 1, 1), true,
        LocalDate.of(2019, 1, 1), false);
  }

  /**
   * Tests that an id bundle cannot be created from a unique id.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetExternalIdBundle() {
    SOURCE.getExternalIdBundle(UniqueId.of("hts", "1"));
  }

  /**
   * Tests getting by external id bundle.
   */
  public void testGetByExternalIdBundleSet() {
    final ExternalIdBundle eid1 = ExternalIdBundle.of("hts1", "1");
    final ExternalIdBundle eid2 = ExternalIdBundle.of("hts2", "2");
    final Set<ExternalIdBundle> eids = new HashSet<>(Arrays.asList(eid1, eid2));
    Map<ExternalIdBundle, HistoricalTimeSeries> result = SOURCE.getHistoricalTimeSeries(eids, "source", "provider", "field", null, false, null, false);
    assertEquals(result.size(), 2);
    assertEquals(result.get(eid1).getTimeSeries(), TS_1);
    assertEquals(result.get(eid2).getTimeSeries(), TS_2);
    result = SOURCE.getHistoricalTimeSeries(eids, "source", "provider", "field", LocalDate.now(), true, null, false);
    assertEquals(result.size(), 2);
    assertEquals(result.get(eid1).getTimeSeries(), TS_1);
    assertEquals(result.get(eid2).getTimeSeries(), TS_2);
    result = SOURCE.getHistoricalTimeSeries(eids, "source", "provider", "field", LocalDate.of(2018, 1, 1), false, LocalDate.of(2019, 1, 1), false);
    assertEquals(result.size(), 2);
    assertEquals(result.get(eid1).getTimeSeries(), TS_1);
    assertEquals(result.get(eid2).getTimeSeries(), TS_2);
  }

  private static class NoImpl extends MarketDataProviderHistoricalTimeSeriesSource {

    NoImpl(final String providerName, final UniqueIdSupplier uniqueIdSupplier,
        final HistoricalTimeSeriesProvider provider) {
      super(providerName, uniqueIdSupplier, provider);
    }
  }

  private static class TestHistoricalTimeSeriesProvider implements HistoricalTimeSeriesProvider {
    private final Map<String, LocalDateDoubleTimeSeries> _schemeToTs;

    TestHistoricalTimeSeriesProvider(final Map<String, LocalDateDoubleTimeSeries> schemeToTs) {
      _schemeToTs = new HashMap<>(schemeToTs);
    }

    @Override
    public LocalDateDoubleTimeSeries getHistoricalTimeSeries(final ExternalIdBundle externalIdBundle, final String dataSource, final String dataProvider,
        final String dataField) {
      if (externalIdBundle.size() == 1) {
        final LocalDateDoubleTimeSeries hts = _schemeToTs.get(externalIdBundle.getExternalIds().iterator().next().getScheme().getName());
        if (hts != null) {
          return hts;
        }
      }
      throw new DataNotFoundException(externalIdBundle.toString());
    }

    @Override
    public LocalDateDoubleTimeSeries getHistoricalTimeSeries(final ExternalIdBundle externalIdBundle, final String dataSource, final String dataProvider,
        final String dataField, final LocalDateRange dateRange) {
      return getHistoricalTimeSeries(externalIdBundle, dataSource, dataProvider, dataField);
    }

    @Override
    public Pair<LocalDate, Double> getLatestDataPoint(final ExternalIdBundle externalIdBundle, final String dataSource, final String dataProvider,
        final String dataField) {
      final LocalDateDoubleTimeSeries hts = getHistoricalTimeSeries(externalIdBundle, dataSource, dataProvider, dataField);
      return Pairs.of(hts.getLatestTime(), hts.getLatestValue());
    }

    @Override
    public Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getHistoricalTimeSeries(final Set<ExternalIdBundle> externalIdBundleSet, final String dataSource,
        final String dataProvider, final String dataField, final LocalDateRange dateRange) {
      final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> results = new HashMap<>();
      for (final ExternalIdBundle externalIdBundle : externalIdBundleSet) {
        results.put(externalIdBundle, getHistoricalTimeSeries(externalIdBundle, dataSource, dataProvider, dataField));
      }
      return results;
    }

    @Override
    public HistoricalTimeSeriesProviderGetResult getHistoricalTimeSeries(final HistoricalTimeSeriesProviderGetRequest request) {
      return new HistoricalTimeSeriesProviderGetResult(getHistoricalTimeSeries(request.getExternalIdBundles(), request.getDataSource(),
          request.getDataProvider(), request.getDataField(), request.getDateRange()));
    }

  }

}
