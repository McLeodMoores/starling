/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.permission;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link PermissionCheckProviderRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class PermissionCheckProviderRequestTest extends AbstractBeanTestCase {
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("user_id", "1");
  private static final String NETWORK_ADDRESS = "127.127.127.127";
  private static final Set<String> REQUESTED_PERMISSIONS = new HashSet<>(Arrays.asList("perm1", "perm2"));

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(PermissionCheckProviderRequest.class, Arrays.asList("userIdBundle", "networkAddress", "requestedPermissions"),
        Arrays.asList(IDS, NETWORK_ADDRESS, REQUESTED_PERMISSIONS),
        Arrays.asList(ExternalIdBundle.of("user_id", "2"), "127.0.0.0", Collections.singleton("perm3")));
  }

  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateIdArrayNullId() {
    PermissionCheckProviderRequest.createGet((ExternalId) null, NETWORK_ADDRESS, "perm1", "perm2");
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateIdArrayNullIdBundle() {
    PermissionCheckProviderRequest.createGet((ExternalIdBundle) null, NETWORK_ADDRESS, "perm1", "perm2");
  }

  /**
   * Tests that the requested permissions cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testCreateIdNullPermissionsArray() {
    PermissionCheckProviderRequest.createGet(ExternalId.of("user_id", "1"), NETWORK_ADDRESS, (String[]) null);
  }

  /**
   * Tests that the requested permissions cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testCreateIdBundleNullPermissionsArray() {
    PermissionCheckProviderRequest.createGet(IDS, NETWORK_ADDRESS, (String[]) null);
  }

  /**
   * Tests that the external id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateIdIterableNullId() {
    PermissionCheckProviderRequest.createGet((ExternalId) null, NETWORK_ADDRESS, Arrays.asList("perm1", "perm2"));
  }

  /**
   * Tests that the external id bundle cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateIdIterableNullIdBundle() {
    PermissionCheckProviderRequest.createGet((ExternalIdBundle) null, NETWORK_ADDRESS, Arrays.asList("perm1", "perm2"));
  }

  /**
   * Tests that the requested permissions cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateIdNullPermissionsIterable() {
    PermissionCheckProviderRequest.createGet(ExternalId.of("user_id", "1"), NETWORK_ADDRESS, (Iterable<String>) null);
  }

  /**
   * Tests that the requested permissions cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateIdBundleNullPermissionsIterable() {
    PermissionCheckProviderRequest.createGet(IDS, NETWORK_ADDRESS, (Iterable<String>) null);
  }

  /**
   * Tests static constructor equivalence.
   */
  public void testStaticConstructor() {
    final ExternalId eid = ExternalId.of("user_id", "1");
    final PermissionCheckProviderRequest request1 = PermissionCheckProviderRequest.createGet(eid, NETWORK_ADDRESS,
        REQUESTED_PERMISSIONS.toArray(new String[0]));
    final PermissionCheckProviderRequest request2 = PermissionCheckProviderRequest.createGet(eid.toBundle(), NETWORK_ADDRESS,
        REQUESTED_PERMISSIONS.toArray(new String[0]));
    final PermissionCheckProviderRequest request3 = PermissionCheckProviderRequest.createGet(eid, NETWORK_ADDRESS, REQUESTED_PERMISSIONS);
    final PermissionCheckProviderRequest request4 = PermissionCheckProviderRequest.createGet(eid.toBundle(), NETWORK_ADDRESS, REQUESTED_PERMISSIONS);
    assertEquals(request1, request2);
    assertEquals(request1, request3);
    assertEquals(request1, request4);
  }
}
