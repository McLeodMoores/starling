/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.historicaltimeseries.QuandlHistoricalTimeSeriesUpdater.MetaDataKey;
import com.mcleodmoores.quandl.testutils.MockHistoricalTimeSeriesProvider;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.DataNotFoundException;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesConstants;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.time.DateUtils;

/**
 * Unit tests for {@link QuandlHistoricalTimeSeriesUpdater}.
 */
@Test//(groups = TestGroup.UNIT)
public class QuandlHistoricalTimeSeriesUpdaterTest {
  /** A mock historical time series provider */
  private static final MockHistoricalTimeSeriesProvider HTS_PROVIDER;
  /** A time series identifier */
  private static final ExternalId ID1 = QuandlConstants.ofCode("HTS1");
  /** A time series identifier */
  private static final ExternalId ID2 = QuandlConstants.ofCode("HTS2");
  /** A time series identifier */
  private static final ExternalId ID3 = QuandlConstants.ofCode("HTS3");
  /** A time series identifier */
  private static final ExternalId ID4 = QuandlConstants.ofCode("HTS4");
  /** A time series */
  private static final LocalDateDoubleTimeSeries TS1;
  /** A time series */
  private static final LocalDateDoubleTimeSeries TS2;
  /** A time series */
  private static final LocalDateDoubleTimeSeries TS3;
  /** A time series */
  private static final LocalDateDoubleTimeSeries TS4;
  /** Time series info for the first series, valid for all dates */
  private static final ManageableHistoricalTimeSeriesInfo HTS_INFO1;
  /** Time series info for the second series, valid only between the start and end dates */
  private static final ManageableHistoricalTimeSeriesInfo HTS_INFO2;
  /** Time series info for the third series, valid only between the start and end dates with observation time Tokyo close */
  private static final ManageableHistoricalTimeSeriesInfo HTS_INFO3;
  /** Time series info for the first series, no validity information set */
  private static final ManageableHistoricalTimeSeriesInfo HTS_INFO4;
  /** The time series start date */
  private static final LocalDate START_DATE = DateUtils.previousWeekDay(LocalDate.now().minusDays(30));
  /** The time series end date */
  private static final LocalDate END_DATE = LocalDate.now();

