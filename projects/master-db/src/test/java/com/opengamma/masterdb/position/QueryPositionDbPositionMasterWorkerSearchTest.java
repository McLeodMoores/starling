/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryPositionDbPositionMasterWorkerSearchTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPositionDbPositionMasterWorkerSearchTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents() {
    final PositionSearchRequest request = new PositionSearchRequest();
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());

    assertEquals(_totalPositions, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(1, 2));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(2, 2));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(3, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
    assert122(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageThree() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(3, 2));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(5, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());

    assertEquals(2, test.getDocuments().size());
    assert123(test.getDocuments().get(0));
    assert222(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_positionIds_none() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setPositionObjectIds(new ArrayList<ObjectId>());
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_positionIds() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionObjectId(ObjectId.of("DbPos", "120"));
    request.addPositionObjectId(ObjectId.of("DbPos", "221"));
    request.addPositionObjectId(ObjectId.of("DbPos", "9999"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert120(test.getDocuments().get(0));
    assert222(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_positionIds_badSchemeValidOid() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionObjectId(ObjectId.of("Rubbish", "120"));
    _posMaster.search(request);
  }

  @Test
  public void test_search_tradeIds_none() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setTradeObjectIds(new ArrayList<ObjectId>());
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_tradeIds() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addTradeObjectId(ObjectId.of("DbPos", "402"));
    request.addTradeObjectId(ObjectId.of("DbPos", "403"));
    request.addTradeObjectId(ObjectId.of("DbPos", "407"));
    request.addTradeObjectId(ObjectId.of("DbPos", "9999"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert122(test.getDocuments().get(0));
    assert222(test.getDocuments().get(1));
  }

  @Test
  public void test_search_trades_withPremium() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);

    final ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade1.setPremium(1000000.00);
    trade1.setPremiumCurrency(Currency.USD);
    trade1.setPremiumDate(tradeDate.plusDays(1));
    trade1.setPremiumTime(tradeTime);
    position.getTrades().add(trade1);

    final ManageableTrade trade2 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("C", "D"), tradeDate, tradeTime, ExternalId.of("CPS2", "CPV2"));
    trade2.setPremium(100.00);
    trade2.setPremiumCurrency(Currency.GBP);
    trade2.setPremiumDate(tradeDate.plusDays(10));
    trade2.setPremiumTime(tradeTime.plusHours(1));
    position.getTrades().add(trade2);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.add(doc);
    assertNotNull(trade1.getUniqueId());
    assertNotNull(trade2.getUniqueId());

    final PositionSearchRequest requestByTrade = new PositionSearchRequest();
    requestByTrade.addTradeObjectId(trade1.getUniqueId().getObjectId());

    PositionSearchResult test = _posMaster.search(requestByTrade);
    assertEquals(1, test.getDocuments().size());
    assertEquals(doc, test.getDocuments().get(0));

    final PositionSearchRequest requestByPosition = new PositionSearchRequest();
    requestByPosition.addPositionObjectId(position.getUniqueId().getObjectId());
    test = _posMaster.search(requestByTrade);
    assertEquals(1, test.getDocuments().size());
    assertEquals(doc, test.getDocuments().get(0));

  }

  @Test
  public void test_search_trades_withAttributes() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);

    final ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade1.addAttribute("key11", "value11");
    trade1.addAttribute("key12", "value12");
    position.addTrade(trade1);

    final ManageableTrade trade2 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("C", "D"), tradeDate, tradeTime, ExternalId.of("CPS2", "CPV2"));
    trade2.addAttribute("key21", "value21");
    trade2.addAttribute("key22", "value22");
    position.addTrade(trade2);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.add(doc);
    assertNotNull(trade1.getUniqueId());
    assertNotNull(trade2.getUniqueId());

    final PositionSearchRequest requestByTrade = new PositionSearchRequest();
    requestByTrade.addTradeObjectId(trade1.getUniqueId().getObjectId());

    PositionSearchResult test = _posMaster.search(requestByTrade);
    assertEquals(1, test.getDocuments().size());
    assertEquals(doc, test.getDocuments().get(0));

    final PositionSearchRequest requestByPosition = new PositionSearchRequest();
    requestByPosition.addPositionObjectId(position.getUniqueId().getObjectId());
    test = _posMaster.search(requestByTrade);
    assertEquals(1, test.getDocuments().size());
    assertEquals(doc, test.getDocuments().get(0));
  }

  @Test
  public void test_search_position_withAttributes() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    position.addAttribute("PA1", "A");
    position.addAttribute("PA2", "B");
    position.addAttribute("PA3", "C");

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);

    final ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade1.addAttribute("key11", "value11");
    trade1.addAttribute("key12", "value12");
    position.addTrade(trade1);

    final ManageableTrade trade2 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("C", "D"), tradeDate, tradeTime, ExternalId.of("CPS2", "CPV2"));
    trade2.addAttribute("key21", "value21");
    trade2.addAttribute("key22", "value22");
    position.addTrade(trade2);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.add(doc);
    assertNotNull(trade1.getUniqueId());
    assertNotNull(trade2.getUniqueId());

    final PositionSearchRequest requestByTrade = new PositionSearchRequest();
    requestByTrade.addTradeObjectId(trade1.getUniqueId().getObjectId());

    PositionSearchResult test = _posMaster.search(requestByTrade);
    assertEquals(1, test.getDocuments().size());
    assertEquals(doc, test.getDocuments().get(0));

    final PositionSearchRequest requestByPosition = new PositionSearchRequest();
    requestByPosition.addPositionObjectId(position.getUniqueId().getObjectId());
    test = _posMaster.search(requestByTrade);
    assertEquals(1, test.getDocuments().size());
    assertEquals(doc, test.getDocuments().get(0));
  }



  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_tradeIds_badSchemeValidOid() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addTradeObjectId(ObjectId.of("Rubbish", "402"));
    _posMaster.search(request);
  }

  @Test
  public void test_search_positionAndTradeIds_matchSome() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionObjectId(ObjectId.of("DbPos", "120"));
    request.addPositionObjectId(ObjectId.of("DbPos", "122"));
    request.addTradeObjectId(ObjectId.of("DbPos", "402"));
    request.addTradeObjectId(ObjectId.of("DbPos", "403"));
    request.addTradeObjectId(ObjectId.of("DbPos", "407"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert122(test.getDocuments().get(0));
  }

  @Test
  public void test_search_positionAndTradeIds_matchNone() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionObjectId(ObjectId.of("DbPos", "120"));
    request.addPositionObjectId(ObjectId.of("DbPos", "121"));
    request.addTradeObjectId(ObjectId.of("DbPos", "402"));
    request.addTradeObjectId(ObjectId.of("DbPos", "403"));
    request.addTradeObjectId(ObjectId.of("DbPos", "407"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdSearch(ExternalIdSearch.of(ExternalIdSearchType.EXACT));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ALL));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ANY));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdSearch(ExternalIdSearch.of(ExternalIdSearchType.NONE));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(_totalPositions, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_1() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalId(ExternalId.of("TICKER", "S100"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_1_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalId(ExternalId.of("A", "Z"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_2() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalIds(ExternalId.of("TICKER", "MSFT"), ExternalId.of("TICKER", "S100"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert121(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_Any_2_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalIds(ExternalId.of("E", "H"), ExternalId.of("A", "D"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdValue("S100");
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_case() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdValue("s100");
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdValue("FooBar");
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier_wildcard() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdValue("OR*");
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert122(test.getDocuments().get(0));
    assert123(test.getDocuments().get(1));
  }

  @Test
  public void test_search_identifier_wildcardCase() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdValue("or*");
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(2, test.getDocuments().size());
    assert122(test.getDocuments().get(0));
    assert123(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_1() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalId(ExternalId.of("TICKER", "S100"));
    request.setSecurityExternalIdSearchType(ExternalIdSearchType.ALL);
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_1_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalId(ExternalId.of("A", "Z"));
    request.setSecurityExternalIdSearchType(ExternalIdSearchType.ALL);
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_2() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalIds(ExternalId.of("TICKER", "MSFT"), ExternalId.of("NASDAQ", "Micro"));
    request.setSecurityExternalIdSearchType(ExternalIdSearchType.ALL);
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  @Test
  public void test_search_twoKeys_All_2_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalIds(ExternalId.of("TICKER", "MSFT"), ExternalId.of("A", "D"));
    request.setSecurityExternalIdSearchType(ExternalIdSearchType.ALL);
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalId(ExternalId.of("TICKER", "MSFT"));
    request.setSecurityExternalIdSearchType(ExternalIdSearchType.NONE);
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(5, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert122(test.getDocuments().get(2));
    assert123(test.getDocuments().get(3));
    assert222(test.getDocuments().get(4));
  }

  @Test
  public void test_search_oneKey_None_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalId(ExternalId.of("TICKER", "S100"));
    request.addSecurityExternalId(ExternalId.of("TICKER", "T130"));
    request.addSecurityExternalId(ExternalId.of("TICKER", "MSFT"));
    request.addSecurityExternalId(ExternalId.of("NASDAQ", "Micro"));
    request.addSecurityExternalId(ExternalId.of("TICKER", "ORCL"));
    request.addSecurityExternalId(ExternalId.of("TICKER", "ORCL134"));
    request.addSecurityExternalId(ExternalId.of("NASDAQ", "ORCL135"));
    request.addSecurityExternalId(ExternalId.of("TICKER", "IBMC"));
    request.setSecurityExternalIdSearchType(ExternalIdSearchType.NONE);
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalIds(ExternalId.of("TICKER", "MSFT"), ExternalId.of("NASDAQ", "Micro"));
    request.setSecurityExternalIdSearchType(ExternalIdSearchType.EXACT);
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityExternalId(ExternalId.of("TICKER", "MSFT"));
    request.setSecurityExternalIdSearchType(ExternalIdSearchType.EXACT);
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_positionProviderKey_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setPositionProviderId(ExternalId.of("A", "999"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_positionProviderKey_found() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setPositionProviderId(ExternalId.of("A", "121"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_tradeProviderKey_noMatch() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setTradeProviderId(ExternalId.of("B", "999"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_tradeProviderKey_found() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setTradeProviderId(ExternalId.of("B", "401"));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_minQuantity_below() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(50));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(6, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
    assert222(test.getDocuments().get(5));
  }

  @Test
  public void test_search_minQuantity_mid() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(150));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(1, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
  }

  @Test
  public void test_search_minQuantity_above() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(450));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_maxQuantity_below() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(50));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_maxQuantity_mid() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(150));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(5, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
  }

  @Test
  public void test_search_maxQuantity_above() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(450));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(6, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
    assert222(test.getDocuments().get(5));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(6, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
    assert221(test.getDocuments().get(5));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    final PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    final PositionSearchResult test = _posMaster.search(request);

    assertEquals(6, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
    assert222(test.getDocuments().get(5));  // new version
  }

}
