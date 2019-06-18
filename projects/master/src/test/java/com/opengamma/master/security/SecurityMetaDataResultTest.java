/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityMetaDataResult}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityMetaDataResultTest extends AbstractFudgeBuilderTestCase {
  private static final List<String> SECURITY_TYPES = Arrays.asList("EQUITY", "FUTURE", "SWAP");
  private static final String VERSION = "v1.1";

  /**
   * Tests that the security types cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurityType() {
    new SecurityMetaDataResult().setSecurityTypes(null);
  }

  /**
   * Tests that the schema version can be null.
   */
  @Test
  public void testNullSchemaVersion() {
    new SecurityMetaDataResult().setSchemaVersion(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final SecurityMetaDataResult result = new SecurityMetaDataResult();
    result.setSchemaVersion(VERSION);
    result.setSecurityTypes(SECURITY_TYPES);
    assertEquals(result.getSchemaVersion(), VERSION);
    assertEquals(result.getSecurityTypes(), SECURITY_TYPES);
    assertEquals(result, result);
    assertNotEquals(null, result);
    assertEquals(result.toString(), "SecurityMetaDataResult{securityTypes=[EQUITY, FUTURE, SWAP], schemaVersion=v1.1}");
    final SecurityMetaDataResult other = new SecurityMetaDataResult();
    other.setSchemaVersion(VERSION);
    other.setSecurityTypes(SECURITY_TYPES);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other.setSchemaVersion("v1.5");
    assertNotEquals(result, other);
    other.setSchemaVersion(VERSION);
    other.setSecurityTypes(Collections.<String>emptyList());
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final SecurityMetaDataResult result = new SecurityMetaDataResult();
    result.setSchemaVersion(VERSION);
    result.setSecurityTypes(SECURITY_TYPES);
    assertEquals(result.metaBean().schemaVersion().get(result), VERSION);
    assertEquals(result.metaBean().securityTypes().get(result), SECURITY_TYPES);
    assertEquals(result.property("schemaVersion").get(), VERSION);
    assertEquals(result.property("securityTypes").get(), SECURITY_TYPES);
    result.setSchemaVersion(null);
    assertNull(result.metaBean().schemaVersion().get(result));
    assertEquals(result.metaBean().securityTypes().get(result), SECURITY_TYPES);
    assertNull(result.property("schemaVersion").get());
    assertEquals(result.property("securityTypes").get(), SECURITY_TYPES);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SecurityMetaDataResult result = new SecurityMetaDataResult();
    result.setSchemaVersion(VERSION);
    result.setSecurityTypes(SECURITY_TYPES);
    assertEncodeDecodeCycle(SecurityMetaDataResult.class, result);
    result.setSchemaVersion(null);
    assertEncodeDecodeCycle(SecurityMetaDataResult.class, result);
    result.setSecurityTypes(Collections.<String>emptyList());
    assertEncodeDecodeCycle(SecurityMetaDataResult.class, result);
  }
}
