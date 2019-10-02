/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractDbUpgradeTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the database upgrade scripts.
 */
@Test(groups = TestGroup.UNIT_DB)
public class MasterDbDatabaseUpgradeTest extends AbstractDbUpgradeTest {

  /**
   * @param databaseType
   *          the database type
   * @param masterDB
   *          the master
   * @param targetVersion
   *          the target version
   * @param migrateFromVersion
   *          the previous version
   */
  @Factory(dataProvider = "databasesVersionsForSeparateMasters", dataProviderClass = DbTest.class)
  public MasterDbDatabaseUpgradeTest(final String databaseType, final String masterDB, final int targetVersion, final int migrateFromVersion) {
    super(databaseType, masterDB, targetVersion, migrateFromVersion);
  }

}
