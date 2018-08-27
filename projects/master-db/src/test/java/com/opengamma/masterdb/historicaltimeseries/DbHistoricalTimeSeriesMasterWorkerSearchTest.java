/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerSearchTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerSearchTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());

    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    final PagingRequest pr = PagingRequest.ofPage(1, 2);
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setPagingRequest(pr);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    final PagingRequest pr = PagingRequest.ofPage(2, 2);
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setPagingRequest(pr);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());

    assertEquals(1, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_seriesIds_none() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_seriesIds() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addObjectId(ObjectId.of("DbHts", "101"));
    request.addObjectId(ObjectId.of("DbHts", "201"));
    request.addObjectId(ObjectId.of("DbHts", "9999"));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert203(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_seriesIds_badSchemeValidOid() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "101"));
    _htsMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.EXACT));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ALL));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ANY));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.NONE));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_1() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_1_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("A", "Z"));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_2() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("TICKER", "V503"));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_Any_2_oneMatches() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("TICKER", "RUBBISH"));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_twoKeys_Any_2_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("E", "H"), ExternalId.of("A", "D"));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_identifier() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("V501");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_case() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("v501");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("FooBar");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier_wildcard() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("*3");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_wildcardCase() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("v*3");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_1() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_1_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("A", "Z"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_2() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("NASDAQ", "V502"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_twoKeys_All_2_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("A", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
    assert203(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_None_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    request.addExternalId(ExternalId.of("TICKER", "V503"));
    request.addExternalId(ExternalId.of("TICKER", "V505"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Exact() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("NASDAQ", "V502"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("FooBar");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("N102");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("n102");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("N1*");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("n1*");
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    final HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    final HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);

    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert203(test.getDocuments().get(2));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
