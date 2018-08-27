/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryPortfolioDbPortfolioMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryPortfolioDbPortfolioMasterWorkerSearchTest extends AbstractDbPortfolioMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryPortfolioDbPortfolioMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPortfolioDbPortfolioMasterWorkerSearchTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents_maxDepth() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(-1);
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());

    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_documents_depthZero() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(0);
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());
    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 0);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  @Test
  public void test_search_documents_depthOne() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(1);
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());
    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 1);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(1, 2));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(2, 2));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(3, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_visiblePortfolios, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_portfolioIds_none() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setPortfolioObjectIds(new ArrayList<ObjectId>());
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_portfolioIds_one() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "201"));
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "9999"));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_portfolioIds_two() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "101"));
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "201"));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_portfolioIds_badSchemeValidOid() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("Rubbish", "201"));
    _prtMaster.search(request);
  }

  @Test
  public void test_search_nodeIds_none() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setNodeObjectIds(new ArrayList<ObjectId>());
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_nodeIds() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addNodeObjectId(ObjectId.of("DbPrt", "211"));
    request.addNodeObjectId(ObjectId.of("DbPrt", "9999"));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_nodeIds_badSchemeValidOid() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("Rubbish", "211"));
    _prtMaster.search(request);
  }

  @Test
  public void test_search_portfolioAndNodeIds_matchSome() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "201"));
    request.addNodeObjectId(ObjectId.of("DbPrt", "211"));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_portfolioAndNodeIds_matchNone() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.addPortfolioObjectId(ObjectId.of("DbPrt", "101"));
    request.addNodeObjectId(ObjectId.of("DbPrt", "211"));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("FooBar");
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name_exactMatch() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TestPortfolio101");
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
  }

  @Test
  public void test_search_name_case() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TESTPortfolio101");
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
  }

  @Test
  public void test_search_name_wildcardMatch_one() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TestPortfolio2*");
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcardMatch_two() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TestPortfolio1*");
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName("TESTPortfolio1*");
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_visibility() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setVisibility(DocumentVisibility.HIDDEN);
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(_totalPortfolios, test.getDocuments().size());
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
    assert301(test.getDocuments().get(3));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noPositions() {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setIncludePositions(false);
    final PortfolioSearchResult test = _prtMaster.search(request);

    assertEquals(_visiblePortfolios, test.getDocuments().size());
    assertNoPositions();
    assert101(test.getDocuments().get(0), 999);
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

}
