/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConfigMetaDataRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigMetaDataRequestTest extends AbstractFudgeBuilderTestCase {
  private static final String UID_SCHEME = "conf";
  private static final Boolean FETCH_TYPES = false;

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConfigMetaDataRequest request = new ConfigMetaDataRequest();
    assertTrue(request.isConfigTypes());
    request.setConfigTypes(FETCH_TYPES);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.getUniqueIdScheme(), UID_SCHEME);
    assertFalse(request.isConfigTypes());
    assertEquals(request, request);
    assertNotEquals(null, request);
    assertEquals(request.toString(), "ConfigMetaDataRequest{uniqueIdScheme=conf, configTypes=false}");
    final ConfigMetaDataRequest other = new ConfigMetaDataRequest();
    other.setConfigTypes(FETCH_TYPES);
    other.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setConfigTypes(!FETCH_TYPES);
    assertNotEquals(request, other);
    other.setConfigTypes(FETCH_TYPES);
    other.setUniqueIdScheme("uid");
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConfigMetaDataRequest request = new ConfigMetaDataRequest();
    request.setConfigTypes(FETCH_TYPES);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.propertyNames().size(), 2);
    assertEquals(request.metaBean().configTypes().get(request), FETCH_TYPES);
    assertEquals(request.metaBean().uniqueIdScheme().get(request), UID_SCHEME);
    assertEquals(request.property("configTypes").get(), FETCH_TYPES);
    assertEquals(request.property("uniqueIdScheme").get(), UID_SCHEME);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ConfigMetaDataRequest request = new ConfigMetaDataRequest();
    assertEncodeDecodeCycle(ConfigMetaDataRequest.class, request);
    request.setConfigTypes(FETCH_TYPES);
    assertEncodeDecodeCycle(ConfigMetaDataRequest.class, request);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEncodeDecodeCycle(ConfigMetaDataRequest.class, request);
  }
}
