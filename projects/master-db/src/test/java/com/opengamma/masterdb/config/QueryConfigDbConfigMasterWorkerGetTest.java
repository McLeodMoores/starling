/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryConfigDbConfigMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryConfigDbConfigMasterWorkerGetTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryConfigDbConfigMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryConfigDbConfigMasterWorkerGetTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getConfig_nullUID() {
    _cfgMaster.get((UniqueId) null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConfig_versioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "0", "0");
    _cfgMaster.get(uniqueId);
  }

  @Test
  public void test_getConfig_versioned_oneConfigKey() {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    final ConfigDocument test = _cfgMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getConfig_versioned_twoConfigKeys() {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "102", "0");
    final ConfigDocument test = _cfgMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getConfig_versioned_notLatest() {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "201", "0");
    final ConfigDocument test = _cfgMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getConfig_versioned_latest() {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "201", "1");
    final ConfigDocument test = _cfgMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConfig_unversioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "0");
    _cfgMaster.get(uniqueId);
  }

  @Test
  public void test_getConfig_unversioned() {
    final UniqueId uid = UniqueId.of("DbCfg", "201");
    final ConfigDocument test = _cfgMaster.get(uid);
    assert202(test);
  }

  @Test
  public void test_get_noType() {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    final ConfigDocument test = _cfgMaster.get(uniqueId);
    assertNotNull(test);
    if (test.getConfig().getValue() instanceof ExternalId) {
      assertEquals(test.getType(), ExternalId.class);
      assert101(test);
    } else {
      Assert.fail();
    }
  }

}
