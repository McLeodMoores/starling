/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A user of the OpenGamma system.
 */
public class User implements UserDetails {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The database id.
   */
  private Long _id;
  /**
   * The user name.
   */
  private String _username;
  /**
   * The hash of the password.
   */
  private String _passwordHash;
  /**
   * The groups the user belongs to.
   */
  private Set<UserGroup> _userGroups = new HashSet<>();
  /**
   * The instant of last logon.
   */
  private Date _lastLogin;

  /**
   * Creates an instance of the user.
   * @param id  the database id
   * @param username  the user name
   * @param password  the password, hashed internally
   * @param userGroups  the set of groups
   * @param lastLogin  the last logon instant
   */
  public User(final Long id, final String username, final String password, final Set<UserGroup> userGroups, final Date lastLogin) {
    _id = id;
    _username = username;
    setPassword(password);
    _userGroups = userGroups;
    _lastLogin = lastLogin;
  }

  /**
   * Restricted constructor for tools.
   */
  protected User() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the identifier.
   *
   * @return  the identifier
   */
  public Long getId() {
    return _id;
  }

  /**
   * Sets the identifier.
   *
   * @param id  the identifier
   */
  public void setId(final Long id) {
    _id = id;
  }

  @Override
  public String getUsername() {
    return _username;
  }

  /**
   * Sets the username.
   *
   * @param username  the username
   */
  public void setUsername(final String username) {
    this._username = username;
  }

  /**
   * Throws an exception, as the password is not directly stored for security reasons.
   * @return never
   */
  @Override
  public String getPassword() {
    throw new UnsupportedOperationException("For security reasons, the password is not stored directly");
  }

  /**
   * Sets the password, which hashes the password internally.
   * @param password  the password to set
   */
  public void setPassword(final String password) {
    // we don't store the actual password
    _passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
  }

  /**
   * Checks if the password specified matches the stored password.
   * @param password  the password to check
   * @return true if the password is OK, false otherwise
   */
  public boolean checkPassword(final String password) {
    return BCrypt.checkpw(password, _passwordHash);
  }

  /**
   * Gets the password hash.
   *
   * @return  the password hash
   */
  public String getPasswordHash() {
    return _passwordHash;
  }

  /**
   * Sets the password hash.
   *
   * @param passwordHash  the password hash
   */
  public void setPasswordHash(final String passwordHash) {
    this._passwordHash = passwordHash;
  }

  /**
   * Gets the user groups.
   *
   * @return  the user groups
   */
  public Set<UserGroup> getUserGroups() {
    return _userGroups;
  }

  /**
   * Sets the user groups.
   *
   * @param userGroups  the user groups
   */
  public void setUserGroups(final Set<UserGroup> userGroups) {
    this._userGroups = userGroups;
  }

  /**
   * Gets the last login.
   *
   * @return  the last login
   */
  public Date getLastLogin() {
    return _lastLogin;
  }

  /**
   * Sets the last login.
   *
   * @param lastLogin  the last login
   */
  public void setLastLogin(final Date lastLogin) {
    this._lastLogin = lastLogin;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    final Collection<GrantedAuthority> authorities = new ArrayList<>();
    for (final Authority authority : getAuthoritySet()) {
      authorities.add(new SimpleGrantedAuthority(authority.getRegex()));
    }
    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  /**
   * Gets a set of Authorities.
   *
   * @return  the authorities
   */
  public Set<Authority> getAuthoritySet() {
    final Set<Authority> authorities = new HashSet<>();
    for (final UserGroup group : _userGroups) {
      authorities.addAll(group.getAuthorities());
    }
    return authorities;
  }

  /**
   * Returns whether this <code>User</code> has the given permission.
   * This will be the case if and only if the permission matches at least one of this user's <code>Authorities</code>.
   *
   * @param permission Permission to check, for example /MarketData/Bloomberg/AAPL/View
   * @return true if this <code>User</code> has the given permission, false otherwise
   * @see Authority#matches
   */
  public boolean hasPermission(final String permission) {
    for (final Authority authority : getAuthoritySet()) {
      if (authority.matches(permission)) {
        return true;
      }
    }
    return false;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    final User rhs = (User) obj;
    return new EqualsBuilder().append(_id, rhs._id).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(_id).toHashCode();
  }

  @Override
  public String toString() {
    return _username;
  }

}