  static {
    LocalDate date = START_DATE;
    final List<LocalDate> dates = new ArrayList<>();
    final List<LocalDate> shortDates = new ArrayList<>();
    final List<Double> values1 = new ArrayList<>();
    final List<Double> values2 = new ArrayList<>();
    final List<Double> values3 = new ArrayList<>();
    final List<Double> values4 = new ArrayList<>();
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
          if (date.isBefore(END_DATE.minusDays(7))) {
            shortDates.add(date);
            values3.add(100.);
            values4.add(1000.);
          }
          date = date.plusDays(1);
      }
    }
    final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> ts = new HashMap<>();
    TS1 = ImmutableLocalDateDoubleTimeSeries.of(dates, values1);
    TS2 = ImmutableLocalDateDoubleTimeSeries.of(dates, values2);
    TS3 = ImmutableLocalDateDoubleTimeSeries.of(shortDates, values3);
    TS4 = ImmutableLocalDateDoubleTimeSeries.of(shortDates, values4);
    ts.put(ID1.toBundle(), TS1);
    ts.put(ID2.toBundle(), TS2);
    ts.put(ID3.toBundle(), TS3);
    ts.put(ID4.toBundle(), TS4);
    HTS_PROVIDER = new MockHistoricalTimeSeriesProvider(ts);
    HTS_INFO1 = new ManageableHistoricalTimeSeriesInfo();
    HTS_INFO1.setDataField(QuandlConstants.RATE_FIELD_NAME);
    HTS_INFO1.setDataProvider(QuandlConstants.DEFAULT_PROVIDER);
    HTS_INFO1.setDataSource(QuandlConstants.QUANDL_DATA_SOURCE_NAME);
    HTS_INFO1.setObservationTime(HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME);
    HTS_INFO1.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID1, LocalDate.MIN, LocalDate.MAX)));
    HTS_INFO2 = new ManageableHistoricalTimeSeriesInfo();
    HTS_INFO2.setDataField(QuandlConstants.VALUE_FIELD_NAME);
    HTS_INFO2.setDataProvider(QuandlConstants.DEFAULT_PROVIDER);
    HTS_INFO2.setDataSource(QuandlConstants.QUANDL_DATA_SOURCE_NAME);
    HTS_INFO2.setObservationTime(HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME);
    HTS_INFO2.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID2, START_DATE,
        DateUtils.previousWeekDay(DateUtils.previousWeekDay(LocalDate.now())))));
    HTS_INFO3 = new ManageableHistoricalTimeSeriesInfo();
    HTS_INFO3.setDataField(QuandlConstants.RATE_FIELD_NAME);
    HTS_INFO3.setDataProvider(QuandlConstants.DEFAULT_PROVIDER);
    HTS_INFO3.setDataSource(QuandlConstants.QUANDL_DATA_SOURCE_NAME);
    HTS_INFO3.setObservationTime(HistoricalTimeSeriesConstants.TOKYO_CLOSE);
    HTS_INFO3.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID3, START_DATE, DateUtils.previousWeekDay(LocalDate.now()))));
    HTS_INFO4 = new ManageableHistoricalTimeSeriesInfo();
    HTS_INFO4.setDataField(QuandlConstants.RATE_FIELD_NAME);
    HTS_INFO4.setDataProvider(QuandlConstants.DEFAULT_PROVIDER);
    HTS_INFO4.setDataSource(QuandlConstants.QUANDL_DATA_SOURCE_NAME);
    HTS_INFO4.setObservationTime(HistoricalTimeSeriesConstants.LONDON_CLOSE);
    HTS_INFO4.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID4, null, null)));
  }

  /**
   * Tests the behaviour when the master is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullMaster() {
    new QuandlHistoricalTimeSeriesUpdater(null, HTS_PROVIDER);
  }

  /**
   * Tests the behaviour when the provider is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullProvider() {
    new QuandlHistoricalTimeSeriesUpdater(new InMemoryHistoricalTimeSeriesMaster(), null);
  }

  /**
   * Tests that the data provider name is resolved correctly.
   */
  @Test
  public void testResolveDataProvider() {
    assertEquals(QuandlHistoricalTimeSeriesUpdater.resolveDataProvider(null), QuandlHistoricalTimeSeriesLoader.DEFAULT_DATA_PROVIDER);
    assertEquals(QuandlHistoricalTimeSeriesUpdater.resolveDataProvider(QuandlHistoricalTimeSeriesLoader.UNKNOWN_DATA_PROVIDER.toLowerCase()),
        QuandlHistoricalTimeSeriesLoader.DEFAULT_DATA_PROVIDER);
    assertEquals(QuandlHistoricalTimeSeriesUpdater.resolveDataProvider(QuandlHistoricalTimeSeriesLoader.DEFAULT_DATA_PROVIDER.toLowerCase()),
        QuandlHistoricalTimeSeriesLoader.DEFAULT_DATA_PROVIDER);
    assertEquals(QuandlHistoricalTimeSeriesUpdater.resolveDataProvider("Test"), "Test");
  }

  /**
   * Tests that expired time series are correctly identified. This test uses reflection to access the method being tested.
   * @throws NoSuchMethodException  if there is no method called "identifyExpiredTimeSeries"
   * @throws InvocationTargetException  if the method called "identifyExpiredTimeSeries" cannot be invoked
   * @throws IllegalAccessException  if the method called "identifyExpiredTimeSeries" has not been set accessible
   */
  @Test
  public void testExpiredTimeSeriesIdentification() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Set<HistoricalTimeSeriesInfoDocument> docs = new HashSet<>();
    final HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(HTS_INFO1);
    final HistoricalTimeSeriesInfoDocument doc2 = new HistoricalTimeSeriesInfoDocument(HTS_INFO2);
    final HistoricalTimeSeriesInfoDocument doc3 = new HistoricalTimeSeriesInfoDocument(HTS_INFO3);
    final HistoricalTimeSeriesInfoDocument doc4 = new HistoricalTimeSeriesInfoDocument(HTS_INFO4);
    docs.add(doc1);
    docs.add(doc2);
    docs.add(doc3);
    docs.add(doc4);
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(new InMemoryHistoricalTimeSeriesMaster(), HTS_PROVIDER);
    final Class<? extends QuandlHistoricalTimeSeriesUpdater> clazz = updater.getClass();
    final Method method = clazz.getDeclaredMethod("identifyExpiredTimeSeries", Iterable.class);
    method.setAccessible(true);
    // expect first, third and fourth time series to be valid
    @SuppressWarnings("unchecked")
    final List<HistoricalTimeSeriesInfoDocument> validTs = (List<HistoricalTimeSeriesInfoDocument>) method.invoke(null, docs);
    assertEquals(validTs.size(), 3);
    assertEquals(Sets.newHashSet(validTs), Sets.newHashSet(doc1, doc3, doc4));
  }

  /**
   * Tests that only Quandl time series are valid to be updated. This test uses reflection to access the method being tested.
   * @throws NoSuchMethodException  if there is no method called "getCurrentQuandlTimeSeriesDocuments"
   * @throws InvocationTargetException  if the method called "getCurrentQuandlTimeSeriesDocuments" cannot be invoked
   * @throws IllegalAccessException  if the method called "getCurrentQuandlTimeSeriesDocuments" has not been set accessible
   */
  @Test
  public void testCurrentQuandlTimeSeriesDocuments() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final ManageableHistoricalTimeSeriesInfo htsInfo = new ManageableHistoricalTimeSeriesInfo();
    htsInfo.setDataField(QuandlConstants.RATE_FIELD_NAME);
    htsInfo.setDataProvider(QuandlConstants.DEFAULT_PROVIDER);
    htsInfo.setDataSource("Test");
    htsInfo.setObservationTime(HistoricalTimeSeriesConstants.DEFAULT_OBSERVATION_TIME);
    htsInfo.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ID2, null, LocalDate.MAX)));
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO1));
    master.add(new HistoricalTimeSeriesInfoDocument(htsInfo));
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, HTS_PROVIDER);
    final Class<? extends QuandlHistoricalTimeSeriesUpdater> clazz = updater.getClass();
    final Method method = clazz.getDeclaredMethod("getCurrentQuandlTimeSeriesDocuments");
    method.setAccessible(true);
    // expect first time series to be selected
    @SuppressWarnings("unchecked")
    final List<HistoricalTimeSeriesInfoDocument> current = (List<HistoricalTimeSeriesInfoDocument>) method.invoke(updater, (Object[]) null);
    assertEquals(current.size(), 1);
    assertEquals(current.get(0), doc1);
  }

  /**
   * Tests the check for an up-to-date time series. This test uses reflection to access the method being tested.
   * @throws NoSuchMethodException  if there is no method called "isUpToDate"
   * @throws InvocationTargetException  if the method called "isUpToDate" cannot be invoked
   * @throws IllegalAccessException  if the method called "isUpToDate" has not been set accessible
   */
  @Test
  public void testIsUpToDate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(new InMemoryHistoricalTimeSeriesMaster(), HTS_PROVIDER);
    final Class<? extends QuandlHistoricalTimeSeriesUpdater> clazz = updater.getClass();
    final Method method = clazz.getDeclaredMethod("isUpToDate", LocalDate.class, String.class);
    method.setAccessible(true);
    // tested date is after previous week day
    assertTrue((boolean) method.invoke(null, LocalDate.now(), HistoricalTimeSeriesConstants.LONDON_CLOSE));
    // tested date is previous week date
    assertTrue((boolean) method.invoke(null, DateUtils.previousWeekDay(), HistoricalTimeSeriesConstants.LONDON_CLOSE));
    // tested date is previous week date plus one for Tokyo
    assertTrue((boolean) method.invoke(null, DateUtils.previousWeekDay().plusDays(1), HistoricalTimeSeriesConstants.TOKYO_CLOSE));
    // tested date is previous week date but observation time is Tokyo close
    assertFalse((boolean) method.invoke(null, DateUtils.previousWeekDay(), HistoricalTimeSeriesConstants.TOKYO_CLOSE));
    // tested date is previous week date minus a week
    assertFalse((boolean) method.invoke(null, DateUtils.previousWeekDay().minusDays(7), HistoricalTimeSeriesConstants.LONDON_CLOSE));
  }

  /**
   * Tests the behaviour when an update is requested for a time series that has not been loaded into the master.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testCheckForUpdatesTsNotInMaster() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument doc1 = new HistoricalTimeSeriesInfoDocument(HTS_INFO1);
    doc1.setUniqueId(UniqueId.of(InMemoryHistoricalTimeSeriesMaster.DEFAULT_OID_SCHEME, "1"));
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, HTS_PROVIDER);
    final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> tsRequest = new HashMap<>();
    final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap = new HashMap<>();
    updater.checkForUpdates(doc1, metaDataKeyMap, tsRequest);
  }

  /**
   * Tests that the time series that should be updated are correctly identified and that the correct fields will be used.
   */
  @Test
  public void testIdentifyTimeSeries1() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO1));
    master.updateTimeSeriesDataPoints(doc1.getObjectId(), TS1);
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO2));
    master.updateTimeSeriesDataPoints(doc2.getObjectId(), TS2);
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO3));
    master.updateTimeSeriesDataPoints(doc3.getObjectId(), TS3);
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO4));
    master.updateTimeSeriesDataPoints(doc4.getObjectId(), TS4);
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, HTS_PROVIDER);
    final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> tsRequest = new HashMap<>();
    final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap = new HashMap<>();
    // compare the previous week date with the last date in the time series in the master
    assertFalse(updater.checkForUpdates(doc1, metaDataKeyMap, tsRequest));
    assertFalse(updater.checkForUpdates(doc2, metaDataKeyMap, tsRequest));
    assertTrue(updater.checkForUpdates(doc3, metaDataKeyMap, tsRequest));
    assertTrue(updater.checkForUpdates(doc4, metaDataKeyMap, tsRequest));
    // both time series need to be updated from the same date
    assertEquals(tsRequest.size(), 1);
    LocalDate dateToUpdateFrom = LocalDate.now().minusDays(7);
    if (dateToUpdateFrom.getDayOfWeek() == DayOfWeek.SATURDAY || dateToUpdateFrom.getDayOfWeek() == DayOfWeek.SUNDAY) {
      dateToUpdateFrom = DateUtils.nextWeekDay(dateToUpdateFrom);
    }
    assertEquals(Iterables.getOnlyElement(tsRequest.keySet()), DateUtils.previousWeekDay(dateToUpdateFrom));
    // the series to be updated have the same data provider
    assertEquals(Iterables.getOnlyElement(tsRequest.values()).size(), 1);
    final Map<String, Map<String, Set<ExternalIdBundle>>> providerToFieldToIds = Iterables.getOnlyElement(tsRequest.values());
    assertEquals(Iterables.getOnlyElement(providerToFieldToIds.keySet()), QuandlHistoricalTimeSeriesLoader.DEFAULT_DATA_PROVIDER);
    // the series to be updated have the same data field
    final Map<String, Set<ExternalIdBundle>> fieldToIds = Iterables.getOnlyElement(providerToFieldToIds.values());
    assertEquals(fieldToIds.size(), 1);
    assertEquals(Iterables.getOnlyElement(fieldToIds.keySet()), QuandlConstants.RATE_FIELD_NAME);
    assertEquals(Iterables.getOnlyElement(fieldToIds.values()), Sets.newHashSet(ID3.toBundle(), ID4.toBundle()));
    assertEquals(metaDataKeyMap.size(), 2);
  }

  /**
   * Tests that the time series that should be updated are correctly identified and that the correct fields will be used.
   */
  @Test
  public void testIdentifyTimeSeries2() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO1));
    master.updateTimeSeriesDataPoints(doc1.getObjectId(), TS1);
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO2));
    master.updateTimeSeriesDataPoints(doc2.getObjectId(), TS2);
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO3));
    master.updateTimeSeriesDataPoints(doc3.getObjectId(), TS3);
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO4));
    master.updateTimeSeriesDataPoints(doc4.getObjectId(), TS4);
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, HTS_PROVIDER);
    // all time series will need to be updated
    final LocalDate startDate = ScheduleCalculator.getAdjustedDate(DateUtils.previousWeekDay().plusDays(7), 0, new MondayToFridayCalendar("Weekend"));
    updater.setStartDate(startDate);
    final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> tsRequest = new HashMap<>();
    final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap = new HashMap<>();
    // compare the previous week date with the last date in the time series in the master
    assertTrue(updater.checkForUpdates(doc1, metaDataKeyMap, tsRequest));
    assertTrue(updater.checkForUpdates(doc2, metaDataKeyMap, tsRequest));
    assertTrue(updater.checkForUpdates(doc3, metaDataKeyMap, tsRequest));
    assertTrue(updater.checkForUpdates(doc4, metaDataKeyMap, tsRequest));
    // time series need to be updated from the same date
    assertEquals(tsRequest.size(), 1);
    assertEquals(Iterables.getOnlyElement(tsRequest.keySet()), startDate);
    // the series to be updated have the same data provider
    assertEquals(Iterables.getOnlyElement(tsRequest.values()).size(), 1);
    final Map<String, Map<String, Set<ExternalIdBundle>>> providerToFieldToIds = Iterables.getOnlyElement(tsRequest.values());
    assertEquals(Iterables.getOnlyElement(providerToFieldToIds.keySet()), QuandlHistoricalTimeSeriesLoader.DEFAULT_DATA_PROVIDER);
    // the series to be updated have different data fields
    final Map<String, Set<ExternalIdBundle>> fieldToIds = Iterables.getOnlyElement(providerToFieldToIds.values());
    assertEquals(fieldToIds.size(), 2);
    assertEquals(fieldToIds.keySet(), Sets.newHashSet(QuandlConstants.RATE_FIELD_NAME, QuandlConstants.VALUE_FIELD_NAME));
    assertEquals(fieldToIds.get(QuandlConstants.RATE_FIELD_NAME), Sets.newHashSet(ID1.toBundle(), ID3.toBundle(), ID4.toBundle()));
    assertEquals(fieldToIds.get(QuandlConstants.VALUE_FIELD_NAME), Collections.<ExternalIdBundle>singleton(ID2.toBundle()));
    assertEquals(metaDataKeyMap.size(), 4);
  }

  /**
   * Tests the behaviour when a time series is requested that is not available from the data provider. No exceptions are expected.
   */
  @Test
  public void testTsNotAvailable() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final MockHistoricalTimeSeriesProvider provider = new MockHistoricalTimeSeriesProvider(Collections.<ExternalIdBundle, LocalDateDoubleTimeSeries>emptyMap());
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, provider);
    updater.updateTimeSeries();
  }

  /**
   * Tests the behaviour when the time series in the master that is to be updated is empty.
   */
  @Test
  public void testTsInMasterIsEmpty() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument doc = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO1));
    master.updateTimeSeriesDataPoints(doc.getObjectId(), ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    final MockHistoricalTimeSeriesProvider provider = new MockHistoricalTimeSeriesProvider(Collections.singletonMap(ID1.toBundle(), TS1));
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, provider);
    updater.updateTimeSeries();
    assertEquals(master.getTimeSeries(doc.getUniqueId()).getTimeSeries(), TS1);
  }

  /**
   * Tests the behaviour when the time series from the provider does not contain any data.
   */
  @Test
  public void testTsInProviderIsEmpty() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument doc = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO3));
    master.updateTimeSeriesDataPoints(doc.getObjectId(), TS3);
    final MockHistoricalTimeSeriesProvider provider = new MockHistoricalTimeSeriesProvider(
        Collections.<ExternalIdBundle, LocalDateDoubleTimeSeries>singletonMap(ID3.toBundle(), ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES));
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, provider);
    updater.updateTimeSeries();
    assertEquals(master.getTimeSeries(doc.getUniqueId()).getTimeSeries(), TS3);
  }

  /**
   * Tests the behaviour when the time series from the provider does not contain any relevant data to add to the original
   * series.
   */
  @Test
  public void testTsInProviderHasNoData() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument doc = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO4));
    master.updateTimeSeriesDataPoints(doc.getObjectId(), TS3);
    final MockHistoricalTimeSeriesProvider provider = new MockHistoricalTimeSeriesProvider(
        Collections.<ExternalIdBundle, LocalDateDoubleTimeSeries>singletonMap(ID4.toBundle(),
            TS4.subSeries(TS4.getEarliestTime(), TS3.getLatestTime().minusDays(1))));
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, provider);
    updater.updateTimeSeries();
    assertEquals(master.getTimeSeries(doc.getUniqueId()).getTimeSeries(), TS3);
  }

  /**
   * Tests that time series are updated.
   */
  @Test
  public void testUpdateTimeSeries() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument doc1 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO1));
    master.updateTimeSeriesDataPoints(doc1.getObjectId(), TS1);
    final HistoricalTimeSeriesInfoDocument doc2 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO2));
    master.updateTimeSeriesDataPoints(doc2.getObjectId(), TS2);
    final HistoricalTimeSeriesInfoDocument doc3 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO3));
    master.updateTimeSeriesDataPoints(doc3.getObjectId(), TS3);
    final HistoricalTimeSeriesInfoDocument doc4 = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO4));
    master.updateTimeSeriesDataPoints(doc4.getObjectId(), TS4);
    final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> ts = new HashMap<>();
    ts.put(ID1.toBundle(), TS1);
    ts.put(ID2.toBundle(), TS2);
    ts.put(ID3.toBundle(), TS1);
    ts.put(ID4.toBundle(), TS2);
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, new MockHistoricalTimeSeriesProvider(ts));
    // third and fourth time series should be updated
    final LocalDate endDate = DateUtils.previousWeekDay(END_DATE.minusDays(3));
    updater.setEndDate(endDate);
    updater.updateTimeSeries();
    assertEquals(master.getTimeSeries(doc1.getUniqueId()).getTimeSeries(), TS1);
    assertEquals(master.getTimeSeries(doc2.getUniqueId()).getTimeSeries(), TS2);
    final LocalDateDoubleTimeSeries updatedTs3 = master.getTimeSeries(doc3.getUniqueId()).getTimeSeries();
    final LocalDateDoubleTimeSeries updatedTs4 = master.getTimeSeries(doc4.getUniqueId()).getTimeSeries();
    // previous values in time series
    assertEquals(updatedTs3.subSeries(TS3.getEarliestTime(), TS3.getLatestTime()), TS3.subSeries(TS3.getEarliestTime(), TS3.getLatestTime()));
    assertEquals(updatedTs4.subSeries(TS4.getEarliestTime(), TS4.getLatestTime()), TS4.subSeries(TS4.getEarliestTime(), TS4.getLatestTime()));
    // time series appended with new values
    assertEquals(updatedTs3.subSeries(TS3.getLatestTime().plusDays(1), endDate), TS1.subSeries(TS3.getLatestTime().plusDays(1), endDate));
    assertEquals(updatedTs4.subSeries(TS4.getLatestTime().plusDays(1), endDate), TS2.subSeries(TS4.getLatestTime().plusDays(1), endDate));
  }

  /**
   * Tests that time series are reloaded even if they are up to date if the flag is set.
   */
  @Test
  public void testReloadTimeSeries() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    // third time series finishes a week ago
    final HistoricalTimeSeriesInfoDocument doc = master.add(new HistoricalTimeSeriesInfoDocument(HTS_INFO3));
    master.updateTimeSeriesDataPoints(doc.getObjectId(), TS3);
    final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> ts = new HashMap<>();
    // new time series values from provider
    ts.put(ID3.toBundle(), TS2);
    final QuandlHistoricalTimeSeriesUpdater updater = new QuandlHistoricalTimeSeriesUpdater(master, new MockHistoricalTimeSeriesProvider(ts));
    updater.setReload(true);
    updater.updateTimeSeries();
    assertEquals(master.getTimeSeries(doc.getUniqueId()).getTimeSeries(), TS2);
  }
}
