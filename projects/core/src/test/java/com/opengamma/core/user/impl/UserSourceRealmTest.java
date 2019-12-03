/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Locale;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.user.DateStyle;
import com.opengamma.core.user.TimeStyle;
import com.opengamma.core.user.UserSource;
import com.opengamma.core.user.impl.UserSourceRealm.ProxyPrincipals;
import com.opengamma.core.user.impl.UserSourceRealm.ProxyProfile;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.auth.ShiroPermissionResolver;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link UserSourceRealm}.
 */
@Test(groups = TestGroup.UNIT)
public class UserSourceRealmTest {

  private static final PrincipalCollection PRINCIPALS = new SimplePrincipalCollection();
  private static final Permission PERMISSION_OTHER_TYPE = new Permission() {
    @Override
    public boolean implies(final Permission p) {
      return false;
    }
  };
  private static final String USER_NAME = "user name";
  private static final String NETWORK_ADDRESS = "127.0.0.0";
  private static final String EMAIL_ADDRESS = "example@example.com";
  private static final SimpleUserPrincipals USER_PRINCIPALS = new SimpleUserPrincipals();
  private static final SimpleUserAccount USER_ACCOUNT = new SimpleUserAccount();
  private UserSource _userSource;

  /**
   * Sets up a user source with a basic change manager and mocks the methods.
   */
  @BeforeMethod
  public void setUp() {
    USER_PRINCIPALS.setUserName(USER_NAME);
    USER_PRINCIPALS.setEmailAddress(EMAIL_ADDRESS);
    USER_PRINCIPALS.setNetworkAddress(NETWORK_ADDRESS);
    USER_ACCOUNT.setUserName(USER_NAME);
    USER_ACCOUNT.setEmailAddress(EMAIL_ADDRESS);
    _userSource = mock(UserSource.class);
    when(_userSource.changeManager()).thenReturn(new BasicChangeManager());
    when(_userSource.getAccount(USER_NAME)).thenReturn(USER_ACCOUNT);
  }

  /**
   * Tears down the source after each method.
   */
  @AfterMethod
  public void tearDown() {
    _userSource = null;
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a principal with a named permission is permitted.
   */
  @Test
  public void testIsPermittedTrue() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(true, realm.isPermitted(PRINCIPALS, "Master:view"));
  }

