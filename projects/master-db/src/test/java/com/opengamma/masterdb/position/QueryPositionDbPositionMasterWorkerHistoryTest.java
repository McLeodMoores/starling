/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryPositionDbPositionMasterWorkerHistoryTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerHistoryTest.class);

  /**
   * @param databaseType
   *          the database type
   * @param databaseVersion
   *          the database version
   */
  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPositionDbPositionMasterWorkerHistoryTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test
  public void testSearchPositionHistoricDocuments() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  /**
   *
   */
  @Test
  public void testSearchPositionHistoricDocumentCountWhenMultipleSecurities() {
    final ObjectId oid = ObjectId.of("DbPos", "121");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());
    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  /**
   *
   */
  @Test
  public void testSearchPositionHistoricDocumentCountWhenMultipleSecuritiesAndMultipleTrades() {
    final ObjectId oid = ObjectId.of("DbPos", "123");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());
    assertEquals(1, test.getDocuments().size());
    assert123(test.getDocuments().get(0));
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test
  public void testSearchPositionHistoricNoInstants() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test
  public void testSearchPositionHistoricNoInstantsPageOne() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setPagingRequest(PagingRequest.ofPage(1, 1));
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
  }

  /**
   *
   */
  @Test
  public void testSearchPositionHistoricNoInstantsPageTwo() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setPagingRequest(PagingRequest.ofPage(2, 1));
    final PositionHistoryResult test = _posMaster.history(request);

    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItemOneBased());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());

    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert221(test.getDocuments().get(0));
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test
  public void testSearchPositionHistoricVersionsFromPreFirst() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  /**
   *
   */
  @Test
  public void testSearchPositionHistoricVersionsFromFirstToSecond() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

  /**
   *
   */
  @Test
  public void testSearchPositionHistoricVersionsFromPostSecond() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test
  public void testSearchPositionHistoricVersionsToPreFirst() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(0, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  /**
   *
   */
  @Test
  public void testSearchPositionHistoricVersionsToFirstToSecond() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert221(test.getDocuments().get(0));
  }

  /**
   *
   */
  @Test
  public void testSearchPositionHistoricVersionsToPostSecond() {
    final ObjectId oid = ObjectId.of("DbPos", "221");
    final PositionHistoryRequest request = new PositionHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    final PositionHistoryResult test = _posMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
    assert221(test.getDocuments().get(1));
  }

}
