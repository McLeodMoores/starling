/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static org.testng.Assert.assertTrue;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.rest.BatchRunSearchRequest;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbBatchSearchTest extends AbstractDbBatchMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(DbBatchSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchSearchTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------

  @Test
  public void testSearchBatchByMarketSnapshotUid() {
    final BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setMarketDataUid(_marketDataSnapshotUid);
    final Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() > 0);
  }

  @Test
  public void testSearchBatchByMarketSnapshotUidNoResults() {
    final UniqueId nonExistentUid = UniqueId.of("MrkDta", "non_existent_market_data_snapshot_uid");
    final BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setMarketDataUid(nonExistentUid);
    final Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() == 0);
  }

  @Test
  public void testSearchBatchByVersionCorrection() {
    final BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setVersionCorrection(_versionCorrection);
    final Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() > 0);
  }

  @Test
  public void testSearchBatchByVersionCorrectionNoResults() {
    final BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setVersionCorrection(VersionCorrection.of(Instant.now().minus(Duration.ofHours(3)), Instant.now()));
    final Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() == 0);
  }

  @Test
  public void testSearchBatchByValuationTime() {
    final BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setValuationTime(_valuationTime);
    final Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() > 0);
  }

  @Test
  public void testSearchBatchByValuationTimeNoResults() {
    final BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setValuationTime(Instant.now());
    final Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() == 0);
  }

  @Test
  public void testSearchBatchByViewDefinition() {
    final BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setViewDefinitionUid(_viewDefinitionUid);
    final Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() > 0);
  }

  @Test
  public void testSearchBatchByViewDefinitionNoResults() {
    final UniqueId nonExistentUid = UniqueId.of("ViewDef", "non_existent_view_definition_uid");
    final BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setViewDefinitionUid(nonExistentUid);
    final Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() == 0);
  }
}
