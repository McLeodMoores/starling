/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.MapCache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.SoftHashMap;
import org.threeten.bp.ZoneId;

import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.user.DateStyle;
import com.opengamma.core.user.TimeStyle;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.UserPrincipals;
import com.opengamma.core.user.UserProfile;
import com.opengamma.core.user.UserSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.auth.ShiroPermissionResolver;

/**
 * A security {@code Realm} that accesses the user source.
 * <p>
 * The {@code UserSource} insulates the main application from Apache Shiro.
 */
public class UserSourceRealm extends AuthorizingRealm {

  /**
   * The user profiles.
   * This cache operates with {@code ProxyProfile} to ensure that changes to a user are correctly propagated.
   */
  private final MapCache<String, UserProfile> _profiles = new MapCache<>("profiles", new SoftHashMap<String, UserProfile>());
  /**
   * The user principals.
   * This cache operates with {@code ProxyPrincipals} to ensure that changes to a user are correctly propagated.
   */
  private final MapCache<String, UserPrincipals> _principals = new MapCache<>("principals", new SoftHashMap<String, UserPrincipals>());
  /**
   * The user master.
   */
  private final UserSource _userSource;

  /**
   * Creates an instance.
   *
   * @param userSource  the user source, not null
   */
  public UserSourceRealm(final UserSource userSource) {
    setName("UserSourceRealm");
    _userSource = ArgumentChecker.notNull(userSource, "userSource");
    // clear everything if any user changed
    _userSource.changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(final ChangeEvent event) {
        if (event.getType() == ChangeType.CHANGED || event.getType() == ChangeType.REMOVED) {
          final Cache<Object, AuthenticationInfo> authnCache = getAuthenticationCache();
          if (authnCache != null) {
            authnCache.clear();
          }
          final Cache<Object, AuthorizationInfo> authzCache = getAuthorizationCache();
          if (authzCache != null) {
            authzCache.clear();
          }
          _profiles.clear();
          _principals.clear();
        }
      }
    });
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user profile.
   * <p>
   * This method binds the proxy profile that is stored in the user session
   * to the real profile stored in the cache.
   *
   * @param userName  the user name, not null
   * @return the user profile, null if not found
   */
  UserProfile getUserProfile(final String userName) {
    UserProfile profile = _profiles.get(userName);
    if (profile == null) {
      try {
        profile = _userSource.getAccount(userName).getProfile();
        _profiles.put(userName, profile);
      } catch (final DataNotFoundException ex) {
        // ignored
      }
    }
    return profile;
  }

  /**
   * Gets the user principals.
   * <p>
   * This method binds the proxy profile that is stored in the user session
   * to the real profile stored in the cache.
   *
   * @param userName  the user name, not null
   * @return the user principals, null if not found
   */
  UserPrincipals getUserPrincipals(final String userName) {
    final UserPrincipals principals = _principals.get(userName);
    if (principals == null) {
      try {
        final UserAccount account = _userSource.getAccount(userName);
        _principals.put(userName, SimpleUserPrincipals.from(account));
      } catch (final DataNotFoundException ex) {
        // ignored
      }
    }
    return principals;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isAuthenticationCachingEnabled() {
    // additional work performed in doGetAuthenticationInfo() which must not be skipped by caching
    return false;
  }

  @Override
  public boolean supports(final AuthenticationToken token) {
    return token instanceof UsernamePasswordToken;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
    try {
      // load and validate
      final UsernamePasswordToken upToken = (UsernamePasswordToken) token;
      final String enteredUserName = upToken.getUsername();
      final UserAccount account = loadUserByName(enteredUserName);
      account.getStatus().check();
      // make data available in the session
      final String userName = account.getUserName();
      _profiles.put(userName, account.getProfile());
      _principals.put(userName, SimpleUserPrincipals.from(account));
      AuthUtils.getSubject().getSession().setAttribute(UserProfile.ATTRIBUTE_KEY, new ProxyProfile(userName));
      AuthUtils.getSubject().getSession().setAttribute(UserPrincipals.ATTRIBUTE_KEY, new ProxyPrincipals(userName, upToken.getHost()));
      // return Shiro data
      final SimplePrincipalCollection principals = new SimplePrincipalCollection();
      principals.add(userName, getName());
      return new SimpleAuthenticationInfo(principals, account.getPasswordHash());
    } catch (final RuntimeException ex) {
      throw new AuthenticationException("Unable to load authentication data: " + token, ex);
    }
  }

  @Override
  protected void assertCredentialsMatch(final AuthenticationToken token, final AuthenticationInfo info) throws AuthenticationException {
    // cleanup after login failure
    // Apache Shiro should provide a protected method to handle this better
    try {
      super.assertCredentialsMatch(token, info);
    } catch (final AuthenticationException ex) {
      final String userName = info.getPrincipals().getPrimaryPrincipal().toString();
      _profiles.remove(userName);
      _principals.remove(userName);
      AuthUtils.getSubject().getSession().removeAttribute(UserProfile.ATTRIBUTE_KEY);
      AuthUtils.getSubject().getSession().removeAttribute(UserPrincipals.ATTRIBUTE_KEY);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
    if (principals == null) {
      throw new AuthorizationException("PrincipalCollection must not be null");
    }
    try {
      // try UniqueId
      final Collection<String> userNames = principals.byType(String.class);
      if (userNames.size() == 0) {
        return null;
      }
      if (userNames.size() > 1) {
        throw new AuthorizationException("PrincipalCollection must not contain two UserAccount instances");
      }
      final String userName = userNames.iterator().next();
      final UserAccount account = loadUserByName(userName);
      final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
      info.addRoles(account.getRoles());
      for (final String permStr : account.getPermissions()) {
        info.addObjectPermission(getPermissionResolver().resolvePermission(permStr));
      }
      return info;

    } catch (final AuthorizationException ex) {
      throw ex;
    } catch (final RuntimeException ex) {
      throw new AuthorizationException("Unable to load authorization data: " + principals, ex);
    }
  }

  private UserAccount loadUserByName(final String userName) {
    try {
      return _userSource.getAccount(userName);
    } catch (final DataNotFoundException ex) {
      throw new UnknownAccountException("User not found: " + userName, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * The proxy profile.
   */
  class ProxyProfile implements UserProfile {
    private final String _userName;

    /**
     * @param userName  the user name
     */
    ProxyProfile(final String userName) {
      _userName = userName;
    }

    @Override
    public String getDisplayName() {
      return getUserProfile(_userName).getDisplayName();
    }

    @Override
    public Locale getLocale() {
      return getUserProfile(_userName).getLocale();
    }

    @Override
    public ZoneId getZone() {
      return getUserProfile(_userName).getZone();
    }

    @Override
    public DateStyle getDateStyle() {
      return getUserProfile(_userName).getDateStyle();
    }

    @Override
    public TimeStyle getTimeStyle() {
      return getUserProfile(_userName).getTimeStyle();
    }

    @Override
    public Map<String, String> getExtensions() {
      return getUserProfile(_userName).getExtensions();
    }

    @Override
    public String toString() {
      return String.format("ProxyProfile[%s]", _userName);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * The proxy principals.
   */
  class ProxyPrincipals implements UserPrincipals {
    private final String _userName;
    private final String _networkAddress;

    /**
     * @param userName  the user name
     * @param networkAddress  the network address
     */
    ProxyPrincipals(final String userName, final String networkAddress) {
      _userName = userName;
      _networkAddress = networkAddress;
    }

    @Override
    public String getUserName() {
      return getUserPrincipals(_userName).getUserName();
    }

    @Override
    public ExternalIdBundle getAlternateIds() {
      return getUserPrincipals(_userName).getAlternateIds();
    }

    @Override
    public String getNetworkAddress() {
      return _networkAddress;
    }

    @Override
    public String getEmailAddress() {
      return getUserPrincipals(_userName).getEmailAddress();
    }

    @Override
    public String toString() {
      return String.format("ProxyPrincipals[%s]", _userName);
    }
  }

  //-------------------------------------------------------------------------
  // override Authorizer permission methods
  // all interesting methods are overridden to insulate against changes in superclass
  // the array versions of the methods are not overridden as they are not used
  @Override
  public ShiroPermissionResolver getPermissionResolver() {
    return (ShiroPermissionResolver) super.getPermissionResolver();
  }

  @Override
  public boolean isPermitted(final PrincipalCollection subjectPrincipal, final String requiredPermission) {
    final Permission required = getPermissionResolver().resolvePermission(requiredPermission);
    return isPermitted(subjectPrincipal, required);
  }

  @Override
  public boolean isPermitted(final PrincipalCollection subjectPrincipal, final Permission requiredPermission) {
    return isPermittedAll(subjectPrincipal, ImmutableList.of(requiredPermission));
  }

  @Override
  public boolean isPermittedAll(final PrincipalCollection subjectPrincipal, final String... requiredPermissions) {
    if (requiredPermissions.length == 0) {
      return true;
    }
    final List<Permission> required = getPermissionResolver().resolvePermissions(requiredPermissions);
    return isPermittedAll(subjectPrincipal, required);
  }

  @Override
  public boolean isPermittedAll(final PrincipalCollection subjectPrincipal, final Collection<Permission> requiredPermissions) {
    final AuthorizationInfo info = getAuthorizationInfo(subjectPrincipal);
    if (info == null) {
      return false;
    }
    return getPermissionResolver().isPermittedAll(info.getObjectPermissions(), requiredPermissions);
  }

  //-------------------------------------------------------------------------
  @Override
  public void checkPermission(final PrincipalCollection subjectPrincipal, final String requiredPermission) throws AuthorizationException {
    final Permission required = getPermissionResolver().resolvePermission(requiredPermission);
    checkPermission(subjectPrincipal, required);
  }

  @Override
  public void checkPermission(final PrincipalCollection subjectPrincipal, final Permission requiredPermission) throws AuthorizationException {
    checkPermissions(subjectPrincipal, ImmutableList.of(requiredPermission));
  }

  @Override
  public void checkPermissions(final PrincipalCollection subjectPrincipal, final String... requiredPermissions) throws AuthorizationException {
    if (requiredPermissions.length > 0) {
      final List<Permission> required = getPermissionResolver().resolvePermissions(requiredPermissions);
      checkPermissions(subjectPrincipal, required);
    }
  }

  @Override
  public void checkPermissions(final PrincipalCollection subjectPrincipal, final Collection<Permission> requiredPermissions) throws AuthorizationException {
    final AuthorizationInfo info = getAuthorizationInfo(subjectPrincipal);
    if (info == null) {
      throw new UnauthenticatedException("Permission denied, user not authenticated");
    }
    getPermissionResolver().checkPermissions(info.getObjectPermissions(), requiredPermissions);
  }

}
