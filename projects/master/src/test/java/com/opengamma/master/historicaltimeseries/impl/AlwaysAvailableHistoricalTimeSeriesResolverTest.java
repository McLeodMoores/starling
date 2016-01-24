/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesInfo;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link AlwaysAvailableHistoricalTimeSeriesResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class AlwaysAvailableHistoricalTimeSeriesResolverTest {
  /** The time series selector */
  private static final HistoricalTimeSeriesSelector SELECTOR = new TestHistoricalTimeSeriesSelector();
  /** The master */
  private static final InMemoryHistoricalTimeSeriesMaster MASTER = new InMemoryHistoricalTimeSeriesMaster();
  /** The ids of a time series stored in the master */
  private static final ExternalIdBundle AVAILABLE_IDS = ExternalIdBundle.of("TEST", "A");
  /** The data source */
  private static final String DATA_SOURCE = "SOURCE";
  /** The data provider */
  private static final String DATA_PROVIDER = "PROVIDER";
  /** The data field */
  private static final String DATA_FIELD = "FIELD";
  /** The observation time */
  private static final String OBSERVATION_TIME = "TIME";

  static {
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setExternalIdBundle(ExternalIdBundleWithDates.of(AVAILABLE_IDS));
    info.setDataSource(DATA_SOURCE);
    info.setDataProvider(DATA_PROVIDER);
    info.setDataField(DATA_FIELD);
    info.setObservationTime(OBSERVATION_TIME);
    final HistoricalTimeSeriesInfoDocument document = new HistoricalTimeSeriesInfoDocument();
    document.setInfo(info);
    MASTER.add(document);
  }

  /**
   * Tests the behaviour when the selector is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSelector() {
    new AlwaysAvailableHistoricalTimeSeriesResolver(null, new InMemoryHistoricalTimeSeriesMaster());
  }

  /**
   * Tests the behaviour when the master is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaster() {
    new AlwaysAvailableHistoricalTimeSeriesResolver(SELECTOR, null);
  }

  /**
   * Tests that if the time series is available from the master, the resolution result is the
   * same as that from the underlying resolver.
   */
  @Test
  public void testUnderlyingResolverUsed() {
    final AlwaysAvailableHistoricalTimeSeriesResolver resolver = new AlwaysAvailableHistoricalTimeSeriesResolver(SELECTOR, MASTER);
    final DefaultHistoricalTimeSeriesResolver underlying = new DefaultHistoricalTimeSeriesResolver(SELECTOR, MASTER);
    final HistoricalTimeSeriesResolutionResult actualResult = resolver.resolve(AVAILABLE_IDS, null, null, null, null, null);
    final HistoricalTimeSeriesResolutionResult expectedResult = underlying.resolve(AVAILABLE_IDS, null, null, null, null, null);
    // no equals() method so need to compare field by field
    assertEquals(actualResult.getAdjuster(), expectedResult.getAdjuster());
    final ManageableHistoricalTimeSeriesInfo actualInfo = actualResult.getHistoricalTimeSeriesInfo();
    final ManageableHistoricalTimeSeriesInfo expectedInfo = expectedResult.getHistoricalTimeSeriesInfo();
    assertEquals(actualInfo.getDataField(), expectedInfo.getDataField());
    assertEquals(actualInfo.getDataField(), DATA_FIELD);
    assertEquals(actualInfo.getDataProvider(), expectedInfo.getDataProvider());
    assertEquals(actualInfo.getDataProvider(), DATA_PROVIDER);
    assertEquals(actualInfo.getDataSource(), expectedInfo.getDataSource());
    assertEquals(actualInfo.getDataSource(), DATA_SOURCE);
    assertEquals(actualInfo.getObservationTime(), expectedInfo.getObservationTime());
    assertEquals(actualInfo.getObservationTime(), OBSERVATION_TIME);
    assertEquals(actualInfo.getExternalIdBundle(), expectedInfo.getExternalIdBundle());
    assertEquals(actualInfo.getExternalIdBundle().toBundle(), AVAILABLE_IDS);
  }

  /**
   * Tests that if the time series is available from the master, there is no missing info for that
   * unique id stored.
   */
  @Test
  public void testNoMissingInfoStoredForAvailableTimeSeries() {
    final AlwaysAvailableHistoricalTimeSeriesResolver resolver = new AlwaysAvailableHistoricalTimeSeriesResolver(SELECTOR, MASTER);
    final HistoricalTimeSeriesResolutionResult result = resolver.resolve(AVAILABLE_IDS, null, null, null, null, null);
    assertNotNull(result);
    assertNull(resolver.getMissingTimeSeriesInfoForUniqueId(result.getHistoricalTimeSeriesInfo().getUniqueId()));
  }

  /**
   * Tests that a unique id for a time series that was not generated by this resolver returns null.
   */
  @Test
  public void testNoMissingInfoStoredForUnresolvedTimeSeries() {
    final AlwaysAvailableHistoricalTimeSeriesResolver resolver = new AlwaysAvailableHistoricalTimeSeriesResolver(SELECTOR, MASTER);
    assertNull(resolver.getMissingTimeSeriesInfoForUniqueId(UniqueId.of(AlwaysAvailableHistoricalTimeSeriesResolver.SCHEME, "1")));
    assertNull(resolver.getMissingTimeSeriesInfoForUniqueId(UniqueId.of("DbHts", "1")));
  }

  /**
   * Tests that the resolution result for a time series that is not available from the master is returned
   * with a unique id from this resolver.
   */
  @Test
  public void testResolutionForMissingTimeSeries() {
    final ExternalIdBundle missingId = ExternalIdBundle.of("TEST", "B");
    final AlwaysAvailableHistoricalTimeSeriesResolver resolver = new AlwaysAvailableHistoricalTimeSeriesResolver(SELECTOR, MASTER);
    final HistoricalTimeSeriesResolutionResult result = resolver.resolve(missingId, null, null, null, null, null);
    assertNotNull(result);
    final HistoricalTimeSeriesInfo info = result.getHistoricalTimeSeriesInfo();
    // all null because these fields weren't set when requesting the info
    assertNull(info.getDataField());
    assertNull(info.getDataProvider());
    assertNull(info.getDataSource());
    assertNull(info.getObservationTime());
    assertEquals(info.getExternalIdBundle().toBundle(), missingId);
    final UniqueId uid = info.getUniqueId();
    assertNotNull(uid);
    assertEquals(uid.getScheme(), AlwaysAvailableHistoricalTimeSeriesResolver.SCHEME);
    final ObjectId oid = info.getTimeSeriesObjectId();
    assertNotNull(oid);
    assertEquals(oid.getScheme(), AlwaysAvailableHistoricalTimeSeriesResolver.SCHEME);
  }

  /**
   * Tests that the info for a time series that is not available from the master is stored internally
   * in the resolver.
   */
  @Test
  public void testMissingInfoForMissingTimeSeries() {
    final ExternalIdBundle missingId = ExternalIdBundle.of("TEST", "B");
    final AlwaysAvailableHistoricalTimeSeriesResolver resolver = new AlwaysAvailableHistoricalTimeSeriesResolver(SELECTOR, MASTER);
    final HistoricalTimeSeriesResolutionResult result = resolver.resolve(missingId, null, null, null, null, null);
    final ManageableHistoricalTimeSeriesInfo actualInfo = result.getHistoricalTimeSeriesInfo();
    final ManageableHistoricalTimeSeriesInfo expectedInfo = resolver.getMissingTimeSeriesInfoForUniqueId(result.getHistoricalTimeSeriesInfo().getUniqueId());
    assertEquals(actualInfo.getDataField(), expectedInfo.getDataField());
    assertEquals(actualInfo.getDataProvider(), expectedInfo.getDataProvider());
    assertEquals(actualInfo.getDataSource(), expectedInfo.getDataSource());
    assertEquals(actualInfo.getObservationTime(), expectedInfo.getObservationTime());
    assertEquals(actualInfo.getExternalIdBundle(), expectedInfo.getExternalIdBundle());
    assertEquals(actualInfo.getExternalIdBundle().toBundle(), missingId);
  }
}
