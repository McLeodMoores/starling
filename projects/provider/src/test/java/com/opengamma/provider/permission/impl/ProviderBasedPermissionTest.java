/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.core.user.UserPrincipals;
import com.opengamma.core.user.impl.SimpleUserPrincipals;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.auth.ShiroPermissionResolver;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT, singleThreaded = true)
public class ProviderBasedPermissionTest {

  private static final Permission WILDCARD_PERM = new ShiroPermissionResolver().resolvePermission("Data:12345");
  private static final ExternalIdBundle USER_BUNDLE = ExternalIdBundle.of("DATAUSER", "TEST");
  private static final UserPrincipals PRINCIPALS;
  static {
    final SimpleUserPrincipals principals = new SimpleUserPrincipals();
    principals.setUserName("Tester");
    principals.setAlternateIds(USER_BUNDLE);
    principals.setNetworkAddress("1.1.1.1");
    PRINCIPALS = principals;
  }

  /**
   * Sets up the local context.
   */
  @BeforeMethod
  public void setUp() {
    ThreadContext.bind(new DefaultSecurityManager());
  }

  /**
   * Tears down the local context.
   */
  @AfterMethod
  public void tearDown() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the information from the argument is used.
   */
  public void impliesTrue() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    when(provider.isPermitted(USER_BUNDLE, "1.1.1.1", "Data:12345")).thenReturn(Boolean.TRUE);
    final ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    final ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test1.implies(test2));
  }

  /**
   * Tests that the information from the argument is used.
   */
  public void impliesFalse() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    when(provider.isPermitted(USER_BUNDLE, "1.1.1.1", "Data:12345")).thenReturn(Boolean.FALSE);
    final ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    final ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test1.implies(test2));
  }

  /**
   * Tests that wildcard permissions are not implied from an existing
   * permission.
   */
  public void impliesFalseAgainstWildcardPermission() {
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test.implies(WILDCARD_PERM));
    assertFalse(WILDCARD_PERM.implies(test));
  }

  /**
   * Tests that the permission is not permitted if there is no user.
   */
  public void noUserImpliesFalse() {
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    when(provider.isPermitted(USER_BUNDLE, "1.1.1.1", "Data:12345")).thenReturn(Boolean.FALSE);
    final ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    final ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test1.implies(test2));
  }
  //-------------------------------------------------------------------------
  /**
   * Tests that the information from the argument is used.
   */
  public void checkImpliesTrue() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.TRUE));
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    final ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test1.checkImplies(test2));
  }

  /**
   * Tests that the information from the argument is used.
   */
  public void checkImpliesFalse() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.FALSE));
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    final ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test1.checkImplies(test2));
  }

  /**
   * Tests that the information from the argument is used.
   */
  @Test(expectedExceptions = UnauthenticatedException.class)
  public void checkImpliesUnauthenticated() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    final ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    test1.checkImplies(test2);
  }

  /**
   * Tests that the information from the argument is used.
   */
  @Test(expectedExceptions = AuthorizationException.class)
  public void checkImpliesUnauthorized() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    final ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    test1.checkImplies(test2);
  }

  /**
   * Tests that the information from the argument is used.
   */
  public void checkImpliesFalseAgainstWildcardPermission() {
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test.checkImplies(WILDCARD_PERM));
  }

  /**
   * Tests that the permission treated as if it is unauthenticated if there is
   * no user.
   */
  @Test(expectedExceptions = UnauthenticatedException.class)
  public void noUserCheckImplies() {
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    when(provider.isPermitted(USER_BUNDLE, "1.1.1.1", "Data:12345")).thenReturn(Boolean.FALSE);
    final ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    final ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    test1.checkImplies(test2);
  }
  //-------------------------------------------------------------------------
  /**
   * Tests that if all have permission then checkImpliesAll returns true.
   */
  public void checkImpliesAllOneTrue() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.TRUE));
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    final Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1), true));
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1), false));
  }

  /**
   * Tests that if all have permission then checkImpliesAll returns true.
   */
  public void checkImpliesAllManyTrue() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(
        USER_BUNDLE, "1.1.1.1", "Data:12345", "Data:67890");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.TRUE, "Data:67890", Boolean.TRUE));
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    final Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    final Permission required2 = new ProviderBasedPermission(provider, "Data:67890");
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1, required2), true));
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1, required2), false));
  }

  /**
   * Tests that if not all have permission then checkImpliesAll returns false.
   */
  public void checkImpliesAllOneFalse() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.FALSE));
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    final Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    assertFalse(test.checkImpliesAll(ImmutableList.of(required1), true));
    assertFalse(test.checkImpliesAll(ImmutableList.of(required1), false));
  }

  /**
   * Tests that if not all have permission then checkImpliesAll returns false.
   */
  public void checkImpliesAllManyFalse() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(
        USER_BUNDLE, "1.1.1.1", "Data:12345", "Data:67890");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.of(
        ImmutableMap.of("Data:12345", Boolean.FALSE, "Data:67890", Boolean.TRUE));
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    final Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    final Permission required2 = new ProviderBasedPermission(provider, "Data:67890");
    assertFalse(test.checkImpliesAll(ImmutableList.of(required1, required2), true));
    assertFalse(test.checkImpliesAll(ImmutableList.of(required1, required2), false));
  }

  /**
   * Tests an unauthenticated check.
   */
  @Test(expectedExceptions = UnauthenticatedException.class)
  public void checkImpliesAllUnauthenticated() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthenticationError("Not authenticated");
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    final Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1), true));
  }

  /**
   * Tests an unauthorized check.
   */
  @Test(expectedExceptions = AuthorizationException.class)
  public void checkImpliesAllUnauthorized() {
    AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, PRINCIPALS);
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(USER_BUNDLE, "1.1.1.1", "Data:12345");
    final PermissionCheckProviderResult result = PermissionCheckProviderResult.ofAuthorizationError("Not authorized");
    when(provider.isPermitted(request)).thenReturn(result);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data");
    final Permission required1 = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test.checkImpliesAll(ImmutableList.of(required1), true));
  }

  /**
   * Tests wildcard permissions.
   */
  public void checkImpliesAllAgainstWildcardPermission() {
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data:12345");
    final Permission required = new ProviderBasedPermission(provider, "Data:12345");
    assertNull(test.checkImpliesAll(ImmutableList.of(required, WILDCARD_PERM), true));
    assertNull(test.checkImpliesAll(ImmutableList.of(required, WILDCARD_PERM), false));
  }

  /**
   * Tests that an empty permission collection implies that all are allowed.
   */
  public void checkImpliesAllEmptyPermissions() {
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final ProviderBasedPermission test = new ProviderBasedPermission(provider, "Data:12345");
    assertTrue(test.checkImpliesAll(Collections.<Permission> emptySet(), false));
    assertTrue(test.checkImpliesAll(Collections.<Permission> emptySet(), true));
  }

  /**
   * Tests that the permission treated as if it is unauthenticated if there is
   * no user.
   */
  @Test(expectedExceptions = UnauthenticatedException.class)
  public void noUserCheckImpliesAll() {
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    when(provider.isPermitted(USER_BUNDLE, "1.1.1.1", "Data:12345")).thenReturn(Boolean.FALSE);
    final ProviderBasedPermission test1 = new ProviderBasedPermission(provider, "Data");
    final ProviderBasedPermission test2 = new ProviderBasedPermission(provider, "Data:12345");
    // no exception thrown
    assertFalse(test1.checkImpliesAll(Collections.<Permission> singleton(test2), false));
    // exception thrown
    test1.checkImpliesAll(Collections.<Permission> singleton(test2), true);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final PermissionCheckProvider provider = mock(PermissionCheckProvider.class);
    final ProviderBasedPermission permission = new ProviderBasedPermission(provider, "Data");
    assertEquals(permission, permission);
    assertNotEquals(provider, permission);
    assertEquals(permission, new ProviderBasedPermission(provider, "Data"));
    assertEquals(permission.hashCode(), new ProviderBasedPermission(provider, "Data").hashCode());
    assertNotEquals(permission, new ProviderBasedPermission(provider, "data"));
  }
}
