/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerHistoryTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerHistoryTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerHistoryTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertEquals(3, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(3, test.getPaging().getTotalItems());

    assertEquals(3, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final PagingRequest pr = PagingRequest.ofPage(1, 2);
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    request.setPagingRequest(pr);
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(3, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final PagingRequest pr = PagingRequest.ofPage(2, 2);
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    request.setPagingRequest(pr);
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(3, test.getPaging().getTotalItems());

    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertEquals(3, test.getPaging().getTotalItems());

    assertEquals(3, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertEquals(3, test.getPaging().getTotalItems());

    assertEquals(3, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertEquals(0, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    final ObjectId oid = ObjectId.of("DbHts", "201");
    final HistoricalTimeSeriesInfoHistoryRequest request = new HistoricalTimeSeriesInfoHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    final HistoricalTimeSeriesInfoHistoryResult test = _htsMaster.history(request);

    assertEquals(3, test.getPaging().getTotalItems());

    assertEquals(3, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
