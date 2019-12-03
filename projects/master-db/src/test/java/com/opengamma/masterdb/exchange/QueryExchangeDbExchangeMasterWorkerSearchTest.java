/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryExchangeDbExchangeMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryExchangeDbExchangeMasterWorkerSearchTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryExchangeDbExchangeMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryExchangeDbExchangeMasterWorkerSearchTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalExchanges, test.getPaging().getTotalItems());

    assertEquals(_totalExchanges, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(1, 2));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalExchanges, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(2, 2));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(3, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalExchanges, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("FooBar");
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("TestExchange102");
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("TESTExchange102");
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("TestExchange1*");
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("TESTExchange1*");
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_exchangeIds_none() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_exchangeIds() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addObjectId(ObjectId.of("DbExg", "101"));
    request.addObjectId(ObjectId.of("DbExg", "201"));
    request.addObjectId(ObjectId.of("DbExg", "9999"));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_exchangeIds_badSchemeValidOid() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "120"));
    _exgMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExternalIdSearchType(ExternalIdSearchType.ANY);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_allMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(_totalExchanges, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_AB() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_CD() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_Any_EF() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_Any_GH() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("G", "H"));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("A", "H"));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_AB_CD() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_EF_GH() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("E", "F"), ExternalId.of("G", "H"));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_twoKeys_Any_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("E", "H"), ExternalId.of("A", "D"));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_AB() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_CD() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_oneKey_All_EF() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_All_GH() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("G", "H"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("A", "H"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_AB_CD() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_CD_EF() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_All_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "H")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_All_AB_CD_EF() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_AB_CD_GH() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_All_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "H")));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None_AB() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("A", "B"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_None_CD_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact_AB_CD_EF() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_AB_CD_GH() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalIds(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_oneId_AB() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(ExternalId.of("A", "B"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_oneId_CD() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(ExternalId.of("C", "D"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(3, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//    assert202(test.getDocuments().get(2));
//  }
//
//  @Test
//  public void test_search_oneId_EF() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(ExternalId.of("E", "F"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert202(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_oneId_GH() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(ExternalId.of("G", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(1, test.getDocuments().size());
//    assert102(test.getDocuments().get(0));
//  }
//
//  @Test
//  public void test_search_oneId_noMatch() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(ExternalId.of("A", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(0, test.getDocuments().size());
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_twoIds_AB_CD() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(ExternalId.of("A", "B"), ExternalId.of("C", "D"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_twoIds_CD_EF() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(ExternalId.of("C", "D"), ExternalId.of("E", "F"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(2, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert202(test.getDocuments().get(1));
//  }
//
//  @Test
//  public void test_search_twoIds_noMatch() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(ExternalId.of("C", "D"), ExternalId.of("E", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(0, test.getDocuments().size());
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_threeIds_AB_CD_EF() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(1, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//  }
//
//  @Test
//  public void test_search_threeIds_AB_CD_GH() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("G", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(1, test.getDocuments().size());
//    assert102(test.getDocuments().get(0));
//  }
//
//  @Test
//  public void test_search_threeIds_noMatch() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKeys(ExternalId.of("C", "D"), ExternalId.of("E", "F"), ExternalId.of("A", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(0, test.getDocuments().size());
//  }
//
//  //-------------------------------------------------------------------------
//  @Test
//  public void test_search_ids_AB_or_CD() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(ExternalId.of("A", "B"));
//    request.addExchangeKey(ExternalId.of("C", "D"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(3, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//    assert202(test.getDocuments().get(2));
//  }
//
//  @Test
//  public void test_search_ids_EF_or_GH() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(ExternalId.of("E", "F"));
//    request.addExchangeKey(ExternalId.of("G", "H"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(3, test.getDocuments().size());
//    assert101(test.getDocuments().get(0));
//    assert102(test.getDocuments().get(1));
//    assert202(test.getDocuments().get(2));
//  }
//
//  @Test
//  public void test_search_ids_or_noMatch() {
//    ExchangeSearchRequest request = new ExchangeSearchRequest();
//    request.addExchangeKey(ExternalId.of("E", "H"));
//    request.addExchangeKey(ExternalId.of("A", "D"));
//    ExchangeSearchResult test = _worker.search(request);
//
//    assertEquals(0, test.getDocuments().size());
//  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    final ExchangeSearchResult test = _exgMaster.search(request);

    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

}
