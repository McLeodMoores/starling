/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.region;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.id.UniqueId;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebRegionData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebRegionDataTest extends AbstractBeanTestCase {
  private static final String REGION_URI = "region/AU";
  private static final String VERSION_URI = "version=1";
  private static final SimpleRegion REGION = new SimpleRegion();
  static {
    REGION.setCountry(Country.AU);
    REGION.setCurrency(Currency.AUD);
  }
  private static final RegionDocument DOCUMENT = new RegionDocument(REGION);
  private static final List<RegionDocument> PARENTS = Collections.singletonList(DOCUMENT);
  private static final List<RegionDocument> CHILDREN = Collections.emptyList();
  private static final WebRegionData DATA = new WebRegionData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("reg", "0"));
    DATA.setRegion(DOCUMENT);
    DATA.setRegionParents(PARENTS);
    DATA.setRegionChildren(CHILDREN);
    DATA.setUriRegionId(REGION_URI);
    DATA.setUriVersionId(VERSION_URI);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebRegionData.class,
        Arrays.asList("uriRegionId", "uriVersionId", "region", "regionParents", "regionChildren", "versioned"),
        Arrays.asList(REGION_URI, VERSION_URI, DOCUMENT, PARENTS, CHILDREN, DOCUMENT),
        Arrays.asList(VERSION_URI, REGION_URI, new RegionDocument(new SimpleRegion()), CHILDREN, PARENTS, new RegionDocument(new SimpleRegion())));
  }

  /**
   * Tests getting the best region if the override id is not null.
   */
  public void testBestRegionOverrideId() {
    final UniqueId uid = UniqueId.of("reg", "1");
    assertEquals(DATA.getBestRegionUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best region if there is no region document.
   */
  public void testBestRegionNoRegionDocument() {
    final WebRegionData data = DATA.clone();
    data.setRegion(null);
    assertEquals(data.getBestRegionUriId(null), REGION_URI);
  }

  /**
   * Tests getting the best region from the document.
   */
  public void testBestRegionFromDocument() {
    assertEquals(DATA.getBestRegionUriId(null), DOCUMENT.getUniqueId().toString());
  }
}
