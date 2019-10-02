/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests OG-UtilDB clock.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbClockTest extends AbstractDbTest {

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbClockTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  // -------------------------------------------------------------------------
  // public void test_clock_threaded() {
  // for (int i = 0; i < 10; i++) {
  // final int ii = i;
  // Runnable r = new Runnable() {
  // @Override
  // public void run() {
  // while (true) {
  // System.out.println(ii + " " + _connector.now());
  // try {
  // Thread.sleep(200);
  // } catch (InterruptedException ex) {
  // ex.printStackTrace();
  // }
  // }
  // }
  // };
  // new Thread(r).start();
  // }
  // }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testClock() {
    final DbConnector dbConnector = getDbConnector();
    final List<Instant> instants1 = Lists.newArrayList();
    for (int i = 0; i < 50000; i++) {
      instants1.add(dbConnector.now());
    }
    final List<Instant> instants2 = new ArrayList<>(instants1);
    Collections.sort(instants2);
    assertEquals(instants1, instants2);
  }

}
