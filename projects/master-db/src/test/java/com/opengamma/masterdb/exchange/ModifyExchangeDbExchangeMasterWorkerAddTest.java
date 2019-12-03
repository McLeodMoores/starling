/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyExchangeDbExchangeMasterWorkerAddTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerAddTest.class);
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");
  private static final ExternalIdBundle REGION = ExternalIdBundle.of("C", "D");

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyExchangeDbExchangeMasterWorkerAddTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, false);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addExchange_nullDocument() {
    _exgMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noExchange() {
    final ExchangeDocument doc = new ExchangeDocument();
    _exgMaster.add(doc);
  }

  @Test
  public void test_add() {
    final Instant now = Instant.now(_exgMaster.getClock());

    final ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    final ExchangeDocument doc = new ExchangeDocument(exchange);
    final ExchangeDocument test = _exgMaster.add(doc);

    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbExg", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageableExchange testExchange = test.getExchange();
    assertNotNull(testExchange);
    assertEquals(uniqueId, testExchange.getUniqueId());
    assertEquals("Test", test.getName());
    assertEquals(BUNDLE, testExchange.getExternalIdBundle());
    assertEquals(REGION, testExchange.getRegionIdBundle());
    assertEquals(null, testExchange.getTimeZone());
  }

  @Test
  public void test_add_addThenGet() {
    final ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    final ExchangeDocument doc = new ExchangeDocument(exchange);
    final ExchangeDocument added = _exgMaster.add(doc);

    final ExchangeDocument test = _exgMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingNameProperty() {
    final ManageableExchange exchange = new ManageableExchange();
    final ExchangeDocument doc = new ExchangeDocument(exchange);
    _exgMaster.add(doc);
  }

  @Test
  public void test_add_addWithMinimalProperties() {
    final ManageableExchange exchange = new ManageableExchange();
    exchange.setName("Test");
    final ExchangeDocument doc = new ExchangeDocument(exchange);
    _exgMaster.add(doc);
  }

}
