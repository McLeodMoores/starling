/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PermissivePermissionCheckProviderTest {

  /**
   * Tests that all are permitted.
   */
  @Test
  public void allTrueWithRequest() {
    final PermissivePermissionCheckProvider test = new PermissivePermissionCheckProvider();
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(ExternalIdBundle.of("A", "B"), "127.0.0.1", "A", "B", "C");
    final PermissionCheckProviderResult resultHolder = test.isPermitted(request);
    assertNotNull(resultHolder);
    assertNotNull(resultHolder.getCheckedPermissions());

    final Map<String, Boolean> permissionCheckResult = resultHolder.getCheckedPermissions();
    assertPermissionResult(permissionCheckResult);

    resultHolder.checkErrors();
    resultHolder.checkPermitted("A");
    assertTrue(resultHolder.isPermitted("A"));
  }

  /**
   * Tests that all are permitted.
   */
  public void allTrueWithIdIpAddressPermissions() {
    final PermissivePermissionCheckProvider test = new PermissivePermissionCheckProvider();
    final Map<String, Boolean> result = test.isPermitted(ExternalIdBundle.of("A", "B"), "127.0.0.1", Sets.newHashSet("A", "B", "C"));
    assertNotNull(result);
    assertPermissionResult(result);
  }

  private static void assertPermissionResult(final Map<String, Boolean> permissionCheckResult) {
    assertEquals(permissionCheckResult.size(), 3);
    assertTrue(permissionCheckResult.get("A"));
    assertTrue(permissionCheckResult.get("B"));
    assertTrue(permissionCheckResult.get("C"));
  }

}
