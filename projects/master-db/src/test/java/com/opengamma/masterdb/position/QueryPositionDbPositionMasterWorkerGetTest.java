/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryPositionDbPositionMasterWorkerGetTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPositionDbPositionMasterWorkerGetTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_get_nullUID() {
    _posMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundId() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "0", "0");
    _posMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_versioned_notFoundVersion() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "121", "1");
    _posMaster.get(uniqueId);
  }

  @Test
  public void test_getPosition_versioned_oneSecurityKey() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "122", "0");
    final PositionDocument test = _posMaster.get(uniqueId);
    assert122(test);
  }

  @Test
  public void test_getPosition_versioned_twoSecurityKeys() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "121", "0");
    final PositionDocument test = _posMaster.get(uniqueId);
    assert121(test);
  }

  @Test
  public void test_getPosition_versioned_notLatest() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "221", "0");
    final PositionDocument test = _posMaster.get(uniqueId);
    assert221(test);
  }

  @Test
  public void test_getPosition_versioned_latest() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "221", "1");
    final PositionDocument test = _posMaster.get(uniqueId);
    assert222(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getPosition_unversioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "0");
    _posMaster.get(uniqueId);
  }

  @Test
  public void test_getPosition_unversioned() {
    final UniqueId oid = UniqueId.of("DbPos", "221");
    final PositionDocument test = _posMaster.get(oid);
    assert222(test);
  }

}
