/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConventionMetaDataRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class ConventionMetaDataRequestTest extends AbstractFudgeBuilderTestCase {
  private static final String UID_SCHEME = "conv";
  private static final Boolean FETCH_TYPES = false;
  private static final Boolean FETCH_VERSION = true;

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConventionMetaDataRequest request = new ConventionMetaDataRequest();
    assertTrue(request.isConventionTypes());
    assertFalse(request.isSchemaVersion());
    request.setSchemaVersion(FETCH_VERSION);
    request.setConventionTypes(FETCH_TYPES);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.getUniqueIdScheme(), UID_SCHEME);
    assertFalse(request.isConventionTypes());
    assertTrue(request.isSchemaVersion());
    assertEquals(request, request);
    assertNotEquals(null, request);
    assertEquals(request.toString(), "ConventionMetaDataRequest{uniqueIdScheme=conv, conventionTypes=false, schemaVersion=true}");
    final ConventionMetaDataRequest other = new ConventionMetaDataRequest();
    other.setSchemaVersion(FETCH_VERSION);
    other.setConventionTypes(FETCH_TYPES);
    other.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setSchemaVersion(!FETCH_VERSION);
    assertNotEquals(request, other);
    other.setSchemaVersion(FETCH_VERSION);
    other.setConventionTypes(!FETCH_TYPES);
    assertNotEquals(request, other);
    other.setConventionTypes(FETCH_TYPES);
    other.setUniqueIdScheme("uid");
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConventionMetaDataRequest request = new ConventionMetaDataRequest();
    request.setSchemaVersion(FETCH_VERSION);
    request.setConventionTypes(FETCH_TYPES);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.propertyNames().size(), 3);
    assertEquals(request.metaBean().schemaVersion().get(request), FETCH_VERSION);
    assertEquals(request.metaBean().conventionTypes().get(request), FETCH_TYPES);
    assertEquals(request.metaBean().uniqueIdScheme().get(request), UID_SCHEME);
    assertEquals(request.property("schemaVersion").get(), FETCH_VERSION);
    assertEquals(request.property("conventionTypes").get(), FETCH_TYPES);
    assertEquals(request.property("uniqueIdScheme").get(), UID_SCHEME);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final ConventionMetaDataRequest request = new ConventionMetaDataRequest();
    assertEncodeDecodeCycle(ConventionMetaDataRequest.class, request);
    request.setSchemaVersion(FETCH_VERSION);
    assertEncodeDecodeCycle(ConventionMetaDataRequest.class, request);
    request.setConventionTypes(FETCH_TYPES);
    assertEncodeDecodeCycle(ConventionMetaDataRequest.class, request);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEncodeDecodeCycle(ConventionMetaDataRequest.class, request);
  }
}
