/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.regression;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;

/**
 * Unit tests for {@link MapReferenceDataProvider}.
 */
public class MapReferenceDataProviderTest {
  /** Reference data for two futures */
  private static final Map<String, Multimap<String, String>> REFERENCE_DATA = new HashMap<>();
  /** The reference data provider */
  private static final MapReferenceDataProvider PROVIDER;

  static {
    REFERENCE_DATA.put("EDZ5", ImmutableSetMultimap.<String, String>builder().put("Name", "Eurodollar future").put("Security type", "IR future").put("Expiry", "2015-12-16").build());
    REFERENCE_DATA.put("EDH6", ImmutableSetMultimap.<String, String>builder().put("Name", "Eurodollar future").put("Security type", "IR future").put("Expiry", "2016-03-16").build());
    PROVIDER = new MapReferenceDataProvider(REFERENCE_DATA);
  }

  /**
   * Tests the behaviour when the map is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap() {
    new MapReferenceDataProvider(null);
  }

  /**
   * Tests the behaviour when the request is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRequest() {
    PROVIDER.doBulkGet(null);
  }

  /**
   * Tests the behaviour when reference data is requested for an identifier that is not present in the map.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoReferenceDataForId() {
    final ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet("EDM6", "Name", false);
    PROVIDER.doBulkGet(request);
  }

  /**
   * Tests the behaviour when reference data is requested for an identifier that does not contain any information.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoFieldsAvailableForId() {
    final Map<String, Multimap<String, String>> referenceData = new HashMap<>();
    referenceData.put("EDZ5", ImmutableSetMultimap.<String, String>builder().put("Name", "Eurodollar future").put("Security type", "IR future").put("Expiry", "2015-12-16").putAll("Zone", Collections.<String>emptySet()).build());
    final MapReferenceDataProvider provider = new MapReferenceDataProvider(referenceData);
    // no data available for that field
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet("EDZ5", "Issuer", false);
    provider.doBulkGet(request);
    // empty data available for that field
    request = ReferenceDataProviderGetRequest.createGet("EDZ5", "Zone", false);
    provider.doBulkGet(request);
  }

  /**
   * Tests that the correct reference data is returned.
   */
  @Test
  public void testReferenceData() {
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet("EDZ5", Arrays.asList("Name", "Security type", "Expiry"), false);
    ReferenceDataProviderGetResult result = PROVIDER.doBulkGet(request);
    final ReferenceData edz5 = result.getReferenceData("EDZ5");
    assertEquals(edz5.getFieldValues().getByName("Name").getValue(), "Eurodollar future");
    assertEquals(edz5.getFieldValues().getByName("Security type").getValue(), "IR future");
    assertEquals(edz5.getFieldValues().getByName("Expiry").getValue(), "2015-12-16");
    request = ReferenceDataProviderGetRequest.createGet("EDH6", Arrays.asList("Name", "Security type", "Expiry"), false);
    result = PROVIDER.doBulkGet(request);
    final ReferenceData edh6 = result.getReferenceData("EDH6");
    assertEquals(edh6.getFieldValues().getByName("Name").getValue(), "Eurodollar future");
    assertEquals(edh6.getFieldValues().getByName("Security type").getValue(), "IR future");
    assertEquals(edh6.getFieldValues().getByName("Expiry").getValue(), "2016-03-16");
  }
}
