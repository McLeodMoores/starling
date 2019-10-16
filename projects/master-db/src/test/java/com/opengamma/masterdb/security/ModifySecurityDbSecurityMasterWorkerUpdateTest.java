/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.elsql.ElSqlConfig;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifySecurityDbSecurityMasterWorkerUpdateTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorkerUpdateTest.class);

  /**
   * @param databaseType
   *          the database type
   * @param databaseVersion
   *          the database version
   */
  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifySecurityDbSecurityMasterWorkerUpdateTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, false);
    LOGGER.info("running testcases for {}", databaseType);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpdateSecurityNullDocument() {
    _secMaster.update(null);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpdateNoSecurityId() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "101");
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    _secMaster.update(doc);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpdateNoSecurity() {
    final SecurityDocument doc = new SecurityDocument();
    doc.setUniqueId(UniqueId.of("DbSec", "101", "0"));
    _secMaster.update(doc);
  }

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testUpdateNotFound() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "0", "0");
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.update(doc);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpdateNotLatestVersion() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "201", "0");
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.update(doc);
  }

  /**
   *
   */
  @Test
  public void testUpdateGetUpdateGet() {
    final Instant now = Instant.now(_secMaster.getClock());

    final UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    final SecurityDocument base = _secMaster.get(uniqueId);
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument input = new SecurityDocument(security);

    final SecurityDocument updated = _secMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getSecurity(), updated.getSecurity());

    final SecurityDocument old = _secMaster.get(uniqueId);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant()); // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getSecurity(), old.getSecurity());

    final SecurityHistoryRequest search = new SecurityHistoryRequest(base.getUniqueId(), null, now);
    search.setFullDetail(false);
    final SecurityHistoryResult searchResult = _secMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  /**
   * @throws Exception
   *           if there is a problem
   */
  @Test
  public void testUpdatePermissions() throws Exception {
    _secMaster.setClock(OpenGammaClock.getInstance());

    final UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    final SecurityDocument baseDoc = _secMaster.get(uniqueId);
    assertNotNull(baseDoc);
    final ManageableSecurity baseSecurity = baseDoc.getSecurity();
    assertNotNull(baseSecurity);
    assertNotNull(baseSecurity.getRequiredPermissions());
    assertTrue(baseSecurity.getRequiredPermissions().isEmpty());

    final SecurityDocument input = new SecurityDocument(baseSecurity.clone());
    input.getSecurity().getRequiredPermissions().add("A");
    input.getSecurity().getRequiredPermissions().add("B");
    baseDoc.getSecurity().setRequiredPermissions(Sets.newHashSet("A", "B"));

    Thread.sleep(100);
    SecurityDocument updated = _secMaster.update(baseDoc);
    assertNotNull(updated);
    ManageableSecurity updatedSecurity = updated.getSecurity();
    assertNotNull(updatedSecurity);
    assertNotNull(updatedSecurity.getRequiredPermissions());
    assertEquals(2, updatedSecurity.getRequiredPermissions().size());
    assertTrue(updatedSecurity.getRequiredPermissions().contains("A"));
    assertTrue(updatedSecurity.getRequiredPermissions().contains("B"));
    assertEquals(baseSecurity.getName(), updatedSecurity.getName());
    assertEquals(baseSecurity.getSecurityType(), updatedSecurity.getSecurityType());
    assertEquals(baseSecurity.getExternalIdBundle(), updatedSecurity.getExternalIdBundle());

    assertEquals(updatedSecurity, _secMaster.get(updated.getUniqueId()).getSecurity());

    updated.getSecurity().setRequiredPermissions(Sets.newHashSet("C", "D", "E"));
    Thread.sleep(100);
    updated = _secMaster.update(updated);
    assertNotNull(updated);
    updatedSecurity = updated.getSecurity();
    assertNotNull(updatedSecurity);
    assertNotNull(updatedSecurity.getRequiredPermissions());
    assertEquals(3, updatedSecurity.getRequiredPermissions().size());
    assertTrue(updatedSecurity.getRequiredPermissions().contains("C"));
    assertTrue(updatedSecurity.getRequiredPermissions().contains("D"));
    assertTrue(updatedSecurity.getRequiredPermissions().contains("E"));
    assertEquals(baseSecurity.getName(), updatedSecurity.getName());
    assertEquals(baseSecurity.getSecurityType(), updatedSecurity.getSecurityType());
    assertEquals(baseSecurity.getExternalIdBundle(), updatedSecurity.getExternalIdBundle());

    assertEquals(updatedSecurity, _secMaster.get(updated.getUniqueId()).getSecurity());

    final SecurityHistoryRequest search = new SecurityHistoryRequest(baseSecurity.getUniqueId(), null, Instant.now(_secMaster.getClock()));
    search.setFullDetail(false);
    final SecurityHistoryResult searchResult = _secMaster.history(search);
    assertEquals(3, searchResult.getDocuments().size());
  }

  /**
   *
   */
  @Test
  public void testUpdatRollback() {
    final DbSecurityMaster w = new DbSecurityMaster(_secMaster.getDbConnector());
    w.setElSqlBundle(ElSqlBundle.of(new ElSqlConfig("TestRollback"), DbSecurityMaster.class));
    final SecurityDocument base = _secMaster.get(UniqueId.of("DbSec", "101", "0"));
    final UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument input = new SecurityDocument(security);
    try {
      w.update(input);
      Assert.fail();
    } catch (final BadSqlGrammarException ex) {
      // expected
    }
    final SecurityDocument test = _secMaster.get(UniqueId.of("DbSec", "101", "0"));

    assertEquals(base, test);
  }

}
