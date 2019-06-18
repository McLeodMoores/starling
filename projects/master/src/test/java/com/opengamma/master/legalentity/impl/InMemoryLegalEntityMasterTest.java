/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link com.opengamma.master.legalentity.impl.InMemoryLegalEntityMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryLegalEntityMasterTest {

  private static final String NAME = "FooBar";
  private static final ExternalId ID_ISIN_12345 = ExternalId.of(ExternalSchemes.ISIN, "12345");
  private static final ExternalId ID_FOO = ExternalId.of("FOO", "979");
  private static final ExternalId ID_BAR = ExternalId.of("BAR", "987654");
  private static final ExternalId ID_OTHER1 = ExternalId.of("TEST_SCHEME", "VAL1");
  private static final ExternalId ID_OTHER2 = ExternalId.of("TEST_SCHEME", "VAL2");
  private static final ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(ID_ISIN_12345, ID_BAR, ID_FOO);
  private static final ExternalIdBundle BUNDLE_PART = ExternalIdBundle.of(ID_ISIN_12345, ID_FOO);
  private static final ExternalIdBundle BUNDLE_OTHER = ExternalIdBundle.of(ID_ISIN_12345, ID_BAR, ID_OTHER1);

  private InMemoryLegalEntityMaster _master;
  private LegalEntityDocument _addedDoc;

  /**
   *
   */
  @BeforeMethod
  public void setUp() {
    _master = new InMemoryLegalEntityMaster();
    final ManageableLegalEntity inputLegalEntity = new MockLegalEntity(NAME, BUNDLE_FULL, Currency.GBP);
    final LegalEntityDocument inputDoc = new LegalEntityDocument(inputLegalEntity);
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
    final LegalEntityDocument result = _master.get(_addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemLen", "1"), result.getUniqueId());
    assertEquals(_addedDoc, result);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchOneIdNoMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest(ID_OTHER1);
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchOneIdMic() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest(ID_ISIN_12345);
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchOneIdCcid() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest(ID_ISIN_12345);
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchOneBundleNoMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest(BUNDLE_OTHER);
    request.setExternalIdSearch(request.getExternalIdSearch().withSearchType(ExternalIdSearchType.ALL));
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchOneBundleFull() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest(BUNDLE_FULL);
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchOneBundlePart() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest(BUNDLE_PART);
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchTwoBundlesNoMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ID_OTHER1);
    request.addExternalId(ID_OTHER2);
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchTwoBundlesOneMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ID_ISIN_12345);
    request.addExternalId(ID_OTHER1);
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchTwoBundlesBothMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ID_ISIN_12345);
    request.addExternalId(ID_FOO);
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchNameNoMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("No match");
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchNameMatch() {
    final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName(NAME);
    final LegalEntitySearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

}
