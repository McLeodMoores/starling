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
import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests querying.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryDbConventionBeanMasterTest extends AbstractDbConventionBeanMasterTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryDbConventionBeanMasterTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryDbConventionBeanMasterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getConvention_nullUID() {
    _cnvMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConvention_versioned_notFoundId() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "0", "0");
    _cnvMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConvention_versioned_notFoundVersion() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "101", "1");
    _cnvMaster.get(uniqueId);
  }

  @Test
  public void test_getConvention_versioned_oneConventionKey() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "101", "0");
    final ConventionDocument test = _cnvMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getConvention_versioned_twoConventionKeys() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "102", "0");
    final ConventionDocument test = _cnvMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getConvention_versioned_notLatest() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "201", "0");
    final ConventionDocument test = _cnvMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getConvention_versioned_latest() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "201", "1");
    final ConventionDocument test = _cnvMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getConvention_unversioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbCnv", "0");
    _cnvMaster.get(uniqueId);
  }

  @Test
  public void test_getConvention_unversioned() {
    final UniqueId oid = UniqueId.of("DbCnv", "201");
    final ConventionDocument test = _cnvMaster.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_searchSecurities_documents() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    final ConventionSearchResult test = _cnvMaster.search(request);

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
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setPagingRequest(pr);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    final PagingRequest pr = PagingRequest.ofPage(2, 2);
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setPagingRequest(pr);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_pageAtEnd() {
    final PagingRequest pr = PagingRequest.ofIndex(3, 2);
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setPagingRequest(pr);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("B");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_identifier_case() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("hi");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("FooBar");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier_wildcard() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("H*");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_wildcardCase() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdValue("h*");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_scheme() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("A");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_scheme_case() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("gh");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_scheme_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("FooBar");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_scheme_wildcard() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("G*");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_scheme_wildcardCase() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdScheme("g*");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_name_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("FooBar");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("TestConvention102");
    final ConventionSearchResult test = _cnvMaster.search(request);
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("TESTConvention102");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("TestConvention1*");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("TESTConvention1*");
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setConventionType(ConventionType.of("MOCK"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    final ConventionDocument doc0 = test.getDocuments().get(0);
    final ConventionDocument doc1 = test.getDocuments().get(1);
    final ConventionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbCnv", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "201", "1"), doc2.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_securityIds_none() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_securityIds() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addObjectId(ObjectId.of("DbCnv", "101"));
    request.addObjectId(ObjectId.of("DbCnv", "201"));
    request.addObjectId(ObjectId.of("DbCnv", "9999"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_securityIds_badSchemeValidOid() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "120"));
    _cnvMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ANY);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(_totalSecurities, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GHI() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GHI() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("E", "F"), ExternalId.of("GH", "HI"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("E", "HI"), ExternalId.of("A", "D"));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GHI() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GHI() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GHI() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    final ConventionDocument doc0 = test.getDocuments().get(0);
    final ConventionDocument doc1 = test.getDocuments().get(1);
    final ConventionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbCnv", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "201", "0"), doc2.getUniqueId());  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    final ConventionSearchResult test = _cnvMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    final ConventionDocument doc0 = test.getDocuments().get(0);
    final ConventionDocument doc1 = test.getDocuments().get(1);
    final ConventionDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbCnv", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbCnv", "201", "1"), doc2.getUniqueId());  // new version
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleConventions() {
    final ObjectId oid = ObjectId.of("DbCnv", "102");
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final PagingRequest pr = PagingRequest.ofPage(1, 1);
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setPagingRequest(pr);
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final PagingRequest pr = PagingRequest.ofPage(2, 1);
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setPagingRequest(pr);
    final ConventionHistoryResult test = _cnvMaster.history(request);

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
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(0, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    final ObjectId oid = ObjectId.of("DbCnv", "201");
    final ConventionHistoryRequest request = new ConventionHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    final ConventionHistoryResult test = _cnvMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

}
