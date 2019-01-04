/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Test {@link MasterHistoricalTimeSeriesSource}.
 * Ensure it makes the right method calls to the underlying master and resolver.
 */
@Test(groups = TestGroup.UNIT)
public class MasterHistoricalTimeSeriesSourceTest {

  private static final String TEST_CONFIG = "TEST_CONFIG";
  private static final UniqueId UID_1 = UniqueId.of("A", "1");
  private static final UniqueId UID_2 = UniqueId.of("A", "2");
  private static final String CLOSE_DATA_FIELD = "CLOSE";
  private static final String CMPL_DATA_PROVIDER = "CMPL";
  private static final String BBG_DATA_SOURCE = "BLOOMBERG";
  private static final ExternalIdBundle IDENTIFIERS = ExternalIdBundle.of("A", "B");
  private static final ExternalIdBundleWithDates IDENTIFIERS_WITH_DATES;
  static {
    final List<ExternalIdWithDates> ids = new ArrayList<>();
    for (final ExternalId id : IDENTIFIERS) {
      ids.add(ExternalIdWithDates.of(id, LocalDate.now(), LocalDate.now().plusYears(1)));
    }
    IDENTIFIERS_WITH_DATES = ExternalIdBundleWithDates.of(ids);
  }
  private static final HistoricalTimeSeriesAdjuster ADJUSTER = new HistoricalTimeSeriesAdjuster() {

    @Override
    public HistoricalTimeSeries adjust(final ExternalIdBundle securityIdBundle, final HistoricalTimeSeries timeSeries) {
      return getAdjustment(securityIdBundle).adjust(timeSeries);
    }

    @Override
    public HistoricalTimeSeriesAdjustment getAdjustment(final ExternalIdBundle securityIdBundle) {
      return new HistoricalTimeSeriesAdjustment.DivideBy(100);
    }

  };

  private HistoricalTimeSeriesMaster _mockMaster;
  private HistoricalTimeSeriesResolver _mockResolver;
  private MasterHistoricalTimeSeriesSource _tsSource;

  /**
   * Sets up the master, resolver and source.
   *
   * @throws Exception  if there is a problem with the setup
   */
  @BeforeMethod
  public void setUp() throws Exception {
    _mockMaster = mock(HistoricalTimeSeriesMaster.class);
    _mockResolver = mock(HistoricalTimeSeriesResolver.class);
    _tsSource = new MasterHistoricalTimeSeriesSource(_mockMaster, _mockResolver);
  }

