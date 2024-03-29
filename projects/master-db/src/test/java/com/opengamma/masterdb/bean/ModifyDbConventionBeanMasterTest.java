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
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ConventionSearchSortOrder;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.masterdb.convention.DbConventionBeanMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests modification.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyDbConventionBeanMasterTest extends AbstractDbConventionBeanMasterTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(ModifyDbConventionBeanMasterTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyDbConventionBeanMasterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, false);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addConvention_nullDocument() {
    _cnvMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noConvention() {
    final ConventionDocument doc = new ConventionDocument();
    _cnvMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    final Instant now = Instant.now(_cnvMaster.getClock());

    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final ConventionDocument doc = new ConventionDocument();
    doc.setConvention(convention);
    final ConventionDocument test = _cnvMaster.add(doc);

    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbCnv", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageableConvention testConvention = test.getConvention();
    assertNotNull(testConvention);
    assertEquals(uniqueId, testConvention.getUniqueId());
    assertEquals("TestConvention", convention.getName());
    assertEquals("MOCK", convention.getConventionType().getName());
    final ExternalIdBundle idKey = convention.getExternalIdBundle();
    assertNotNull(idKey);
    assertEquals(1, idKey.size());
    assertEquals(ExternalId.of("A", "B"), idKey.getExternalIds().iterator().next());
  }

  @Test
  public void test_add_addThenGet() {
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final ConventionDocument doc = new ConventionDocument();
    doc.setConvention(convention);
    final ConventionDocument added = _cnvMaster.add(doc);

    final ConventionDocument test = _cnvMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingNameProperty() throws Exception {
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final Field field = ManageableConvention.class.getDeclaredField("_name");
    field.setAccessible(true);
    field.set(convention, null);
    final ConventionDocument doc = new ConventionDocument(convention);
    _cnvMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingExternalIdBundleProperty() throws Exception {
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final Field field = ManageableConvention.class.getDeclaredField("_externalIdBundle");
    field.setAccessible(true);
    field.set(convention, null);
    final ConventionDocument doc = new ConventionDocument(convention);
    _cnvMaster.add(doc);
  }

  @Test
  public void test_add_searchByAttribute() {
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention.addAttribute("city", "London");
    convention.addAttribute("office", "Southern");
    final ConventionDocument added = _cnvMaster.add(new ConventionDocument(convention));

    final ManageableConvention convention2 = new MockConvention("TestConvention2", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention2.addAttribute("office", "Southern");
    final ConventionDocument added2 = _cnvMaster.add(new ConventionDocument(convention2));

    ConventionSearchRequest searchRequest = new ConventionSearchRequest();
    searchRequest.addAttribute("city", "London");
    ConventionSearchResult searchResult = _cnvMaster.search(searchRequest);
    assertEquals(1, searchResult.getDocuments().size());
    assertEquals(added, searchResult.getDocuments().get(0));

    searchRequest = new ConventionSearchRequest();
    searchRequest.setSortOrder(ConventionSearchSortOrder.NAME_ASC);
    searchRequest.addAttribute("office", "Southern");
    searchResult = _cnvMaster.search(searchRequest);
    assertEquals(2, searchResult.getDocuments().size());
    assertEquals(added, searchResult.getDocuments().get(0));
    assertEquals(added2, searchResult.getDocuments().get(1));

    searchRequest = new ConventionSearchRequest();
    searchRequest.addAttribute("city", "London");
    searchRequest.addAttribute("office", "*thern");
    searchResult = _cnvMaster.search(searchRequest);
    assertEquals(1, searchResult.getDocuments().size());
    assertEquals(added, searchResult.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_updateConvention_nullDocument() {
    _cnvMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noConventionId() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "101");
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention.setUniqueId(uniqueId);
    final ConventionDocument doc = new ConventionDocument();
    doc.setConvention(convention);
    _cnvMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noConvention() {
    final ConventionDocument doc = new ConventionDocument();
    doc.setUniqueId(UniqueId.of("DbCnv", "101", "0"));
    _cnvMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "0", "0");
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention.setUniqueId(uniqueId);
    final ConventionDocument doc = new ConventionDocument(convention);
    _cnvMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "201", "0");
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention.setUniqueId(uniqueId);
    final ConventionDocument doc = new ConventionDocument(convention);
    _cnvMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    final Instant now = Instant.now(_cnvMaster.getClock());

    final UniqueId uniqueId = UniqueId.of("DbCnv", "101", "0");
    final ConventionDocument base = _cnvMaster.get(uniqueId);
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention.setUniqueId(uniqueId);
    final ConventionDocument input = new ConventionDocument(convention);

    final ConventionDocument updated = _cnvMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getConvention(), updated.getConvention());

    final ConventionDocument old = _cnvMaster.get(uniqueId);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getConvention(), old.getConvention());

    final ConventionHistoryRequest search = new ConventionHistoryRequest(base.getUniqueId(), null, now);
    final ConventionHistoryResult searchResult = _cnvMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    final DbConventionBeanMaster w = new DbConventionBeanMaster(_cnvMaster.getDbConnector());
    w.setElSqlBundle(ElSqlBundle.of(new ElSqlConfig("TestRollback"), DbBeanMaster.class));
    final ConventionDocument base = _cnvMaster.get(UniqueId.of("DbCnv", "101", "0"));
    final UniqueId uniqueId = UniqueId.of("DbCnv", "101", "0");
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention.setUniqueId(uniqueId);
    final ConventionDocument input = new ConventionDocument(convention);
    try {
      w.update(input);
      Assert.fail();
    } catch (final BadSqlGrammarException ex) {
      // expected
    }
    final ConventionDocument test = _cnvMaster.get(UniqueId.of("DbCnv", "101", "0"));

    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correctConvention_nullDocument() {
    _cnvMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noConventionId() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "101");
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention.setUniqueId(uniqueId);
    final ConventionDocument doc = new ConventionDocument();
    doc.setConvention(convention);
    _cnvMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noConvention() {
    final ConventionDocument doc = new ConventionDocument();
    doc.setUniqueId(UniqueId.of("DbCnv", "101", "0"));
    _cnvMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "0", "0");
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention.setUniqueId(uniqueId);
    final ConventionDocument doc = new ConventionDocument(convention);
    _cnvMaster.correct(doc);
  }

  @Test
  public void test_correct_getUpdateGet() {
    final Instant now = Instant.now(_cnvMaster.getClock());

    final UniqueId uniqueId = UniqueId.of("DbCnv", "101", "0");
    final ConventionDocument base = _cnvMaster.get(uniqueId);
    final ManageableConvention convention = new MockConvention("TestConvention", ExternalIdBundle.of("A", "B"), Currency.GBP);
    convention.setUniqueId(uniqueId);
    final ConventionDocument input = new ConventionDocument(convention);

    final ConventionDocument corrected = _cnvMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getConvention(), corrected.getConvention());

    final ConventionDocument old = _cnvMaster.get(UniqueId.of("DbCnv", "101", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getConvention(), old.getConvention());

    final ConventionHistoryRequest search = new ConventionHistoryRequest(base.getUniqueId(), now, null);
    final ConventionHistoryResult searchResult = _cnvMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeConvention_versioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "0", "0");
    _cnvMaster.remove(uniqueId);
  }

  @Test
  public void test_remove_removed() {
    final Instant now = Instant.now(_cnvMaster.getClock());

    final UniqueId uniqueId = UniqueId.of("DbCnv", "101", "0");
    _cnvMaster.remove(uniqueId);
    final ConventionDocument test = _cnvMaster.get(uniqueId);

    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageableConvention convention = test.getConvention();
    assertNotNull(convention);
    assertEquals(uniqueId, convention.getUniqueId());
    assertEquals("TestConvention101", convention.getName());
    assertEquals("MOCK", convention.getConventionType().getName());
    assertEquals(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F")), convention.getExternalIdBundle());
  }

}
