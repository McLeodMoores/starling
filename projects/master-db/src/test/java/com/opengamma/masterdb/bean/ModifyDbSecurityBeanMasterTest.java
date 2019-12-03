/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.bean;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.elsql.ElSqlConfig;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.masterdb.security.DbSecurityBeanMaster;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyDbSecurityBeanMasterTest extends AbstractDbSecurityBeanMasterTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(ModifyDbSecurityBeanMasterTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyDbSecurityBeanMasterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, false);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addSecurity_nullDocument() {
    _secMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noSecurity() {
    final SecurityDocument doc = new SecurityDocument();
    _secMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    final Instant now = Instant.now(_secMaster.getClock());

    final ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    final SecurityDocument test = _secMaster.add(doc);

    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbSec", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageableSecurity testSecurity = test.getSecurity();
    assertNotNull(testSecurity);
    assertEquals(uniqueId, testSecurity.getUniqueId());
    assertEquals("TestSecurity", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    final ExternalIdBundle idKey = security.getExternalIdBundle();
    assertNotNull(idKey);
    assertEquals(1, idKey.size());
    assertEquals(ExternalId.of("A", "B"), idKey.getExternalIds().iterator().next());
  }

  @Test
  public void test_add_addThenGet() {
    final ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    final SecurityDocument added = _secMaster.add(doc);

    final SecurityDocument test = _secMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingNameProperty() throws Exception {
    final ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", ExternalIdBundle.of("A", "B"));
    final Field field = ManageableSecurity.class.getDeclaredField("_name");
    field.setAccessible(true);
    field.set(security, null);
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingExternalIdBundleProperty() throws Exception {
    final ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", ExternalIdBundle.of("A", "B"));
    final Field field = ManageableSecurity.class.getDeclaredField("_externalIdBundle");
    field.setAccessible(true);
    field.set(security, null);
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingTypeProperty() throws Exception {
    final ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", ExternalIdBundle.of("A", "B"));
    final Field field = ManageableSecurity.class.getDeclaredField("_securityType");
    field.setAccessible(true);
    field.set(security, null);
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.add(doc);
  }

  @Test
  public void test_add_addWithMinimalProperties() {
    final ManageableSecurity security = new ManageableSecurity();
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.add(doc);
  }

  @Test
  public void test_add_searchByAttribute() {
    final ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", ExternalIdBundle.of("A", "B"));
    security.addAttribute("city", "London");
    security.addAttribute("office", "Southern");
    final SecurityDocument added = _secMaster.add(new SecurityDocument(security));

    final ManageableSecurity security2 = new ManageableSecurity(null, "TestSecurity2", "EQUITY", ExternalIdBundle.of("A", "B"));
    security2.addAttribute("office", "Southern");
    final SecurityDocument added2 = _secMaster.add(new SecurityDocument(security2));

    SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    searchRequest.addAttribute("city", "London");
    SecuritySearchResult searchResult = _secMaster.search(searchRequest);
    assertEquals(1, searchResult.getDocuments().size());
    assertEquals(added, searchResult.getDocuments().get(0));

    searchRequest = new SecuritySearchRequest();
    searchRequest.setSortOrder(SecuritySearchSortOrder.NAME_ASC);
    searchRequest.addAttribute("office", "Southern");
    searchResult = _secMaster.search(searchRequest);
    assertEquals(2, searchResult.getDocuments().size());
    assertEquals(added, searchResult.getDocuments().get(0));
    assertEquals(added2, searchResult.getDocuments().get(1));

    searchRequest = new SecuritySearchRequest();
    searchRequest.addAttribute("city", "London");
    searchRequest.addAttribute("office", "*thern");
    searchResult = _secMaster.search(searchRequest);
    assertEquals(1, searchResult.getDocuments().size());
    assertEquals(added, searchResult.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_updateSecurity_nullDocument() {
    _secMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noSecurityId() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "101");
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    _secMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noSecurity() {
    final SecurityDocument doc = new SecurityDocument();
    doc.setUniqueId(UniqueId.of("DbSec", "101", "0"));
    _secMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "0", "0");
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "201", "0");
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
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
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getSecurity(), old.getSecurity());

    final SecurityHistoryRequest search = new SecurityHistoryRequest(base.getUniqueId(), null, now);
    search.setFullDetail(false);
    final SecurityHistoryResult searchResult = _secMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    final DbSecurityBeanMaster w = new DbSecurityBeanMaster(_secMaster.getDbConnector());
    w.setElSqlBundle(ElSqlBundle.of(new ElSqlConfig("TestRollback"), DbBeanMaster.class));
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

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correctSecurity_nullDocument() {
    _secMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noSecurityId() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "101");
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument();
    doc.setSecurity(security);
    _secMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noSecurity() {
    final SecurityDocument doc = new SecurityDocument();
    doc.setUniqueId(UniqueId.of("DbSec", "101", "0"));
    _secMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "0", "0");
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.correct(doc);
  }

//  @Test(expected = IllegalArgumentException.class)
//  public void test_correct_notLatestCorrection() {
//    UniqueId uniqueId = UniqueId("DbSec", "201", "0");
//    DefaultSecurity security = new DefaultSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
//    SecurityDocument doc = new SecurityDocument(security);
//    _worker.correct(doc);
//  }

  @Test
  public void test_correct_getUpdateGet() {
    final Instant now = Instant.now(_secMaster.getClock());

    final UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    final SecurityDocument base = _secMaster.get(uniqueId);
    final ManageableSecurity security = new ManageableSecurity(uniqueId, "Name", "Type", ExternalIdBundle.of("A", "B"));
    final SecurityDocument input = new SecurityDocument(security);

    final SecurityDocument corrected = _secMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getSecurity(), corrected.getSecurity());

    final SecurityDocument old = _secMaster.get(UniqueId.of("DbSec", "101", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getSecurity(), old.getSecurity());

    final SecurityHistoryRequest search = new SecurityHistoryRequest(base.getUniqueId(), now, null);
    search.setFullDetail(false);
    final SecurityHistoryResult searchResult = _secMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeSecurity_versioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "0", "0");
    _secMaster.remove(uniqueId);
  }

  @Test
  public void test_remove_removed() {
    final Instant now = Instant.now(_secMaster.getClock());

    final UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    _secMaster.remove(uniqueId);
    final SecurityDocument test = _secMaster.get(uniqueId);

    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uniqueId, security.getUniqueId());
    assertEquals("TestSecurity101", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F")), security.getExternalIdBundle());
  }

}
