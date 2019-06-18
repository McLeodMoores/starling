/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityMetaDataRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityMetaDataRequestTest extends AbstractFudgeBuilderTestCase {
  private static final String UID_SCHEME = "sec";
  private static final Boolean FETCH_TYPES = false;
  private static final Boolean FETCH_VERSION = true;

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final SecurityMetaDataRequest request = new SecurityMetaDataRequest();
    assertTrue(request.isSecurityTypes());
    assertFalse(request.isSchemaVersion());
    request.setSchemaVersion(FETCH_VERSION);
    request.setSecurityTypes(FETCH_TYPES);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.getUniqueIdScheme(), UID_SCHEME);
    assertFalse(request.isSecurityTypes());
    assertTrue(request.isSchemaVersion());
    assertEquals(request, request);
    assertNotEquals(null, request);
    assertEquals(request.toString(), "SecurityMetaDataRequest{uniqueIdScheme=sec, securityTypes=false, schemaVersion=true}");
    final SecurityMetaDataRequest other = new SecurityMetaDataRequest();
    other.setSchemaVersion(FETCH_VERSION);
    other.setSecurityTypes(FETCH_TYPES);
    other.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setSchemaVersion(!FETCH_VERSION);
    assertNotEquals(request, other);
    other.setSchemaVersion(FETCH_VERSION);
    other.setSecurityTypes(!FETCH_TYPES);
    assertNotEquals(request, other);
    other.setSecurityTypes(FETCH_TYPES);
    other.setUniqueIdScheme("uid");
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final SecurityMetaDataRequest request = new SecurityMetaDataRequest();
    request.setSchemaVersion(FETCH_VERSION);
    request.setSecurityTypes(FETCH_TYPES);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.propertyNames().size(), 3);
    assertEquals(request.metaBean().schemaVersion().get(request), FETCH_VERSION);
    assertEquals(request.metaBean().securityTypes().get(request), FETCH_TYPES);
    assertEquals(request.metaBean().uniqueIdScheme().get(request), UID_SCHEME);
    assertEquals(request.property("schemaVersion").get(), FETCH_VERSION);
    assertEquals(request.property("securityTypes").get(), FETCH_TYPES);
    assertEquals(request.property("uniqueIdScheme").get(), UID_SCHEME);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SecurityMetaDataRequest request = new SecurityMetaDataRequest();
    assertEncodeDecodeCycle(SecurityMetaDataRequest.class, request);
    request.setSchemaVersion(FETCH_VERSION);
    assertEncodeDecodeCycle(SecurityMetaDataRequest.class, request);
    request.setSecurityTypes(FETCH_TYPES);
    assertEncodeDecodeCycle(SecurityMetaDataRequest.class, request);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEncodeDecodeCycle(SecurityMetaDataRequest.class, request);
  }
}
