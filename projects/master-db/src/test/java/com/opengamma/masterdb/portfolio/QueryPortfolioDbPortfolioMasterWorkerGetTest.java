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
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryPortfolioDbPortfolioMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryPortfolioDbPortfolioMasterWorkerGetTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryPortfolioDbPortfolioMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPortfolioDbPortfolioMasterWorkerGetTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_get_nullUID() {
    _prtMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundId() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "0", "0");
    _prtMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundVersion() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "101", "1");
    _prtMaster.get(uniqueId);
  }

  @Test
  public void test_get_versioned() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "101", "0");
    final PortfolioDocument test = _prtMaster.get(uniqueId);
    assert101(test, 999);
  }

  @Test
  public void test_get_versioned_notLatest() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "201", "0");
    final PortfolioDocument test = _prtMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_get_versioned_latest() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "201", "1");
    final PortfolioDocument test = _prtMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_unversioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbPrt", "0");
    _prtMaster.get(uniqueId);
  }

  @Test
  public void test_get_unversioned_latest() {
    final UniqueId oid = UniqueId.of("DbPrt", "201");
    final PortfolioDocument test = _prtMaster.get(oid);
    assert202(test);
  }

  @Test
  public void test_get_unversioned_nodesLoaded() {
    final UniqueId oid = UniqueId.of("DbPrt", "101");
    final PortfolioDocument test = _prtMaster.get(oid);
    assert101(test, 999);
  }

}
