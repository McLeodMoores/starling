/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryConventionMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryConventionMasterTest {

  private static final String NAME = "FooBar";
  private static final ExternalId ID_ISIN_12345 = ExternalId.of(ExternalSchemes.ISIN, "12345");
  private static final ExternalId ID_FOO = ExternalId.of("FOO", "979");
  private static final ExternalId ID_BAR = ExternalId.of("BAR", "987654");
  private static final ExternalId ID_OTHER1 = ExternalId.of("TEST_SCHEME", "VAL1");
  private static final ExternalId ID_OTHER2 = ExternalId.of("TEST_SCHEME", "VAL2");
  private static final ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(ID_ISIN_12345, ID_BAR, ID_FOO);
  private static final ExternalIdBundle BUNDLE_PART = ExternalIdBundle.of(ID_ISIN_12345, ID_FOO);
  private static final ExternalIdBundle BUNDLE_OTHER = ExternalIdBundle.of(ID_ISIN_12345, ID_BAR, ID_OTHER1);

  private InMemoryConventionMaster _master;
  private ConventionDocument _addedDoc;

  /**
   *
   */
  @BeforeMethod
  public void setUp() {
    _master = new InMemoryConventionMaster();
    final ManageableConvention inputConvention = new MockConvention(NAME, BUNDLE_FULL, Currency.GBP);
    final ConventionDocument inputDoc = new ConventionDocument(inputConvention);
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
    final ConventionDocument result = _master.get(_addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemCnv", "1"), result.getUniqueId());
    assertEquals(_addedDoc, result);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchOneIdNoMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest(ID_OTHER1);
    final ConventionSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchOneIdIsin() {
    final ConventionSearchRequest request = new ConventionSearchRequest(ID_ISIN_12345);
    final ConventionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchOneIdCcid() {
    final ConventionSearchRequest request = new ConventionSearchRequest(ID_ISIN_12345);
    final ConventionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchOneBundleNoMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest(BUNDLE_OTHER);
    request.setExternalIdSearch(request.getExternalIdSearch().withSearchType(ExternalIdSearchType.ALL));
    final ConventionSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchOneBundleFull() {
    final ConventionSearchRequest request = new ConventionSearchRequest(BUNDLE_FULL);
    final ConventionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchOneBundlePart() {
    final ConventionSearchRequest request = new ConventionSearchRequest(BUNDLE_PART);
    final ConventionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchTwoBundlesNoMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID_OTHER1);
    request.addExternalId(ID_OTHER2);
    final ConventionSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchTwoBundlesOneMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID_ISIN_12345);
    request.addExternalId(ID_OTHER1);
    final ConventionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchTwoBundlesBothMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID_ISIN_12345);
    request.addExternalId(ID_FOO);
    final ConventionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchNameNoMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("No match");
    final ConventionSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchNameMatch() {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName(NAME);
    final ConventionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

}