  /**
   * Tears down the master, resolver and source.
   *
   * @throws Exception  if there is a problem with the tear-down
   */
  @AfterMethod
  public void tearDown() throws Exception {
    _tsSource = null;
    _mockResolver = null;
    _mockMaster = null;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks that the underlying master cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructorWith1ArgNull() {
    final HistoricalTimeSeriesResolver mock = mock(HistoricalTimeSeriesResolver.class);
    new MasterHistoricalTimeSeriesSource(null, mock);
  }

  /**
   * Checks that the underyling resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructorWith2ArgNull() {
    final HistoricalTimeSeriesMaster mock = mock(HistoricalTimeSeriesMaster.class);
    new MasterHistoricalTimeSeriesSource(mock, null);
  }

  /**
   * Checks that the underylings cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructorWithNull() {
    new MasterHistoricalTimeSeriesSource(null, null);
  }

  /**
   * Tests that null is returned if the identifiers cannot be resolved.
   */
  public void testNullResolutionResult() {
    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null))
    .thenReturn(null);
    final HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertNull(test);
  }

  /**
   * Tests that null is returned if the identifiers cannot be resolved.
   */
  public void testValidityDateNullResolutionResult() {
    final LocalDate validityDate = LocalDate.now();
    when(_mockResolver.resolve(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null))
    .thenReturn(null);
    final HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    assertNull(test);
  }

  /**
   * Tests retrieval of a time series using a request that has meta-data attached.
   */
  public void getHistoricalTimeSeriesByExternalIdWithMetaData()  {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    request.setValidityDate(LocalDate.now());
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);

    final HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID_1);

    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null))
    .thenReturn(new HistoricalTimeSeriesResolutionResult(doc.getInfo()));

    doc.getInfo().setTimeSeriesObjectId(UID_1.getObjectId());
    searchResult.getDocuments().add(doc);
    when(_mockMaster.search(request)).thenReturn(searchResult);

    final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID_1);
    final LocalDateDoubleTimeSeries randomTimeSeries = randomTimeSeries();
    hts.setTimeSeries(randomTimeSeries);
    when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1))).thenReturn(hts);

    final HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    final Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    verify(_mockMaster, times(1)).getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1));

    assertEquals(UID_1, test.getUniqueId());
    assertEquals(randomTimeSeries, test.getTimeSeries());
    assertEquals(randomTimeSeries.getLatestTime(), latest.getFirst());
    assertEquals(randomTimeSeries.getLatestValue(), latest.getSecond());
  }

  /**
   * Tests retrieval of a time series using a request that has meta-data attached and a validity date.
   */
  public void getHistoricalTimeSeriesByExternalIdWithMetaDataAndValidityDate()  {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    final LocalDate validityDate = LocalDate.now();
    request.setValidityDate(validityDate);
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);

    final HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID_1);

    when(_mockResolver.resolve(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null))
    .thenReturn(new HistoricalTimeSeriesResolutionResult(doc.getInfo()));

    doc.getInfo().setTimeSeriesObjectId(UID_1.getObjectId());
    searchResult.getDocuments().add(doc);
    when(_mockMaster.search(request)).thenReturn(searchResult);

    final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID_1);
    final LocalDateDoubleTimeSeries randomTimeSeries = randomTimeSeries();
    hts.setTimeSeries(randomTimeSeries);
    when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1))).thenReturn(hts);

    final HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    final Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1));

    assertEquals(UID_1, test.getUniqueId());
    assertEquals(randomTimeSeries, test.getTimeSeries());
    assertEquals(randomTimeSeries.getLatestTime(), latest.getFirst());
    assertEquals(randomTimeSeries.getLatestValue(), latest.getSecond());
  }

  /**
   * Tests retrieval of an adjusted time series using a request that has meta-data attached.
   */
  public void getHistoricalTimeSeriesByExternalIdWithMetaDataAndAdjuster()  {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    request.setValidityDate(LocalDate.now());
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);

    final HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID_1);
    doc.getInfo().setExternalIdBundle(IDENTIFIERS_WITH_DATES);

    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null))
    .thenReturn(new HistoricalTimeSeriesResolutionResult(doc.getInfo(), ADJUSTER));

    doc.getInfo().setTimeSeriesObjectId(UID_1.getObjectId());
    searchResult.getDocuments().add(doc);
    when(_mockMaster.search(request)).thenReturn(searchResult);

    final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID_1);
    final LocalDateDoubleTimeSeries randomTimeSeries = randomTimeSeries();
    hts.setTimeSeries(randomTimeSeries);
    when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1))).thenReturn(hts);

    final HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    final Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1));

    assertEquals(UID_1, test.getUniqueId());
    assertEquals(randomTimeSeries.divide(100), test.getTimeSeries());
    assertEquals(randomTimeSeries.getLatestTime(), latest.getFirst());
    assertEquals(randomTimeSeries.getLatestValue() / 100, latest.getSecond());
  }

  /**
   * Tests retrieval of an adjusted time series using a request that has meta-data attached and a validity date.
   */
  public void getHistoricalTimeSeriesByExternalIdWithMetaDataValidityDateAndAdjuster()  {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    final LocalDate validityDate = LocalDate.now();
    request.setValidityDate(validityDate);
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);

    final HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID_1);
    doc.getInfo().setExternalIdBundle(IDENTIFIERS_WITH_DATES);

    when(_mockResolver.resolve(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null))
    .thenReturn(new HistoricalTimeSeriesResolutionResult(doc.getInfo(), ADJUSTER));

    doc.getInfo().setTimeSeriesObjectId(UID_1.getObjectId());
    searchResult.getDocuments().add(doc);
    when(_mockMaster.search(request)).thenReturn(searchResult);

    final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID_1);
    final LocalDateDoubleTimeSeries randomTimeSeries = randomTimeSeries();
    hts.setTimeSeries(randomTimeSeries);
    when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1))).thenReturn(hts);

    final HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    final Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1));

    assertEquals(UID_1, test.getUniqueId());
    assertEquals(randomTimeSeries.divide(100), test.getTimeSeries());
    assertEquals(randomTimeSeries.getLatestTime(), latest.getFirst());
    assertEquals(randomTimeSeries.getLatestValue() / 100, latest.getSecond());
  }

  /**
   * Tests the exception when there is no time series available for an id.
   */
  public void getHistoricalTimeSeriesNoResolutionResultForId1() {
    assertNull(_tsSource.getHistoricalTimeSeries(CLOSE_DATA_FIELD, IDENTIFIERS, TEST_CONFIG));
  }

  /**
   * Tests the exception when there is no time series available for an id.
   */
  public void getLatestDataPointNoResolutionResultForId1() {
    assertNull(_tsSource.getLatestDataPoint(CLOSE_DATA_FIELD, IDENTIFIERS, TEST_CONFIG));
  }

  /**
   * Tests the exception when there is no time series available for an id.
   */
  public void getHistoricalTimeSeriesNoResolutionResultForId2() {
    assertNull(_tsSource.getHistoricalTimeSeries(CLOSE_DATA_FIELD, IDENTIFIERS, LocalDate.now(), TEST_CONFIG, LocalDate.now().minusDays(7), true, LocalDate.now(), true, 3));
  }

  /**
   * Tests the exception when there is no time series available for an id.
   */
  public void getLatestDataPointNoResolutionResultForId2() {
    assertNull(_tsSource.getLatestDataPoint(CLOSE_DATA_FIELD, IDENTIFIERS, LocalDate.now(), TEST_CONFIG, LocalDate.now().minusDays(7), true, LocalDate.now(), true));
  }

  /**
   * Tests that the default name is used for resolution.
   */
  public void testDefaultTssResolverNameUsed() {
    final LocalDateDoubleTimeSeries randomTimeSeries1 = randomTimeSeries();
    final LocalDateDoubleTimeSeries randomTimeSeries2 = randomTimeSeries();
    final ManageableHistoricalTimeSeries hts1 = new ManageableHistoricalTimeSeries();
    hts1.setUniqueId(UID_1);
    hts1.setTimeSeries(randomTimeSeries1);
    final ManageableHistoricalTimeSeries hts2 = new ManageableHistoricalTimeSeries();
    hts2.setUniqueId(UID_2);
    hts2.setTimeSeries(randomTimeSeries2);
    when(_mockMaster.getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts1);
    when(_mockMaster.getTimeSeries(UID_2, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts2);
    when(_mockMaster.getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1))).thenReturn(hts1);
    when(_mockMaster.getTimeSeries(UID_2, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1))).thenReturn(hts2);
    final ManageableHistoricalTimeSeriesInfo tsInfo1 = new ManageableHistoricalTimeSeriesInfo();
    tsInfo1.setUniqueId(UID_1);
    final ManageableHistoricalTimeSeriesInfo tsInfo2 = new ManageableHistoricalTimeSeriesInfo();
    tsInfo2.setUniqueId(UID_2);
    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), null, null, CLOSE_DATA_FIELD, TEST_CONFIG))
    .thenReturn(new HistoricalTimeSeriesResolutionResult(tsInfo1));
    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), null, null, CLOSE_DATA_FIELD, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME))
    .thenReturn(new HistoricalTimeSeriesResolutionResult(tsInfo2));
    final HistoricalTimeSeries test1 = _tsSource.getHistoricalTimeSeries(CLOSE_DATA_FIELD, IDENTIFIERS, TEST_CONFIG);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    final HistoricalTimeSeries test2 = _tsSource.getHistoricalTimeSeries(CLOSE_DATA_FIELD, IDENTIFIERS, null);
    verify(_mockMaster, times(1)).getTimeSeries(UID_2, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    final Pair<LocalDate, Double> latest1 = _tsSource.getLatestDataPoint(CLOSE_DATA_FIELD, IDENTIFIERS, TEST_CONFIG);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1));
    final Pair<LocalDate, Double> latest2 = _tsSource.getLatestDataPoint(CLOSE_DATA_FIELD, IDENTIFIERS, null);
    verify(_mockMaster, times(1)).getTimeSeries(UID_2, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1));

    assertEquals(UID_1, test1.getUniqueId());
    assertEquals(randomTimeSeries1, test1.getTimeSeries());
    assertEquals(UID_2, test2.getUniqueId());
    assertEquals(randomTimeSeries2, test2.getTimeSeries());
    assertEquals(randomTimeSeries1.getLatestTime(), latest1.getFirst());
    assertEquals(randomTimeSeries1.getLatestValue(), latest1.getSecond());
    assertEquals(randomTimeSeries2.getLatestTime(), latest2.getFirst());
    assertEquals(randomTimeSeries2.getLatestValue(), latest2.getSecond());
  }

  /**
   * Tests retrieval of a time series using a simple request.
   */
  public void getHistoricalTimeSeriesByExternalIdWithoutMetaData() {
    final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID_1);
    hts.setTimeSeries(randomTimeSeries());
    when(_mockMaster.getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    when(_mockMaster.getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1))).thenReturn(hts);
    final ManageableHistoricalTimeSeriesInfo tsInfo = new ManageableHistoricalTimeSeriesInfo();
    tsInfo.setUniqueId(UID_1);
    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), null, null, CLOSE_DATA_FIELD, TEST_CONFIG)).thenReturn(new HistoricalTimeSeriesResolutionResult(tsInfo));

    final HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(CLOSE_DATA_FIELD, IDENTIFIERS, TEST_CONFIG);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    final Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(CLOSE_DATA_FIELD, IDENTIFIERS, TEST_CONFIG);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1));

    assertEquals(UID_1, test.getUniqueId());
    assertEquals(hts.getTimeSeries().times(), test.getTimeSeries().times());
    assertEquals(hts.getTimeSeries().values(), test.getTimeSeries().values());
    assertEquals(hts.getTimeSeries().getLatestTime(), latest.getFirst());
    assertEquals(hts.getTimeSeries().getLatestValue(), latest.getSecond());
  }

  /**
   * Tests retrieval of an adjusted time series using a simple request.
   */
  public void getHistoricalTimeSeriesByExternalIdWithoutMetaDataAndAdjuster() {
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID_1);
    doc.getInfo().setExternalIdBundle(IDENTIFIERS_WITH_DATES);

    when(_mockResolver.resolve(IDENTIFIERS, null, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null))
    .thenReturn(new HistoricalTimeSeriesResolutionResult(doc.getInfo(), ADJUSTER));
    final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID_1);
    hts.setTimeSeries(randomTimeSeries());
    when(_mockMaster.getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    when(_mockMaster.getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1))).thenReturn(hts);
    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), null, null, CLOSE_DATA_FIELD, TEST_CONFIG))
    .thenReturn(new HistoricalTimeSeriesResolutionResult(doc.getInfo(), ADJUSTER));

    doc.getInfo().setTimeSeriesObjectId(UID_1.getObjectId());
    final HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(CLOSE_DATA_FIELD, IDENTIFIERS, TEST_CONFIG);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    final Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(CLOSE_DATA_FIELD, IDENTIFIERS, TEST_CONFIG);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1));

    assertEquals(UID_1, test.getUniqueId());
    assertEquals(hts.getTimeSeries().divide(100), test.getTimeSeries());
    assertEquals(hts.getTimeSeries().getLatestTime(), latest.getFirst());
    assertEquals(hts.getTimeSeries().getLatestValue() / 100, latest.getSecond());
  }

  /**
   * Tests inclusive and exclusive date logic.
   */
  public void getHistoricalWithInclusiveExclusiveDates() {
    final LocalDate end = DateUtils.previousWeekDay();
    final LocalDate start = end.minusDays(7);
    final LocalDate validityDate = LocalDate.now();

    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    request.setValidityDate(LocalDate.now());
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);
    final LocalDateDoubleTimeSeries timeSeries = randomTimeSeries();

    final HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID_1);
    doc.getInfo().setTimeSeriesObjectId(UID_1.getObjectId());
    searchResult.getDocuments().add(doc);

    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null)).thenReturn(new HistoricalTimeSeriesResolutionResult(doc.getInfo()));

    for (final boolean includeStart : new boolean[] {true, false})  {
      for (final boolean includeEnd : new boolean[] {true, false}) {
        // Also test max points limit for various values
        for (final Integer maxPoints : new Integer[] {null, -10, -1, 1, 0, -2, 2, 10}) {
          LocalDate startInput = start;
          LocalDate endInput = end;
          if (!includeStart) {
            startInput = start.plusDays(1);
          }
          if (!includeEnd) {
            endInput = end.minusDays(1);
          }

          final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
          final LocalDateDoubleTimeSeries lddts =
              maxPoints == null || Math.abs(maxPoints) >= timeSeries.subSeries(start, includeStart, end, includeEnd).size()
              ? timeSeries.subSeries(start, includeStart, end, includeEnd)
                  : maxPoints >= 0
                  ? timeSeries.subSeries(start, includeStart, end, includeEnd).head(maxPoints)
                      : timeSeries.subSeries(start, includeStart, end, includeEnd).tail(-maxPoints);
                  hts.setUniqueId(UID_1);
                  hts.setTimeSeries(lddts);
                  when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(startInput, endInput, maxPoints))).thenReturn(hts);
                  when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(startInput, endInput, -1))).thenReturn(hts);
                  when(_mockMaster.search(request)).thenReturn(searchResult);

                  HistoricalTimeSeries test = maxPoints == null
                      ? _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, end, includeEnd)
                          : _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, end, includeEnd, maxPoints);
                      Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, end, includeEnd);

                      assertEquals(UID_1, test.getUniqueId());
                      assertEquals(hts.getTimeSeries(), test.getTimeSeries());
                      if (hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
                        assertNull(latest);
                      } else {
                        assertEquals(hts.getTimeSeries().getLatestTime(), latest.getFirst());
                        assertEquals(hts.getTimeSeries().getLatestValue(), latest.getSecond());
                      }
                      // with validity date
                      test = maxPoints == null
                          ? _tsSource.getHistoricalTimeSeries(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, end, includeEnd)
                              : _tsSource.getHistoricalTimeSeries(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, end, includeEnd, maxPoints);
                          latest = _tsSource.getLatestDataPoint(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, end, includeEnd);

                          assertEquals(UID_1, test.getUniqueId());
                          assertEquals(hts.getTimeSeries(), test.getTimeSeries());
                          if (hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
                            assertNull(latest);
                          } else {
                            assertEquals(hts.getTimeSeries().getLatestTime(), latest.getFirst());
                            assertEquals(hts.getTimeSeries().getLatestValue(), latest.getSecond());
                          }

        }
      }
    }
  }

  /**
   * Tests that the includeStart flag is ignored if the start date is null.
   */
  public void getTimeSeriesNullStartDate() {
    final LocalDateDoubleTimeSeries timeSeries = randomTimeSeries();
    final LocalDate start = timeSeries.getEarliestTime();
    final LocalDate end = DateUtils.previousWeekDay();
    final LocalDate validityDate = LocalDate.now();

    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    request.setValidityDate(LocalDate.now());
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);

    final HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID_1);
    doc.getInfo().setTimeSeriesObjectId(UID_1.getObjectId());
    searchResult.getDocuments().add(doc);

    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null)).thenReturn(new HistoricalTimeSeriesResolutionResult(doc.getInfo()));

    for (final boolean includeStart : new boolean[] {true, false})  {
      for (final boolean includeEnd : new boolean[] {true, false}) {
        // Also test max points limit for various values
        for (final Integer maxPoints : new Integer[] {null, -10, -1, 1, 0, -2, 2, 10}) {
          LocalDate endInput = end;
          if (!includeEnd) {
            endInput = end.minusDays(1);
          }

          final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
          final LocalDateDoubleTimeSeries lddts =
              maxPoints == null || Math.abs(maxPoints) >= timeSeries.subSeries(start, includeStart, end, includeEnd).size()
              ? timeSeries.subSeries(start, includeStart, end, includeEnd)
                  : maxPoints >= 0
                  ? timeSeries.subSeries(start, includeStart, end, includeEnd).head(maxPoints)
                      : timeSeries.subSeries(start, includeStart, end, includeEnd).tail(-maxPoints);
                  hts.setUniqueId(UID_1);
                  hts.setTimeSeries(lddts);
                  when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, endInput, maxPoints))).thenReturn(hts);
                  when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(null, endInput, -1))).thenReturn(hts);
                  when(_mockMaster.search(request)).thenReturn(searchResult);

                  HistoricalTimeSeries test = maxPoints == null
                      ? _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null, includeStart, end, includeEnd)
                          : _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null, includeStart, end, includeEnd, maxPoints);
                      Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null, includeStart, end, includeEnd);

                      assertEquals(UID_1, test.getUniqueId());
                      assertEquals(hts.getTimeSeries(), test.getTimeSeries());
                      if (hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
                        assertNull(latest);
                      } else {
                        assertEquals(hts.getTimeSeries().getLatestTime(), latest.getFirst());
                        assertEquals(hts.getTimeSeries().getLatestValue(), latest.getSecond());
                      }

                      // with validity date
                      test = maxPoints == null
                          ? _tsSource.getHistoricalTimeSeries(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null, includeStart, end, includeEnd)
                              : _tsSource.getHistoricalTimeSeries(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null, includeStart, end, includeEnd, maxPoints);
                          latest = _tsSource.getLatestDataPoint(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null, includeStart, end, includeEnd);

                          assertEquals(UID_1, test.getUniqueId());
                          assertEquals(hts.getTimeSeries(), test.getTimeSeries());
                          if (hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
                            assertNull(latest);
                          } else {
                            assertEquals(hts.getTimeSeries().getLatestTime(), latest.getFirst());
                            assertEquals(hts.getTimeSeries().getLatestValue(), latest.getSecond());
                          }
        }
      }
    }
  }

  /**
   * Tests that the includeStart flag is ignored if the start date is null.
   */
  public void getTimeSeriesNullEndDate() {
    final LocalDate start = DateUtils.previousWeekDay().minusDays(7);
    final LocalDate validityDate = LocalDate.now();

    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(IDENTIFIERS);
    request.setValidityDate(LocalDate.now());
    request.setDataSource(BBG_DATA_SOURCE);
    request.setDataProvider(CMPL_DATA_PROVIDER);
    request.setDataField(CLOSE_DATA_FIELD);
    final LocalDateDoubleTimeSeries timeSeries = randomTimeSeries();
    final LocalDate lastTime = timeSeries.getLatestTime();

    final HistoricalTimeSeriesInfoSearchResult searchResult = new HistoricalTimeSeriesInfoSearchResult();
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UID_1);
    doc.getInfo().setTimeSeriesObjectId(UID_1.getObjectId());
    searchResult.getDocuments().add(doc);

    when(_mockResolver.resolve(IDENTIFIERS, LocalDate.now(), BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, null)).thenReturn(new HistoricalTimeSeriesResolutionResult(doc.getInfo()));

    for (final boolean includeStart : new boolean[] {true, false})  {
      for (final boolean includeEnd : new boolean[] {true, false}) {
        // Also test max points limit for various values
        for (final Integer maxPoints : new Integer[] {null, -10, -1, 1, 0, -2, 2, 10}) {
          LocalDate startInput = start;
          if (!includeStart) {
            startInput = start.plusDays(1);
          }

          final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
          final LocalDateDoubleTimeSeries lddts =
              maxPoints == null || Math.abs(maxPoints) >= timeSeries.subSeries(start, includeStart, lastTime, includeEnd).size()
              ? timeSeries.subSeries(start, includeStart, lastTime, includeEnd)
                  : maxPoints >= 0
                  ? timeSeries.subSeries(start, includeStart, lastTime, includeEnd).head(maxPoints)
                      : timeSeries.subSeries(start, includeStart, lastTime, includeEnd).tail(-maxPoints);
                  hts.setUniqueId(UID_1);
                  hts.setTimeSeries(lddts);
                  when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(startInput, null, maxPoints))).thenReturn(hts);
                  when(_mockMaster.getTimeSeries(UID_1.getObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(startInput, null, -1))).thenReturn(hts);
                  when(_mockMaster.search(request)).thenReturn(searchResult);

                  HistoricalTimeSeries test = maxPoints == null
                      ? _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, null, includeEnd)
                          : _tsSource.getHistoricalTimeSeries(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, null, includeEnd, maxPoints);
                      Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(IDENTIFIERS, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, null, includeEnd);

                      assertEquals(UID_1, test.getUniqueId());
                      assertEquals(hts.getTimeSeries(), test.getTimeSeries());
                      if (hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
                        assertNull(latest);
                      } else {
                        assertEquals(hts.getTimeSeries().getLatestTime(), latest.getFirst());
                        assertEquals(hts.getTimeSeries().getLatestValue(), latest.getSecond());
                      }

                      // validity date
                      test = maxPoints == null
                          ? _tsSource.getHistoricalTimeSeries(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, null, includeEnd)
                              : _tsSource.getHistoricalTimeSeries(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, null, includeEnd, maxPoints);
                          latest = _tsSource.getLatestDataPoint(IDENTIFIERS, validityDate, BBG_DATA_SOURCE, CMPL_DATA_PROVIDER, CLOSE_DATA_FIELD, start, includeStart, null, includeEnd);

                          assertEquals(UID_1, test.getUniqueId());
                          assertEquals(hts.getTimeSeries(), test.getTimeSeries());
                          if (hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
                            assertNull(latest);
                          } else {
                            assertEquals(hts.getTimeSeries().getLatestTime(), latest.getFirst());
                            assertEquals(hts.getTimeSeries().getLatestValue(), latest.getSecond());
                          }
        }
      }
    }
  }

  /**
   * Tests the behaviour when there is no time series available for a UID.
   */
  @SuppressWarnings("unchecked")
  public void testNoTimeSeriesForUID() {
    when(_mockMaster.getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenThrow(DataNotFoundException.class);
    assertNull(_tsSource.getHistoricalTimeSeries(UID_1));
    assertNull(_tsSource.getLatestDataPoint(UID_1));
  }

  /**
   * Tests retrieval of a time series by unique id.
   */
  public void getHistoricalTimeSeriesByUID() {
    final ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UID_1);
    hts.setTimeSeries(randomTimeSeries());
    when(_mockMaster.getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null))).thenReturn(hts);
    when(_mockMaster.getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1))).thenReturn(hts);

    final HistoricalTimeSeries test = _tsSource.getHistoricalTimeSeries(UID_1);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null));
    final Pair<LocalDate, Double> latest = _tsSource.getLatestDataPoint(UID_1);
    verify(_mockMaster, times(1)).getTimeSeries(UID_1, HistoricalTimeSeriesGetFilter.ofRange(null, null, -1));

    assertEquals(UID_1, test.getUniqueId());
    assertEquals(hts.getTimeSeries().times(), test.getTimeSeries().times());
    assertEquals(hts.getTimeSeries().values(), test.getTimeSeries().values());
    assertEquals(hts.getTimeSeries().getLatestTime(), latest.getFirst());
    assertEquals(hts.getTimeSeries().getLatestValue(), latest.getSecond());
  }

  private static LocalDateDoubleTimeSeries randomTimeSeries() {
    return RandomTimeSeriesGenerator.makeRandomTimeSeries(200);
  }

}
