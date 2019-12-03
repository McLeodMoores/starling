/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.permission.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ProviderBasedPermissionResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class ProviderBasedPermissionResolverTest {
  private static final String PREFIX = "prefix";
  private static final PermissionCheckProvider PROVIDER = new PermissivePermissionCheckProvider();

  /**
   * Tests that the prefix cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPrefix() {
    new ProviderBasedPermissionResolver(null, PROVIDER);
  }

  /**
   * Tests that the provider cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProvider() {
    new ProviderBasedPermissionResolver(PREFIX, null);
  }

  /**
   * Tests the object.
   */
  public void test() {
    final ProviderBasedPermissionResolver resolver = new ProviderBasedPermissionResolver(PREFIX, PROVIDER);
    assertEquals(resolver.getPrefix(), PREFIX);
    assertTrue(resolver.resolvePermission("perm") instanceof ProviderBasedPermission);
  }
}
