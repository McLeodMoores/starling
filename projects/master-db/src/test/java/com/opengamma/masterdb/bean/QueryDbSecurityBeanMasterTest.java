/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.bean;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests querying.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryDbSecurityBeanMasterTest extends AbstractDbSecurityBeanMasterTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryDbSecurityBeanMasterTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryDbSecurityBeanMasterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getSecurity_nullUID() {
    _secMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getSecurity_versioned_notFoundId() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "0", "0");
    _secMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getSecurity_versioned_notFoundVersion() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "101", "1");
    _secMaster.get(uniqueId);
  }

  @Test
  public void test_getSecurity_versioned_oneSecurityKey() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "101", "0");
    final SecurityDocument test = _secMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getSecurity_versioned_twoSecurityKeys() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "102", "0");
    final SecurityDocument test = _secMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getSecurity_versioned_notLatest() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "201", "0");
    final SecurityDocument test = _secMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getSecurity_versioned_latest() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "201", "1");
    final SecurityDocument test = _secMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getSecurity_unversioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbSec", "0");
    _secMaster.get(uniqueId);
  }

  @Test
  public void test_getSecurity_unversioned() {
    final UniqueId oid = UniqueId.of("DbSec", "201");
    final SecurityDocument test = _secMaster.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_searchSecurities_documents() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(_totalSecurities, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    final PagingRequest pr = PagingRequest.ofPage(1, 2);
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(pr);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    final PagingRequest pr = PagingRequest.ofPage(2, 2);
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(pr);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_pageAtEnd() {
    final PagingRequest pr = PagingRequest.ofIndex(3, 2);
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setPagingRequest(pr);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("B");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_identifier_case() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("hi");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("FooBar");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier_wildcard() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("H*");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_wildcardCase() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdValue("h*");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_scheme() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("A");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_scheme_case() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("gh");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_scheme_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("FooBar");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_scheme_wildcard() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("G*");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_scheme_wildcardCase() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdScheme("g*");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_name_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("FooBar");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TestSecurity102");
    final SecuritySearchResult test = _secMaster.search(request);
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TESTSecurity102");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TestSecurity1*");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setName("TESTSecurity1*");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType("EQUITY");
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    final SecurityDocument doc0 = test.getDocuments().get(0);
    final SecurityDocument doc1 = test.getDocuments().get(1);
    final SecurityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbSec", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "201", "1"), doc2.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_securityIds_none() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_securityIds() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addObjectId(ObjectId.of("DbSec", "101"));
    request.addObjectId(ObjectId.of("DbSec", "201"));
    request.addObjectId(ObjectId.of("DbSec", "9999"));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_securityIds_badSchemeValidOid() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "120"));
    _secMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ANY);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(_totalSecurities, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GHI() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GHI() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("E", "F"), ExternalId.of("GH", "HI"));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("E", "HI"), ExternalId.of("A", "D"));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GHI() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GHI() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GHI() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    final SecurityDocument doc0 = test.getDocuments().get(0);
    final SecurityDocument doc1 = test.getDocuments().get(1);
    final SecurityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbSec", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "201", "0"), doc2.getUniqueId());  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    final SecuritySearchResult test = _secMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    final SecurityDocument doc0 = test.getDocuments().get(0);
    final SecurityDocument doc1 = test.getDocuments().get(1);
    final SecurityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbSec", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbSec", "201", "1"), doc2.getUniqueId());  // new version
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleSecuritys() {
    final ObjectId oid = ObjectId.of("DbSec", "102");
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final PagingRequest pr = PagingRequest.ofPage(1, 1);
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setPagingRequest(pr);
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final PagingRequest pr = PagingRequest.ofPage(2, 1);
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setPagingRequest(pr);
    final SecurityHistoryResult test = _secMaster.history(request);

    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(0, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    final ObjectId oid = ObjectId.of("DbSec", "201");
    final SecurityHistoryRequest request = new SecurityHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    final SecurityHistoryResult test = _secMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

}
