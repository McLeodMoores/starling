/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.bean;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.masterdb.convention.DbConventionBeanMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbConventionBeanMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbConventionBeanMasterTest extends AbstractDbTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbConventionBeanMasterTest.class);

  private DbConventionBeanMaster _cnvMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbConventionBeanMasterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _cnvMaster = new DbConventionBeanMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _cnvMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_cnvMaster);
    assertEquals(true, _cnvMaster.getUniqueIdScheme().equals("DbCnv"));
    assertNotNull(_cnvMaster.getDbConnector());
    assertNotNull(_cnvMaster.getClock());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_addAndGet() throws Exception {
    final MockConvention cnv = new MockConvention("London", ExternalIdBundle.of("Test", "OG"), Currency.GBP);
    final ConventionDocument addDoc = new ConventionDocument(cnv);
    final ConventionDocument added = _cnvMaster.add(addDoc);

    final ConventionDocument loaded = _cnvMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test(enabled = false)
  public void test_concurrentModification() {
    final AtomicReference<Throwable> exceptionOccurred = new AtomicReference<>();
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        try {
          test_addAndGet();
        } catch (final Throwable th) {
          exceptionOccurred.compareAndSet(null, th);
        }
      }
    };

    // 5 threads for plenty of concurrent activity
    final ExecutorService executor = Executors.newFixedThreadPool(5);

    // 10 security inserts is always enough to produce a duplicate key exception
    final LinkedList<Future<?>> futures = new LinkedList<>();
    for (int i = 0; i < 10; i++) {
      futures.add(executor.submit(task));
    }

    while (!futures.isEmpty()) {
      final Future<?> future = futures.poll();
      try {
        future.get();
      } catch (final Throwable t) {
        LOGGER.error("Exception waiting for task to complete", t);
      }
    }

    assertEquals(null, exceptionOccurred.get());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbConventionBeanMaster[DbCnv]", _cnvMaster.toString());
  }

}
