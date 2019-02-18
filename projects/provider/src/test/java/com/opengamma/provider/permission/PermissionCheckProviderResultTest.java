/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link PermissionCheckProviderResult}.
 */
@Test(groups = TestGroup.UNIT)
public class PermissionCheckProviderResultTest extends AbstractBeanTestCase {

  /**
   * Tests requested permissions for allowed requests.
   */
  public void isPermitted() {
    final ImmutableMap<String, Boolean> checked = ImmutableMap.of("Data:12345", Boolean.FALSE, "Data:67890", Boolean.TRUE);
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.of(checked);
    assertFalse(result.isPermitted("Data:12345"));
    assertTrue(result.isPermitted("Data:67890"));
    assertFalse(result.isPermittedAll(ImmutableList.of("Data:12345")));
    assertTrue(result.isPermittedAll(ImmutableList.of("Data:67890")));
    assertFalse(result.isPermittedAll(ImmutableList.of("Data:12345", "Data:67890")));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests unauthenticated requests.
   */
  public void isPermittedUnauthenticated() {
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    assertFalse(result.isPermitted("Data:12345"));
    assertFalse(result.isPermittedAll(ImmutableList.of("Data:12345")));
  }

  /**
   * Tests that checkPermitted throws an exception for unauthenticated requests.
   */
  @Test(expectedExceptions = UnauthenticatedException.class)
  public void checkPermittedUnauthenticated() {
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    result.checkPermitted("Data:12345");
  }

  /**
   * Tests that checkErrors throws an exception for unauthenticated requests.
   */
  @Test(expectedExceptions = UnauthenticatedException.class)
  public void checkErrorsUnauthenticated() {
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    result.checkErrors();
  }

  //-------------------------------------------------------------------------
  /**
   * Tests unauthorized requests.
   */
  public void isPermittedUnauthorized() {
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    assertFalse(result.isPermitted("Data:12345"));
    assertFalse(result.isPermittedAll(ImmutableList.of("Data:12345")));
  }

  /**
   * Tests that checkPermitted throws an exception for unauthorized requests.
   */
  @Test(expectedExceptions = AuthorizationException.class)
  public void checkPermittedUnauthorized() {
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    result.checkPermitted("Data:12345");
  }

  /**
   * Tests that checkErrors throws an exception for unauthorized requests.
   */
  @Test(expectedExceptions = AuthorizationException.class)
  public void checkErrorsUnauthorized() {
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    result.checkErrors();
  }

  // -------------------------------------------------------------------------
  /**
   * Tests that an exception is thrown if there is no entry for a requested
   * permission.
   */
  @Test(expectedExceptions = AuthorizationException.class)
  public void checkPermittedNullPermission() {
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.of(Collections.<String, Boolean> emptyMap());
    result.checkPermitted("request");
  }

  /**
   * Tests that an exception is thrown if there is no entry for a requested
   * permission.
   */
  @Test(expectedExceptions = AuthorizationException.class)
  public void checkPermittedFalsePermission() {
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.of(Collections.singletonMap("request", false));
    result.checkPermitted("request");
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    final ImmutableMap<String, Boolean> permissions = ImmutableMap.of("perm1", false, "perm2", true);
    final ImmutableMap<String, Boolean> other = ImmutableMap.of("perm1", true, "perm2", true);
    return new JodaBeanProperties<>(PermissionCheckProviderResult.class, Arrays.asList("checkedPermissions", "authenticationError", "authorizationError"),
        Arrays.asList(permissions, "authenticationError", "authorizationError"), Arrays.asList(other, "authorizationError", "authenticationError"));
  }
}
