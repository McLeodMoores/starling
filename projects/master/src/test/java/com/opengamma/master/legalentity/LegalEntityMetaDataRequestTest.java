/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link LegalEntityMetaDataRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class LegalEntityMetaDataRequestTest extends AbstractFudgeBuilderTestCase {
  private static final String UID_SCHEME = "len";
  private static final Boolean FETCH_VERSION = true;

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final LegalEntityMetaDataRequest request = new LegalEntityMetaDataRequest();
    assertFalse(request.isSchemaVersion());
    request.setSchemaVersion(FETCH_VERSION);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.getUniqueIdScheme(), UID_SCHEME);
    assertTrue(request.isSchemaVersion());
    assertEquals(request, request);
    assertNotEquals(null, request);
    assertEquals(request.toString(), "LegalEntityMetaDataRequest{uniqueIdScheme=len, schemaVersion=true}");
    final LegalEntityMetaDataRequest other = new LegalEntityMetaDataRequest();
    other.setSchemaVersion(FETCH_VERSION);
    other.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request, other);
    assertEquals(request.hashCode(), other.hashCode());
    other.setSchemaVersion(!FETCH_VERSION);
    assertNotEquals(request, other);
    other.setSchemaVersion(FETCH_VERSION);
    other.setUniqueIdScheme("uid");
    assertNotEquals(request, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final LegalEntityMetaDataRequest request = new LegalEntityMetaDataRequest();
    request.setSchemaVersion(FETCH_VERSION);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEquals(request.propertyNames().size(), 2);
    assertEquals(request.metaBean().schemaVersion().get(request), FETCH_VERSION);
    assertEquals(request.metaBean().uniqueIdScheme().get(request), UID_SCHEME);
    assertEquals(request.property("schemaVersion").get(), FETCH_VERSION);
    assertEquals(request.property("uniqueIdScheme").get(), UID_SCHEME);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final LegalEntityMetaDataRequest request = new LegalEntityMetaDataRequest();
    assertEncodeDecodeCycle(LegalEntityMetaDataRequest.class, request);
    request.setSchemaVersion(FETCH_VERSION);
    assertEncodeDecodeCycle(LegalEntityMetaDataRequest.class, request);
    request.setUniqueIdScheme(UID_SCHEME);
    assertEncodeDecodeCycle(LegalEntityMetaDataRequest.class, request);
  }
}
