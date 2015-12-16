/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.historicaltimeseries.QuandlHistoricalTimeSeriesLoader;
import com.mcleodmoores.quandl.testutils.MockHistoricalTimeSeriesProvider;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesConstants;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderResult;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Unit tests for {@link QuandlHistoricalTimeSeriesLoader}.
 */
public class QuandlHistoricalTimeSeriesLoaderTest {
  /** A mock historical time series provider */
  private static final HistoricalTimeSeriesProvider HTS_PROVIDER;
  /** A time series identifier */
  private static final ExternalId ID1 = QuandlConstants.ofCode("HTS1");
  /** A time series identifier */
  private static final ExternalId ID2 = QuandlConstants.ofCode("HTS2");
  /** A time series */
  private static final LocalDateDoubleTimeSeries TS1;
  /** A time series */
  private static final LocalDateDoubleTimeSeries TS2;
  /** Time series info for the first series */
  private static final ManageableHistoricalTimeSeriesInfo HTS_INFO1;
  /** The time series start date */
  private static final LocalDate START_DATE = LocalDate.of(2010, 1, 1);
  /** The time series end date */
  private static final LocalDate END_DATE = LocalDate.of(2010, 1, 10);

  static {
    LocalDate date = START_DATE;
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> values1 = new ArrayList<>();
    final List<Double> values2 = new ArrayList<>();
    while (date.isBefore(END_DATE)) {
      switch (date.getDayOfWeek()) {
        case SATURDAY:
          date = date.plusDays(2);
        case SUNDAY:
          date = date.plusDays(1);
        default:
          dates.add(date);
          values1.add(1.);
          values2.add(10.);
          date = date.plusDays(1);
      }
    }
    final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> ts = new HashMap<>();
    TS1 = ImmutableLocalDateDoubleTimeSeries.of(dates, values1);
    TS2 = ImmutableLocalDateDoubleTimeSeries.of(dates, values2);
    ts.put(ID1.toBundle(), TS1);
    ts.put(ID2.toBundle(), TS2);
    HTS_PROVIDER = new MockHistoricalTimeSeriesProvider(ts);
    HTS_INFO1 = new ManageableHistoricalTimeSeriesInfo();
    HTS_INFO1.setDataField(QuandlConstants.RATE_FIELD_NAME);
    HTS_INFO1.setDataProvider(QuandlConstants.DEFAULT_PROVIDER);
    HTS_INFO1.setDataSource(QuandlConstants.QUANDL_DATA_SOURCE_NAME);
    HTS_INFO1.setObservationTime(HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME);
    HTS_INFO1.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID1, LocalDate.MIN, LocalDate.MAX)));
  }

  /**
   * Tests behaviour when the master is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullMaster() {
    new QuandlHistoricalTimeSeriesLoader(null, HTS_PROVIDER);
  }

  /**
   * Tests behaviour when the provider is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullProvider() {
    new QuandlHistoricalTimeSeriesLoader(new InMemoryHistoricalTimeSeriesMaster(), null);
  }

  /**
   * Tests the behaviour of bulk loading when the request is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullRequest() {
    final QuandlHistoricalTimeSeriesLoader loader = new QuandlHistoricalTimeSeriesLoader(new InMemoryHistoricalTimeSeriesMaster(), HTS_PROVIDER);
    loader.doBulkLoad(null);
  }

  /**
   * Tests the behaviour of the bulk loader when the data field in the request is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullDataField() {
    final QuandlHistoricalTimeSeriesLoader loader = new QuandlHistoricalTimeSeriesLoader(new InMemoryHistoricalTimeSeriesMaster(), HTS_PROVIDER);
    final HistoricalTimeSeriesLoaderRequest request = HistoricalTimeSeriesLoaderRequest.create(Collections.singleton(QuandlConstants.ofCode("Test")),
        QuandlConstants.DEFAULT_PROVIDER, null, null, null);
    loader.doBulkLoad(request);
  }

  /**
   * Tests the observation time observed. The observation time will be null if the data provider is not null,
   * unknown or default.
   */
  @Test
  public void testObservationProvider() {
    assertEquals(QuandlHistoricalTimeSeriesLoader.resolveObservationTime(null), HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME);
    assertEquals(QuandlHistoricalTimeSeriesLoader.resolveObservationTime(QuandlHistoricalTimeSeriesLoader.UNKNOWN_DATA_PROVIDER),
        HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME);
    assertEquals(QuandlHistoricalTimeSeriesLoader.resolveObservationTime(QuandlHistoricalTimeSeriesLoader.DEFAULT_DATA_PROVIDER),
        HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME);
    assertNull(QuandlHistoricalTimeSeriesLoader.resolveObservationTime("Provider"));
  }

  /**
   * Tests that all time series that are not in the master are identified.
   */
  @Test
  public void testMissingTimeSeries() {
    final InMemoryHistoricalTimeSeriesMaster htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    final QuandlHistoricalTimeSeriesLoader loader = new QuandlHistoricalTimeSeriesLoader(htsMaster, HTS_PROVIDER);
    // no time series are present in the master
    final Set<ExternalId> ids = Sets.newHashSet(ID1, ID2);
    Map<ExternalId, UniqueId> result = new HashMap<>();
    Set<ExternalId> missingIds = loader.findTimeSeries(ids, QuandlConstants.DEFAULT_PROVIDER, QuandlConstants.RATE_FIELD_NAME, result);
    assertEquals(result.size(), 0);
    assertEquals(missingIds, ids);
    // tests that the default provider name is used if null has been supplied
    assertEquals(loader.findTimeSeries(ids, null, QuandlConstants.RATE_FIELD_NAME, result), missingIds);
    // add one time series to the master
    final HistoricalTimeSeriesInfoDocument added = htsMaster.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO1));
    final ObjectId oid = added.getObjectId();
    htsMaster.updateTimeSeriesDataPoints(oid, TS1);
    result = new HashMap<>();
    missingIds = loader.findTimeSeries(ids, QuandlConstants.DEFAULT_PROVIDER, QuandlConstants.RATE_FIELD_NAME, result);
    assertEquals(result.size(), 1);
    assertEquals(Iterables.getOnlyElement(result.entrySet()).getKey(), ID1);
    assertEquals(missingIds, Collections.singleton(ID2));
  }

  /**
   * Tests that the expected time series is returned from the underlying provider and that the start and end dates
   * are used.
   */
  @Test
  public void testLoadTimeSeriesFromProvider() {
    final InMemoryHistoricalTimeSeriesMaster htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    final QuandlHistoricalTimeSeriesLoader loader = new QuandlHistoricalTimeSeriesLoader(htsMaster, HTS_PROVIDER);
    final LocalDate date = START_DATE.plusMonths(1);
    final Set<ExternalIdBundle> ids = Sets.newHashSet(ID1.toBundle(), ID2.toBundle());
    HistoricalTimeSeriesProviderGetResult result = loader.provideTimeSeries(ids, QuandlConstants.RATE_FIELD_NAME,
        QuandlConstants.DEFAULT_PROVIDER, LocalDate.MIN, LocalDate.MAX);
    Map<ExternalIdBundle, LocalDateDoubleTimeSeries> results = result.getResultMap();
    assertEquals(results.size(), 2);
    assertEquals(results.keySet(), ids);
    assertEquals(results.get(ID1.toBundle()), TS1);
    assertEquals(results.get(ID2.toBundle()), TS2);
    // start and end dates are set
    result = loader.provideTimeSeries(ids, QuandlConstants.RATE_FIELD_NAME, QuandlConstants.DEFAULT_PROVIDER, date, date.plusMonths(1));
    results = result.getResultMap();
    assertEquals(results.size(), 2);
    assertEquals(results.keySet(), ids);
    assertEquals(results.get(ID1.toBundle()), TS1.subSeries(date, date.plusMonths(1)));
    assertEquals(results.get(ID2.toBundle()), TS2.subSeries(date, date.plusMonths(1)));
  }

  /**
   * Tests that metadata is set correctly for a time series when it is stored in the master.
   */
  @Test
  public void testStoreTimeSeries() {
    final Set<ExternalIdBundle> ids = Sets.newHashSet(ID1.toBundle(), ID2.toBundle());
    final Map<ExternalIdBundleWithDates, ExternalId> bundleToIdentifier = new HashMap<>();
    bundleToIdentifier.put(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID1, LocalDate.MIN, LocalDate.MAX)), ID1);
    bundleToIdentifier.put(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID2, LocalDate.MIN, LocalDate.MAX)), ID2);
    final Map<ExternalIdBundle, ExternalIdBundleWithDates> identifiersToBundleWithDates = new HashMap<>();
    identifiersToBundleWithDates.put(ID1.toBundle(), ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID1, LocalDate.MIN, LocalDate.MAX)));
    identifiersToBundleWithDates.put(ID2.toBundle(), ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID2, LocalDate.MIN, LocalDate.MAX)));
    // no time series in master
    InMemoryHistoricalTimeSeriesMaster htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    QuandlHistoricalTimeSeriesLoader loader = new QuandlHistoricalTimeSeriesLoader(htsMaster, HTS_PROVIDER);
    final HistoricalTimeSeriesProviderGetResult tsResult = loader.provideTimeSeries(ids, QuandlConstants.RATE_FIELD_NAME,
        QuandlConstants.DEFAULT_PROVIDER, LocalDate.MIN, LocalDate.MAX);
    Map<ExternalId, UniqueId> result = new HashMap<>();
    loader.storeTimeSeries(tsResult, QuandlConstants.RATE_FIELD_NAME, QuandlConstants.DEFAULT_PROVIDER, bundleToIdentifier,
        identifiersToBundleWithDates, result);
    assertEquals(result.size(), 2);
    assertEquals(result.keySet(), Sets.newHashSet(ID1, ID2));
    // check that the time series are now in the master
    ManageableHistoricalTimeSeries hts1 = htsMaster.getTimeSeries(result.get(ID1));
    assertNotNull(hts1);
    assertNotNull(hts1.getTimeSeries());
    assertEquals(hts1.getTimeSeries(), TS1);
    ManageableHistoricalTimeSeries hts2 = htsMaster.getTimeSeries(result.get(ID2));
    assertNotNull(hts2);
    assertNotNull(hts2.getTimeSeries());
    assertEquals(hts2.getTimeSeries(), TS2);
    // first time series is in master, will be updated, and second time series will be added
    htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    loader = new QuandlHistoricalTimeSeriesLoader(htsMaster, HTS_PROVIDER);
    final HistoricalTimeSeriesInfoDocument added = htsMaster.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO1));
    final ObjectId oid = added.getObjectId();
    final LocalDateDoubleTimeSeries previousTs1 = ImmutableLocalDateDoubleTimeSeries.of(START_DATE.minusDays(1), 10);
    htsMaster.updateTimeSeriesDataPoints(oid, previousTs1);
    // one time series in master, should be updated
    result = new HashMap<>();
    loader.storeTimeSeries(tsResult, QuandlConstants.RATE_FIELD_NAME, QuandlConstants.DEFAULT_PROVIDER, bundleToIdentifier,
        identifiersToBundleWithDates, result);
    assertEquals(result.size(), 2);
    assertEquals(result.keySet(), Sets.newHashSet(ID1, ID2));
    // first time series should have been updated
    hts1 = htsMaster.getTimeSeries(result.get(ID1));
    assertNotNull(hts1);
    assertNotNull(hts1.getTimeSeries());
    assertEquals(hts1.getTimeSeries().size(), TS1.size() + 1);
    assertEquals(hts1.getTimeSeries(), TS1.unionAdd(previousTs1));
    // second time series should have been added to the master
    hts2 = htsMaster.getTimeSeries(result.get(ID2));
    assertNotNull(hts2);
    assertNotNull(hts2.getTimeSeries());
    assertEquals(hts2.getTimeSeries(), TS2);
  }

  /**
   * Tests that time series are updated correctly.
   */
  @Test
  public void testUpdate() {
    final InMemoryHistoricalTimeSeriesMaster htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument ts = htsMaster.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO1));
    final QuandlHistoricalTimeSeriesLoader loader = new QuandlHistoricalTimeSeriesLoader(htsMaster, HTS_PROVIDER);
    final UniqueId uid = ts.getUniqueId();
    ManageableHistoricalTimeSeries hts = htsMaster.getTimeSeries(uid);
    // time series has not been loaded
    assertEquals(hts.getTimeSeries(), ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    assertTrue(loader.updateTimeSeries(uid));
    // time series has been loaded from provider
    hts = htsMaster.getTimeSeries(uid);
    assertEquals(hts.getTimeSeries(), TS1);
  }

  /**
   * Tests that all of the time series that are stored in the master are returned.
   */
  @Test
  public void testBulkLoad() {
    final InMemoryHistoricalTimeSeriesMaster htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    final QuandlHistoricalTimeSeriesLoader loader = new QuandlHistoricalTimeSeriesLoader(htsMaster, HTS_PROVIDER);
    final HistoricalTimeSeriesLoaderRequest request = HistoricalTimeSeriesLoaderRequest.create(Collections.singleton(ID1),
        QuandlConstants.DEFAULT_PROVIDER, QuandlConstants.RATE_FIELD_NAME, null, null);
    final HistoricalTimeSeriesLoaderResult bulkLoad = loader.doBulkLoad(request);
    assertNotNull(bulkLoad);
    final Map<ExternalId, UniqueId> resultMap = bulkLoad.getResultMap();
    assertEquals(resultMap.size(), 1);
    assertEquals(Iterables.getOnlyElement(resultMap.keySet()), ID1);
    assertEquals(loader.doBulkLoad(HistoricalTimeSeriesLoaderRequest.create(Collections.singleton(ID1), QuandlConstants.DEFAULT_PROVIDER,
        QuandlConstants.RATE_FIELD_NAME, START_DATE, END_DATE)).getResultMap(), resultMap);
  }

}
