/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryRegionMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryRegionMasterTest {

  private static final String NAME = "France";
  private static final ExternalId ID_COUNTRY = ExternalSchemes.countryRegionId(Country.FR);
  private static final ExternalId ID_CURENCY = ExternalSchemes.currencyRegionId(Currency.EUR);
  private static final ExternalId ID_TIME_ZONE = ExternalSchemes.timeZoneRegionId(ZoneId.of("Europe/Paris"));
  private static final ExternalId ID_OTHER1 = ExternalId.of("TEST_SCHEME", "The French");
  private static final ExternalId ID_OTHER2 = ExternalId.of("TEST_SCHEME", "France");
  private static final ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(ID_COUNTRY, ID_TIME_ZONE, ID_CURENCY);
  private static final ExternalIdBundle BUNDLE_PART = ExternalIdBundle.of(ID_COUNTRY, ID_CURENCY);
  private static final ExternalIdBundle BUNDLE_OTHER = ExternalIdBundle.of(ID_COUNTRY, ID_TIME_ZONE, ID_OTHER1);

  private InMemoryRegionMaster _master;
  private RegionDocument _addedDoc;

  /**
   *
   */
  @BeforeMethod
  public void setUp() {
    _master = new InMemoryRegionMaster();
    final ManageableRegion inputRegion = new ManageableRegion();
    inputRegion.setName(NAME);
    inputRegion.setFullName(NAME);
    inputRegion.setClassification(RegionClassification.INDEPENDENT_STATE);
    inputRegion.setCountry(Country.FR);
    inputRegion.setCurrency(Currency.EUR);
    inputRegion.setTimeZone(ZoneId.of("Europe/Paris"));
    final RegionDocument inputDoc = new RegionDocument(inputRegion);
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
    final RegionDocument result = _master.get(_addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemReg", "1"), result.getUniqueId());
    assertEquals(_addedDoc, result);
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchOneIdNoMatch() {
    final RegionSearchRequest request = new RegionSearchRequest(ID_OTHER1);
    final RegionSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchOneIdMic() {
    final RegionSearchRequest request = new RegionSearchRequest(ID_COUNTRY);
    final RegionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchOneIdCcid() {
    final RegionSearchRequest request = new RegionSearchRequest(ID_COUNTRY);
    final RegionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchOneBundleNoMatch() {
    final RegionSearchRequest request = new RegionSearchRequest(BUNDLE_OTHER);
    request.setExternalIdSearch(request.getExternalIdSearch().withSearchType(ExternalIdSearchType.ALL));
    final RegionSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchOneBundleFull() {
    final RegionSearchRequest request = new RegionSearchRequest(BUNDLE_FULL);
    final RegionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchOneBundlePart() {
    final RegionSearchRequest request = new RegionSearchRequest(BUNDLE_PART);
    final RegionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchTwoBundlesNoMatch() {
    final RegionSearchRequest request = new RegionSearchRequest();
    request.addExternalId(ID_OTHER1);
    request.addExternalId(ID_OTHER2);
    final RegionSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchTwoBundlesOneMatch() {
    final RegionSearchRequest request = new RegionSearchRequest();
    request.addExternalId(ID_COUNTRY);
    request.addExternalId(ID_OTHER1);
    final RegionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  /**
   *
   */
  public void testSearchTwoBundlesBothMatch() {
    final RegionSearchRequest request = new RegionSearchRequest();
    request.addExternalId(ID_COUNTRY);
    request.addExternalId(ID_CURENCY);
    final RegionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchNameNoMatch() {
    final RegionSearchRequest request = new RegionSearchRequest();
    request.setName("No match");
    final RegionSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchNameMatch() {
    final RegionSearchRequest request = new RegionSearchRequest();
    request.setName(NAME);
    final RegionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  public void testSearchClassificationNoMatch() {
    final RegionSearchRequest request = new RegionSearchRequest();
    request.setClassification(RegionClassification.DEPENDENCY);
    final RegionSearchResult result = _master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  /**
   *
   */
  public void testSearchClassificationMatch() {
    final RegionSearchRequest request = new RegionSearchRequest();
    request.setClassification(RegionClassification.INDEPENDENT_STATE);
    final RegionSearchResult result = _master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_addedDoc, result.getFirstDocument());
  }

}
