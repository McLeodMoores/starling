/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.region;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.net.URI;

import org.testng.annotations.Test;

import com.opengamma.core.region.RegionClassification;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.MockUriInfo;

/**
 * Tests for {@link WebRegionUris}.
 */
@Test(groups = TestGroup.UNIT)
public class WebRegionUrisTest {
  private static final SimpleRegion REGION = new SimpleRegion();
  private static final ExternalId ID = ExternalId.of("reg", "au");
  private static final String URI_REGION_ID = "reg~au";
  private static final String URI_VERSION_ID = "reg~ver";
  private static final RegionDocument DOCUMENT = new RegionDocument(REGION);
  private static final InMemoryRegionMaster MASTER = new InMemoryRegionMaster();
  private static final MockUriInfo URI_INFO = new MockUriInfo(true);
  private static final WebRegionData DATA = new WebRegionData();
  private static final WebRegionUris URIS = new WebRegionUris(DATA);
  static {
    REGION.setCurrency(Currency.AUD);
    REGION.setClassification(RegionClassification.INDEPENDENT_STATE);
    REGION.addExternalId(ID);
    MASTER.add(DOCUMENT);
    DATA.setRegion(DOCUMENT);
    DATA.setUriRegionId(URI_REGION_ID);
    DATA.setUriVersionId(URI_VERSION_ID);
    DATA.setUriInfo(URI_INFO);
  }

  /**
   * Tests the base.
   */
  public void testBase() {
    assertEquals(URIS.base().getPath(), "/regions");
  }

  /**
   * Tests the regions.
   */
  public void testRegions() {
    assertEquals(URIS.regions().getPath(), "/regions");
  }

  /**
   * Tests the regions.
   */
  public void testRegionsNullIdentifier() {
    assertEquals(URIS.regions((ExternalId) null).getPath(), "/regions");
  }

  /**
   * Tests the regions.
   */
  public void testRegionsIdentifier() {
    assertEquals(URIS.regions(ID).getPath(), "/regions");
  }

  /**
   * Tests the regions.
   */
  public void testRegionsNullIdentifierBundle() {
    assertEquals(URIS.regions((ExternalIdBundle) null).getPath(), "/regions");
  }

  /**
   * Tests the regions.
   */
  public void testRegionsIdentifierBundle() {
    assertEquals(URIS.regions(ID.toBundle()).getPath(), "/regions");
  }

  /**
   * Tests the regions by currency.
   */
  public void testRegionsByCurrencyBadValue() {
    final URI uri = URIS.regionsByCurrency("AB");
    assertEquals(uri.getPath(), "/regions/MemReg~1");
    assertNull(uri.getQuery());
  }

  /**
   * Tests the regions by currency.
   */
  public void testRegionsByCurrency() {
    final URI uri = URIS.regionsByCurrency("AUD");
    assertEquals(uri.getPath(), "/regions");
    assertEquals(uri.getQuery(), "&idscheme.0=ISO_CURRENCY_ALPHA3&idvalue.0=AUD");
  }
}
