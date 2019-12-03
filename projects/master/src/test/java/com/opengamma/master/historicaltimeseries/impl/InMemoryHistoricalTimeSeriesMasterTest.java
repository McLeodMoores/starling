/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryHistoricalTimeSeriesMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryHistoricalTimeSeriesMasterTest {

  // TODO Move the logical tests from here to the generic SecurityMasterTestCase then we can just extend from that

  private static final UniqueId OTHER_UID = UniqueId.of("U", "1");
  private static final ExternalId ID1 = ExternalId.of("A", "B");
  private static final ExternalId ID2 = ExternalId.of("A", "C");
  private static final ExternalIdBundle BUNDLE1 = ExternalIdBundle.of(ID1);
  private static final ExternalIdBundle BUNDLE2 = ExternalIdBundle.of(ID2);

  private InMemoryHistoricalTimeSeriesMaster _testEmpty;
  private InMemoryHistoricalTimeSeriesMaster _testPopulated;
  private HistoricalTimeSeriesInfoDocument _doc1;
  private HistoricalTimeSeriesInfoDocument _doc2;
  private ManageableHistoricalTimeSeriesInfo _info1;
  private ManageableHistoricalTimeSeriesInfo _info2;

  /**
   *
   */
  @BeforeMethod
  public void setUp() {
    _testEmpty = new InMemoryHistoricalTimeSeriesMaster(new ObjectIdSupplier("Test"));
    _testPopulated = new InMemoryHistoricalTimeSeriesMaster(new ObjectIdSupplier("Test"));
    _info1 = new ManageableHistoricalTimeSeriesInfo();
    _info1.setName("Name1");
    _info1.setDataField("DF1");
    _info1.setDataSource("DS1");
    _info1.setDataProvider("DP1");
    _info1.setObservationTime("OT1");
    _info1.setExternalIdBundle(ExternalIdBundleWithDates.of(BUNDLE1));
    _doc1 = new HistoricalTimeSeriesInfoDocument();
    _doc1.setInfo(_info1);
    _doc1 = _testPopulated.add(_doc1);
    _info2 = new ManageableHistoricalTimeSeriesInfo();
    _info2.setName("Name2");
    _info2.setDataField("DF2");
    _info2.setDataSource("DS2");
    _info2.setDataProvider("DP2");
    _info2.setObservationTime("OT2");
    _info2.setExternalIdBundle(ExternalIdBundleWithDates.of(BUNDLE2));
    _doc2 = new HistoricalTimeSeriesInfoDocument();
    _doc2.setInfo(_info2);
    _doc2 = _testPopulated.add(_doc2);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullSupplier() {
    new InMemoryHistoricalTimeSeriesMaster((Supplier<ObjectId>) null);
  }

  /**
   *
   */
  public void testDefaultSupplier() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(_info1);
    final HistoricalTimeSeriesInfoDocument added = master.add(doc);
    assertEquals("MemHts", added.getUniqueId().getScheme());
  }

  /**
   *
   */
  public void testAlternateSupplier() {
    final InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster(new ObjectIdSupplier("Hello"));
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(_info1);
    final HistoricalTimeSeriesInfoDocument added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchEmptyMaster() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchResult result = _testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchPopulatedMasterAll() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(_doc1));
    assertEquals(true, docs.contains(_doc2));
  }

  /**
   *
   */
  public void testSearchPopulatedMasterFilterByBundle() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(BUNDLE1);
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(_doc1));
  }

  /**
   *
   */
  public void testSearchPopulatedMasterFilterByBundleBoth() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(BUNDLE1);
    request.addExternalIds(BUNDLE2);
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(_doc1));
    assertEquals(true, docs.contains(_doc2));
  }

  /**
   *
   */
  public void testSearchPopluatedMasterFilterByExternalIdValue() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("B");
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc1));
  }

  /**
   *
   */
  public void testSearchPopluatedMasterFilterByExternalIdValueCase() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("b");
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc1));
  }

  /**
   *
   */
  public void testSearchPopulatedMasterFilterByName() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("*ame2");
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc2));
  }

  /**
   *
   */
  public void testSearchPopulatedMasterFilterByDataField() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataField("DF2");
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc2));
  }

  /**
   *
   */
  public void testSearchPopulatedMasterFilterByDataSource() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataSource("DS2");
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc2));
  }

  /**
   *
   */
  public void testSearchPopulatedMasterFilterByDataProvider() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataProvider("DP2");
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc2));
  }

  /**
   *
   */
  public void testSearchPopulatedMasterFilterByObservationTime() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setObservationTime("OT2");
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc2));
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetEmptyMaster() {
    assertNull(_testEmpty.get(OTHER_UID));
  }

  /**
   *
   */
  public void testGetPopulatedMaster() {
    assertSame(_doc1, _testPopulated.get(_doc1.getUniqueId()));
    assertSame(_doc2, _testPopulated.get(_doc2.getUniqueId()));
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testAddEmptyMaster() {
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(_info1);
    final HistoricalTimeSeriesInfoDocument added = _testEmpty.add(doc);
    assertNotNull(added.getVersionFromInstant());
    assertNotNull(added.getCorrectionFromInstant());
    assertEquals(added.getVersionFromInstant(), added.getCorrectionFromInstant());
    assertEquals("Test", added.getUniqueId().getScheme());
    added.setUniqueId(null);
    added.getInfo().setTimeSeriesObjectId(null);
    assertEquals(_info1, added.getInfo());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testUpdateEmptyMaster() {
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(_info1);
    doc.setUniqueId(OTHER_UID);
    _testEmpty.update(doc);
  }

  /**
   *
   */
  public void testUpdatePopulatedMaster() {
    final HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(_info1);
    doc.setUniqueId(_doc1.getUniqueId());
    final HistoricalTimeSeriesInfoDocument updated = _testPopulated.update(doc);
    assertEquals(_doc1.getUniqueId(), updated.getUniqueId());
    assertNotNull(_doc1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testRemoveEmptyMaster() {
    _testEmpty.remove(OTHER_UID);
  }

  /**
   *
   */
  public void testRemovePopulatedMaster() {
    _testPopulated.remove(_doc1.getUniqueId());
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchResult result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    final List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(_doc2));
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetTSUIDOtherId() {
    _testEmpty.getTimeSeries(OTHER_UID);
  }

  /**
   *
   */
  public void testPointsUpdateCorrect() {
    final LocalDate[] dates = { LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 2) };
    final double[] values = { 1.1d, 2.2d };
    final LocalDateDoubleTimeSeries input = ImmutableLocalDateDoubleTimeSeries.of(dates, values);

    final UniqueId uniqueId = _testPopulated.updateTimeSeriesDataPoints(_doc1.getUniqueId(), input);
    assertEquals(_doc1.getUniqueId().getObjectId(), uniqueId.getObjectId());

    final ManageableHistoricalTimeSeries test = _testPopulated.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(input, test.getTimeSeries());

    final LocalDate[] dates2 = { LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 3) };
    final double[] values2 = { 1.5d, 2.5d };
    final LocalDateDoubleTimeSeries input2 = ImmutableLocalDateDoubleTimeSeries.of(dates2, values2);

    final UniqueId uniqueId2 = _testPopulated.correctTimeSeriesDataPoints(_doc1.getUniqueId(), input2);
    assertEquals(_doc1.getUniqueId().getObjectId(), uniqueId2.getObjectId());

    final LocalDate[] expectedDates = { LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 2), LocalDate.of(2011, 1, 3) };
    final double[] expectedValues = { 1.5d, 2.2d, 2.5d };
    final LocalDateDoubleTimeSeries expected = ImmutableLocalDateDoubleTimeSeries.of(expectedDates, expectedValues);
    final ManageableHistoricalTimeSeries test2 = _testPopulated.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test2.getUniqueId());
    assertEquals(expected, test2.getTimeSeries());
  }

  /**
   *
   */
  public void testPointsUpdateRemove() {
    final LocalDate[] dates = { LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 2) };
    final double[] values = { 1.1d, 2.2d };
    final LocalDateDoubleTimeSeries input = ImmutableLocalDateDoubleTimeSeries.of(dates, values);

    final UniqueId uniqueId = _testPopulated.updateTimeSeriesDataPoints(_doc1.getUniqueId(), input);
    assertEquals(_doc1.getUniqueId().getObjectId(), uniqueId.getObjectId());

    final ManageableHistoricalTimeSeries test = _testPopulated.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(input, test.getTimeSeries());

    final UniqueId uniqueId2 = _testPopulated.removeTimeSeriesDataPoints(_doc1.getUniqueId(), LocalDate.of(2011, 1, 2), null);
    assertEquals(_doc1.getUniqueId().getObjectId(), uniqueId2.getObjectId());

    final LocalDate[] expectedDates = { LocalDate.of(2011, 1, 1) };
    final double[] expectedValues = { 1.1d };
    final LocalDateDoubleTimeSeries expected = ImmutableLocalDateDoubleTimeSeries.of(expectedDates, expectedValues);
    final ManageableHistoricalTimeSeries test2 = _testPopulated.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test2.getUniqueId());
    assertEquals(expected, test2.getTimeSeries());
  }

  /**
   *
   */
  public void testPointsGetFilter() {

    // Set up HTS for comparison purposes
    final LocalDate[] dates = { LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 2), LocalDate.of(2011, 1, 3), LocalDate.of(2011, 1, 5),
        LocalDate.of(2011, 1, 6), LocalDate.of(2011, 1, 8) };
    final double[] values = { 1.0d, 1.1d, 1.3d, 1.2d, 2.2d, 2.0d };
    final LocalDateDoubleTimeSeries input = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    _testPopulated.updateTimeSeriesDataPoints(_doc1.getUniqueId(), input);
    final ManageableHistoricalTimeSeries reference = _testPopulated.getTimeSeries(_doc1.getUniqueId());

    // Get entire series using blank filter
    final HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofAll();
    ManageableHistoricalTimeSeries test = _testPopulated.getTimeSeries(_doc1.getUniqueId(), filter);
    assertEquals(reference.getTimeSeries(), test.getTimeSeries());
    assertEquals(input, test.getTimeSeries());

    // Get filtered by time, open-ended end
    filter.setEarliestDate(reference.getTimeSeries().getTimeAtIndex(1)); // exclude first point
    test = _testPopulated.getTimeSeries(_doc1.getUniqueId(), filter);
    assertEquals(reference.getTimeSeries().size() - 1, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getValueAtIndex(1), test.getTimeSeries().getEarliestValue());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(1), test.getTimeSeries().getEarliestTime());
    assertEquals(reference.getTimeSeries().getValueAtIndex(reference.getTimeSeries().size() - 1), test.getTimeSeries().getLatestValue());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(reference.getTimeSeries().size() - 1), test.getTimeSeries().getLatestTime());

    // Get filtered by time, closed at both ends
    filter.setLatestDate(reference.getTimeSeries().getTimeAtIndex(reference.getTimeSeries().size() - 2)); // exclude last point
    test = _testPopulated.getTimeSeries(_doc1.getUniqueId(), filter);
    assertEquals(reference.getTimeSeries().size() - 2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getValueAtIndex(1), test.getTimeSeries().getEarliestValue());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(1), test.getTimeSeries().getEarliestTime());
    assertEquals(reference.getTimeSeries().getValueAtIndex(reference.getTimeSeries().size() - 2), test.getTimeSeries().getLatestValue());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(reference.getTimeSeries().size() - 2), test.getTimeSeries().getLatestTime());

    // Get filtered by time, open-ended start
    filter.setEarliestDate(null);
    test = _testPopulated.getTimeSeries(_doc1.getUniqueId(), filter);
    assertEquals(reference.getTimeSeries().size() - 1, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getValueAtIndex(0), test.getTimeSeries().getEarliestValue());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(0), test.getTimeSeries().getEarliestTime());
    assertEquals(reference.getTimeSeries().getValueAtIndex(reference.getTimeSeries().size() - 2), test.getTimeSeries().getLatestValue());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(reference.getTimeSeries().size() - 2), test.getTimeSeries().getLatestTime());

    // Get filtered by +ve maxPoints, open-ended start
    filter.setMaxPoints(2); // get earliest two points
    test = _testPopulated.getTimeSeries(_doc1.getUniqueId(), filter);
    assertEquals(2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(0), test.getTimeSeries().getTimeAtIndex(0));
    assertEquals(reference.getTimeSeries().getValueAtIndex(0), test.getTimeSeries().getValueAtIndex(0));
    assertEquals(reference.getTimeSeries().getTimeAtIndex(1), test.getTimeSeries().getTimeAtIndex(1));
    assertEquals(reference.getTimeSeries().getValueAtIndex(1), test.getTimeSeries().getValueAtIndex(1));

    // Get filtered by +ve maxPoints, closed date range
    filter.setEarliestDate(reference.getTimeSeries().getTimeAtIndex(1)); // exclude first point
    test = _testPopulated.getTimeSeries(_doc1.getUniqueId(), filter);
    assertEquals(2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(1), test.getTimeSeries().getTimeAtIndex(0));
    assertEquals(reference.getTimeSeries().getValueAtIndex(1), test.getTimeSeries().getValueAtIndex(0));
    assertEquals(reference.getTimeSeries().getTimeAtIndex(2), test.getTimeSeries().getTimeAtIndex(1));
    assertEquals(reference.getTimeSeries().getValueAtIndex(2), test.getTimeSeries().getValueAtIndex(1));

    // Get filtered by -ve maxPoints, closed date range
    filter.setMaxPoints(-2); // get latest two points
    test = _testPopulated.getTimeSeries(_doc1.getUniqueId(), filter);
    assertEquals(2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(reference.getTimeSeries().size() - 3), test.getTimeSeries().getTimeAtIndex(0));
    assertEquals(reference.getTimeSeries().getValueAtIndex(reference.getTimeSeries().size() - 3), test.getTimeSeries().getValueAtIndex(0));
    assertEquals(reference.getTimeSeries().getTimeAtIndex(reference.getTimeSeries().size() - 2), test.getTimeSeries().getTimeAtIndex(1));
    assertEquals(reference.getTimeSeries().getValueAtIndex(reference.getTimeSeries().size() - 2), test.getTimeSeries().getValueAtIndex(1));

    // Get filtered by -ve maxPoints, open-ended end
    filter.setLatestDate(null);
    test = _testPopulated.getTimeSeries(_doc1.getUniqueId(), filter);
    assertEquals(2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getTimeAtIndex(reference.getTimeSeries().size() - 2), test.getTimeSeries().getTimeAtIndex(0));
    assertEquals(reference.getTimeSeries().getValueAtIndex(reference.getTimeSeries().size() - 2), test.getTimeSeries().getValueAtIndex(0));
    assertEquals(reference.getTimeSeries().getTimeAtIndex(reference.getTimeSeries().size() - 1), test.getTimeSeries().getTimeAtIndex(1));
    assertEquals(reference.getTimeSeries().getValueAtIndex(reference.getTimeSeries().size() - 1), test.getTimeSeries().getValueAtIndex(1));
  }

}
