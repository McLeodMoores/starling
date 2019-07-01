/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;

/**
 * A manager providing access to the security subsystem.
 */
@Transactional
public class HibernateUserManager implements UserManager, UserDetailsService {

  /**
   * The Hibernate template.
   */
  private final HibernateTemplate _hibernateTemplate;

  /**
   * Creates an instance providing access to the database.
   * @param dbConnector  the database connector, not null
   */
  public HibernateUserManager(final DbConnector dbConnector) {
    _hibernateTemplate = dbConnector.getHibernateTemplate();
  }

  //-------------------------------------------------------------------------
  @Override
  public UserDetails loadUserByUsername(final String username)
      throws UsernameNotFoundException {
    final User user = getUser(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }
    return user;
  }

  @Override
  public User getUser(final String username) {
    ArgumentChecker.notNull(username, "User name");

    return _hibernateTemplate.execute(new HibernateCallback<User>() {
      @Override
      public User doInHibernate(final Session session) throws HibernateException {
        final Query query = session.getNamedQuery("User.one.byUsername");
        query.setString("username", username);
        return (User) query.uniqueResult();
      }
    });
  }

  @Override
  public UserGroup getUserGroup(final String name) {
    ArgumentChecker.notNull(name, "Group name");

    return _hibernateTemplate.execute(new HibernateCallback<UserGroup>() {
      @Override
      public UserGroup doInHibernate(final Session session) throws HibernateException {
        final Query query = session.getNamedQuery("UserGroup.one.byName");
        query.setString("name", name);
        return (UserGroup) query.uniqueResult();
      }
    });
  }

  @Override
  public Authority getAuthority(final String regex) {
    ArgumentChecker.notNull(regex, "Authority");

    return _hibernateTemplate.execute(new HibernateCallback<Authority>() {
      @Override
      public Authority doInHibernate(final Session session) throws HibernateException {
        final Query query = session.getNamedQuery("Authority.one.byAuthorityRegex");
        query.setString("regex", regex);
        return (Authority) query.uniqueResult();
      }
    });
  }

  @Override
  public void addUser(final User user) {
    ArgumentChecker.notNull(user, "User");
    _hibernateTemplate.save(user);
  }

  @Override
  public void deleteUser(final User user) {
    ArgumentChecker.notNull(user, "User");
    _hibernateTemplate.delete(user);
  }

  @Override
  public void updateUser(final User user) {
    ArgumentChecker.notNull(user, "User");
    _hibernateTemplate.update(user);
  }

  @Override
  public void addUserGroup(final UserGroup group) {
    ArgumentChecker.notNull(group, "User group");
    _hibernateTemplate.save(group);
  }

  @Override
  public void deleteUserGroup(final UserGroup group) {
    ArgumentChecker.notNull(group, "User group");
    _hibernateTemplate.delete(group);
  }

  @Override
  public void updateUserGroup(final UserGroup group) {
    ArgumentChecker.notNull(group, "User group");
    _hibernateTemplate.update(group);
  }

  @Override
  public void addAuthority(final Authority authority) {
    ArgumentChecker.notNull(authority, "Authority");
    _hibernateTemplate.save(authority);
  }

  @Override
  public void deleteAuthority(final Authority authority) {
    ArgumentChecker.notNull(authority, "Authority");
    _hibernateTemplate.delete(authority);
  }

  @Override
  public void updateAuthority(final Authority authority) {
    ArgumentChecker.notNull(authority, "Authority");
    _hibernateTemplate.update(authority);
  }

}
