/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.legalentity;

import static org.testng.Assert.assertEquals;
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
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests querying.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryDbLegalEntityBeanMasterTest extends AbstractDbLegalEntityBeanMasterTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryDbLegalEntityBeanMasterTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryDbLegalEntityBeanMasterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getLegalEntity_nullUID() {
    _lenMaster.get((UniqueId) null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getLegalEntity_versioned_notFoundId() {
    final UniqueId uniqueId = UniqueId.of("DbLen", "0", "0");
    _lenMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getLegalEntity_versioned_notFoundVersion() {
    final UniqueId uniqueId = UniqueId.of("DbLen", "101", "1");
    _lenMaster.get(uniqueId);
  }

  @Test
  public void test_getLegalEntity_versioned_oneLegalEntityKey() {
    final UniqueId uniqueId = UniqueId.of("DbLen", "101", "0");
    final LegalEntityDocument test = _lenMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getLegalEntity_versioned_twoLegalEntityKeys() {
    final UniqueId uniqueId = UniqueId.of("DbLen", "102", "0");
    final LegalEntityDocument test = _lenMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getLegalEntity_versioned_notLatest() {
    final UniqueId uniqueId = UniqueId.of("DbLen", "201", "0");
    final LegalEntityDocument test = _lenMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getLegalEntity_versioned_latest() {
    final UniqueId uniqueId = UniqueId.of("DbLen", "201", "1");
    final LegalEntityDocument test = _lenMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getLegalEntity_unversioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbLen", "0");
    _lenMaster.get(uniqueId);
  }

  @Test
  public void test_getLegalEntity_unversioned() {
    final UniqueId oid = UniqueId.of("DbLen", "201");
    final LegalEntityDocument test = _lenMaster.get(oid);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_searchSecurities_documents() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    final LegalEntitySearchResult test = _lenMaster.search(request);

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
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setPagingRequest(pr);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    final PagingRequest pr = PagingRequest.ofPage(2, 2);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setPagingRequest(pr);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_pageAtEnd() {
    final PagingRequest pr = PagingRequest.ofIndex(3, 2);
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setPagingRequest(pr);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalSecurities, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("B");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_identifier_case() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("hi");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("FooBar");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier_wildcard() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("H*");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_wildcardCase() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdValue("h*");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_scheme() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("A");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_scheme_case() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("gh");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_scheme_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("FooBar");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_scheme_wildcard() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("G*");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_scheme_wildcardCase() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdScheme("g*");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------

  @Test
  public void test_search_name_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("FooBar");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("TestLegalEntity102");
    final LegalEntitySearchResult test = _lenMaster.search(request);
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("TESTLegalEntity102");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("TestLegalEntity1*");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("TESTLegalEntity1*");
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    final LegalEntityDocument doc0 = test.getDocuments().get(0);
    final LegalEntityDocument doc1 = test.getDocuments().get(1);
    final LegalEntityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbLen", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "201", "1"), doc2.getUniqueId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_securityIds_none() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_securityIds() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addObjectId(ObjectId.of("DbLen", "101"));
    request.addObjectId(ObjectId.of("DbLen", "201"));
    request.addObjectId(ObjectId.of("DbLen", "9999"));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_securityIds_badSchemeValidOid() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "120"));
    _lenMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.ANY);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of());
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(_totalSecurities, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GHI() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GHI() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("E", "F"), ExternalId.of("GH", "HI"));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("E", "HI"), ExternalId.of("A", "D"));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GHI() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GHI() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "HI")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GHI() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("GH", "HI"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    final LegalEntityDocument doc0 = test.getDocuments().get(0);
    final LegalEntityDocument doc1 = test.getDocuments().get(1);
    final LegalEntityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbLen", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "201", "0"), doc2.getUniqueId());  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    final LegalEntitySearchResult test = _lenMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    final LegalEntityDocument doc0 = test.getDocuments().get(0);
    final LegalEntityDocument doc1 = test.getDocuments().get(1);
    final LegalEntityDocument doc2 = test.getDocuments().get(2);
    assertEquals(UniqueId.of("DbLen", "101", "0"), doc0.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "102", "0"), doc1.getUniqueId());
    assertEquals(UniqueId.of("DbLen", "201", "1"), doc2.getUniqueId());  // new version
  }

  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_documentCountWhenMultipleLegalEntities() {
    final ObjectId oid = ObjectId.of("DbLen", "102");
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final PagingRequest pr = PagingRequest.ofPage(1, 1);
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setPagingRequest(pr);
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final PagingRequest pr = PagingRequest.ofPage(2, 1);
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setPagingRequest(pr);
    final LegalEntityHistoryResult test = _lenMaster.history(request);

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
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(0, test.getPaging().getTotalItems());

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(1, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert201(test.getDocuments().get(0));
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    final ObjectId oid = ObjectId.of("DbLen", "201");
    final LegalEntityHistoryRequest request = new LegalEntityHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    final LegalEntityHistoryResult test = _lenMaster.history(request);

    assertEquals(2, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
    assert201(test.getDocuments().get(1));
  }

}
