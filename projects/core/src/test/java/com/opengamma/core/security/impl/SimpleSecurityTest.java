/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;


import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimpleSecurity}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleSecurityTest {

  private static final UniqueId UID = UniqueId.of("P", "A");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");

  /**
   * Tests the constructor.
   */
  public void testConstructor() {
    final SimpleSecurity test = new SimpleSecurity(UID, BUNDLE, "Type", "Name");
    assertEquals(UID, test.getUniqueId());
    assertEquals(BUNDLE, test.getExternalIdBundle());
    assertEquals("Type", test.getSecurityType());
    assertEquals("Name", test.getName());
  }

  /**
   * Tests that the external ids cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullBundle() {
    new SimpleSecurity(UID, null, "Type", "Name");
  }

  /**
   * Testt that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullType() {
    new SimpleSecurity(UID, BUNDLE, null, "Name");
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullName() {
    new SimpleSecurity(UID, BUNDLE, "Type", null);
  }

}
