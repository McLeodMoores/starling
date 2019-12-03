/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryPortfolioDbPortfolioMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryPortfolioDbPortfolioMasterWorkerGetNodeTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryPortfolioDbPortfolioMasterWorkerGetNodeTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPortfolioDbPortfolioMasterWorkerGetNodeTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getNode_nullUID() {
    _prtMaster.getNode(null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getNode_versioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "0", "0");
    _prtMaster.getNode(uniqueId);
  }

  @Test
  public void test_getNode_versioned() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "111", "0");
    final ManageablePortfolioNode test = _prtMaster.getNode(uniqueId);
    assertNode111(test, 999, UniqueId.of("DbPrt", "101", "0"));
  }

  @Test
  public void test_getNode_versioned_112() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "112", "0");
    final ManageablePortfolioNode test = _prtMaster.getNode(uniqueId);
    assertNode112(test, 999, UniqueId.of("DbPrt", "101", "0"));
  }

  @Test
  public void test_getNode_versioned_113() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "113", "0");
    final ManageablePortfolioNode test = _prtMaster.getNode(uniqueId);
    assertNode113(test, UniqueId.of("DbPrt", "101", "0"));
  }

  @Test
  public void test_getNode_versioned_notLatest() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "211", "0");
    final ManageablePortfolioNode test = _prtMaster.getNode(uniqueId);
    assertNode211(test, UniqueId.of("DbPrt", "201", "0"));
  }

  @Test
  public void test_getNode_versioned_latest() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "211", "1");
    final ManageablePortfolioNode test = _prtMaster.getNode(uniqueId);
    assertNode212(test, UniqueId.of("DbPrt", "201", "1"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getNode_unversioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "0");
    _prtMaster.getNode(uniqueId);
  }

  @Test
  public void test_getNode_unversioned_latest() {
    final UniqueId oid = UniqueId.of("DbPrt", "211");
    final ManageablePortfolioNode test = _prtMaster.getNode(oid);
    assertNode212(test, UniqueId.of("DbPrt", "201", "1"));
  }

  @Test
  public void test_getNode_unversioned_nodesLoaded() {
    final UniqueId oid = UniqueId.of("DbPrt", "111");
    final ManageablePortfolioNode test = _prtMaster.getNode(oid);
    assertNode111(test, 999, UniqueId.of("DbPrt", "101", "0"));
  }

}
