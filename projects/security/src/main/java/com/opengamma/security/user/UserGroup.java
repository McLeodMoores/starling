/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import java.util.HashSet;
import java.util.Set;

/**
 * A user group within OpenGamma.
 */
public class UserGroup {

  /**
   * The database id.
   */
  private Long _id;
  /**
   * The group name.
   */
  private String _name;
  /**
   * The users within the group.
   */
  private Set<User> _users = new HashSet<>();
  /**
   * The authorities controlled by the group.
   */
  private Set<Authority> _authorities = new HashSet<>();

  /**
   * Creates an instance of a user group.
   * @param id  the database id
   * @param name  the group name
   */
  public UserGroup(final Long id, final String name) {
    this(id, name, new HashSet<User>(), new HashSet<Authority>());
  }

  /**
   * Creates an instance of a user group.
   * @param id  the database id
   * @param name  the group name
   * @param users  the users
   * @param authorities  the authorities
   */
  public UserGroup(final Long id, final String name, final Set<User> users, final Set<Authority> authorities) {
    _id = id;
    _name = name;
    _users = users;
    _authorities = authorities;
  }

  /**
   * Restricted constructor for tools.
   */
  protected UserGroup() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user group id.
   *
   * @return  the id
   */
  public Long getId() {
    return _id;
  }

  /**
   * Sets the user group id.
   *
   * @param id  the id
   */
  public void setId(final Long id) {
    _id = id;
  }

  /**
   * Gets the name.
   *
   * @return  the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
   *
   * @param name  the name
   */
  public void setName(final String name) {
    this._name = name;
  }

  /**
   * Gets the users.
   *
   * @return  the users
   */
  public Set<User> getUsers() {
    return _users;
  }

  /**
   * Sets the users.
   *
   * @param users  the users
   */
  public void setUsers(final Set<User> users) {
    this._users = users;
  }

  /**
   * Gets the authorities.
   *
   * @return  the authorities
   */
  public Set<Authority> getAuthorities() {
    return _authorities;
  }

  /**
   * Sets the authorities.
   *
   * @param authorities  the authorities
   */
  public void setAuthorities(final Set<Authority> authorities) {
    this._authorities = authorities;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UserGroup other = (UserGroup) obj;
    if (_id == null) {
      if (other._id != null) {
        return false;
      }
    } else if (!_id.equals(other._id)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_id == null ? 0 : _id.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return _name;
  }

}