  /**
   * Tests that a principal without a named permission is not permitted.
   */
  @Test
  public void testIsPermittedFalse() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(false, realm.isPermitted(PRINCIPALS, "Master:edit"));
  }

  /**
   * Tests that a principal without a permission is not permitted.
   */
  @Test
  public void testIsPermittedOtherType() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(false, realm.isPermitted(PRINCIPALS, PERMISSION_OTHER_TYPE));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that none is allowed if the permissions are null.
   */
  @Test
  public void testIsPermittedAllNull() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        return null;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(false, realm.isPermittedAll(null, "Master:view"));
  }

  /**
   * Tests that all are allowed if there are no required permissions.
   */
  @Test
  public void testIsPermittedAllNone() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(true, realm.isPermittedAll(PRINCIPALS));
  }

  /**
   * Tests that all are permitted if the required permissions match those granted.
   */
  @Test
  public void testIsPermittedAllTrue() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(true, realm.isPermittedAll(PRINCIPALS, "Master:view", "Source:view"));
  }

  /**
   * Tests that not all are permitted if the required permissions do not match those that were granted.
   */
  @Test
  public void testIsPermittedAllFalse() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(false, realm.isPermittedAll(PRINCIPALS, "Master:view", "Source:edit"));
  }

  /**
   * Tests that all are permitted if the required permissions are a subset of those granted.
   */
  @Test
  public void testIsPermittedAllSome() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    assertEquals(true, realm.isPermittedAll(PRINCIPALS, "Master:view"));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that permission is not granted if the authorization info is null.
   */
  @Test(expectedExceptions = UnauthenticatedException.class)
  public void testCheckPermissionNullInfo() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        return null;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermissions(PRINCIPALS, "Master:view");
  }

  /**
   * Tests that permission is granted if the requested permission matches those granted.
   */
  @Test
  public void testCheckPermissionTrue() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermission(PRINCIPALS, "Master:view");
  }

  /**
   * Tests that permission is refused if the required permission does not match those granted.
   */
  @Test(expectedExceptions = UnauthorizedException.class)
  public void testCheckPermissionFalse() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermission(PRINCIPALS, "Master:edit");
  }

  /**
   * Tests that permission is refused if the permission does not match those granted.
   */
  @Test(expectedExceptions = UnauthorizedException.class)
  public void testCheckPermissionOtherType() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermission(PRINCIPALS, PERMISSION_OTHER_TYPE);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that permission is granted if all permissions match those granted.
   */
  @Test
  public void testCheckPermissionsTrue() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermissions(PRINCIPALS, "Master:view", "Source:view");
  }

  /**
   * Tests that permission is refused if they do not match those granted.
   */
  @Test(expectedExceptions = UnauthorizedException.class)
  public void testCheckPermissionsFalse() {
    final ShiroPermissionResolver resolver = new ShiroPermissionResolver();
    final UserSourceRealm realm = new UserSourceRealm(_userSource) {
      @Override
      protected AuthorizationInfo getAuthorizationInfo(final PrincipalCollection principals) {
        final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addObjectPermission(resolver.resolvePermission("Master:view"));
        info.addObjectPermission(resolver.resolvePermission("Source:view"));
        return info;
      }
    };
    realm.setPermissionResolver(resolver);
    realm.checkPermissions(PRINCIPALS, "Master:view", "Source:edit");
  }

  /**
   * Tests the principals proxy.
   */
  @Test
  public void testPrincipalsProxy() {
    final UserSourceRealm userSourceRealm = new UserSourceRealm(_userSource);
    final ProxyPrincipals proxy = userSourceRealm.new ProxyPrincipals(USER_NAME, NETWORK_ADDRESS);
    // user has not been cached, so only this field is available
    try {
      assertEquals(ExternalIdBundle.EMPTY, proxy.getAlternateIds());
      fail();
    } catch (final NullPointerException e) {
      // expected
    }
    // user has been cached so now all fields are available
    assertEquals(NETWORK_ADDRESS, proxy.getNetworkAddress());
    assertEquals(EMAIL_ADDRESS, proxy.getEmailAddress());
    assertEquals(USER_NAME, proxy.getUserName());
    assertEquals(ExternalIdBundle.EMPTY, proxy.getAlternateIds());
    assertEquals(proxy.toString(), "ProxyPrincipals[user name]");
  }

  /**
   * Tests the profile proxy.
   */
  @Test
  public void testProfileProxy() {
    final UserSourceRealm userSourceRealm = new UserSourceRealm(_userSource);
    final ProxyProfile proxy = userSourceRealm.new ProxyProfile(USER_NAME);
    assertEquals(DateStyle.TEXTUAL_MONTH, proxy.getDateStyle());
    assertEquals("", proxy.getDisplayName());
    assertEquals(Collections.emptyMap(), proxy.getExtensions());
    assertEquals(Locale.ENGLISH, proxy.getLocale());
    assertEquals(TimeStyle.ISO, proxy.getTimeStyle());
    assertEquals(OpenGammaClock.getZone(), proxy.getZone());
    assertEquals(proxy.toString(), "ProxyProfile[user name]");
  }

  /**
   * Tests that authentication caching is not allowed.
   */
  @Test
  public void testAuthenitcationCaching() {
    final UserSourceRealm userSourceRealm = new UserSourceRealm(_userSource);
    assertFalse(userSourceRealm.isAuthenticationCachingEnabled());
  }

  /**
   * Tests the supported authentication token type.
   */
  @Test
  public void testAuthenticationTokenType() {
    final UserSourceRealm userSourceRealm = new UserSourceRealm(_userSource);
    @SuppressWarnings("serial")
    final AuthenticationToken token = new AuthenticationToken() {

      @Override
      public Object getPrincipal() {
        return null;
      }

      @Override
      public Object getCredentials() {
        return null;
      }
    };
    assertFalse(userSourceRealm.supports(token));
  }

  /**
   * Tests the failure to get the authentication info.
   */
  @Test(expectedExceptions = AuthenticationException.class)
  public void testAuthenticationInfoFailure() {
    final UserSourceRealm userSourceRealm = new UserSourceRealm(_userSource);
    userSourceRealm.doGetAuthenticationInfo(null);
  }

}
