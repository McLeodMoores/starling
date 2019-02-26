/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryExchangeMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryExchangeMasterTest {

  private static final String NAME = "LIFFE";
  private static final ExternalId ID_LIFFE_MIC = ExternalId.of(ExternalSchemes.ISO_MIC, "XLIF");
  private static final ExternalId ID_LIFFE_CCID = ExternalId.of("COPP_CLARK_CENTER_ID", "979");
  private static final ExternalId ID_LIFFE_CCNAME = ExternalId.of("COPP_CLARK_NAME", "Euronext LIFFE (UK contracts)");
  private static final ExternalId ID_OTHER1 = ExternalId.of("TEST_SCHEME", "EURONEXT LIFFE");
  private static final ExternalId ID_OTHER2 = ExternalId.of("TEST_SCHEME", "LIFFE");
  private static final ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(ID_LIFFE_MIC, ID_LIFFE_CCNAME, ID_LIFFE_CCID);
  private static final ExternalIdBundle BUNDLE_PART = ExternalIdBundle.of(ID_LIFFE_MIC, ID_LIFFE_CCID);
  private static final ExternalIdBundle BUNDLE_OTHER = ExternalIdBundle.of(ID_LIFFE_MIC, ID_LIFFE_CCNAME, ID_OTHER1);
  private static final ExternalIdBundle GB = ExternalIdBundle.of(ExternalSchemes.countryRegionId(Country.GB));

  private InMemoryExchangeMaster _master;
  private ExchangeDocument _addedDoc;

  /**
   *
   */
  @BeforeMethod
  public void setUp() {
    _master = new InMemoryExchangeMaster();
    final ManageableExchange inputExchange = new ManageableExchange(BUNDLE_FULL, NAME, GB, ZoneId.of("Europe/London"));
    final ExchangeDocument inputDoc = new ExchangeDocument(inputExchange);
    _addedDoc = _master.add(inputDoc);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetNoMatch() {
    _master.get(UniqueId.of("A", "B"));
  }

  /**
   *
   */
  public void testGetMatch() {
    final ExchangeDocument result = _master.get(_addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemExg", "1"), result.getUniqueId());
    assertEquals(_addedDoc, result);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchOneIdNoMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest(ID_OTHER1);
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchOneIdMic() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest(ID_LIFFE_MIC);
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchOneIdCcid() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest(ID_LIFFE_MIC);
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchOneBundleNoMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE_OTHER);
    request.setExternalIdSearch(request.getExternalIdSearch().withSearchType(ExternalIdSearchType.ALL));
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchOneBundleFull() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE_FULL);
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchOneBundlePart() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE_PART);
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchTwoBundlesNoMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ID_OTHER1);
    request.addExternalId(ID_OTHER2);
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchTwoBundlesOneMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ID_LIFFE_MIC);
    request.addExternalId(ID_OTHER1);
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchTwoBundlesBothMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ID_LIFFE_MIC);
    request.addExternalId(ID_LIFFE_CCID);
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchNameNoMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("No match");
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchNameMatch() {
    final ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName(NAME);
    final ExchangeSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

}
