/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifySecurityDbSecurityMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifySecurityDbSecurityMasterWorkerAddTest extends AbstractDbSecurityMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(ModifySecurityDbSecurityMasterWorkerAddTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifySecurityDbSecurityMasterWorkerAddTest(final String databaseType, final String databaseVersion) {
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
  public void test_add_addWithMissingNameProperty() {
    final ManageableSecurity security = mock(ManageableSecurity.class);
    when(security.getSecurityType()).thenReturn("MANAGEABLE");
    when(security.getExternalIdBundle()).thenReturn(ExternalIdBundle.of("A", "B"));
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingExternalIdBundleProperty() {
    final ManageableSecurity security = mock(ManageableSecurity.class);
    when(security.getSecurityType()).thenReturn("MANAGEABLE");
    when(security.getName()).thenReturn("Test");
    final SecurityDocument doc = new SecurityDocument(security);
    _secMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingTypeProperty() {
    final ManageableSecurity security = mock(ManageableSecurity.class);
    when(security.getName()).thenReturn("Test");
    when(security.getExternalIdBundle()).thenReturn(ExternalIdBundle.of("A", "B"));
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

  @Test
  public void test_addWithPermission_add() {
    final Instant now = Instant.now(_secMaster.getClock());

    final ManageableSecurity security = new ManageableSecurity(null, "TestSecurity", "EQUITY", ExternalIdBundle.of("A", "B"));
    security.getRequiredPermissions().add("A");
    security.getRequiredPermissions().add("B");
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
    assertNotNull(security.getRequiredPermissions());
    assertEquals(2, security.getRequiredPermissions().size());
    assertTrue(security.getRequiredPermissions().contains("A"));
    assertTrue(security.getRequiredPermissions().contains("B"));
  }

}
