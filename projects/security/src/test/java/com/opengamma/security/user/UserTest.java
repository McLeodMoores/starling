/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link User}.
 */
@Test(groups = TestGroup.UNIT)
public class UserTest {

  /**
   * Tests permissioning.
   */
  public void testPermissioning() {
    final User user = new User();

    final UserGroup group1 = new UserGroup(0L, "group1");
    group1.getAuthorities().add(new Authority(0L, "/Portfolio/123456/*"));

    final UserGroup group2 = new UserGroup(1L, "group2");
    group2.getAuthorities().add(new Authority(1L, "/Portfolio/7890/Read"));

    user.getUserGroups().add(group1);
    user.getUserGroups().add(group2);

    assertTrue(user.hasPermission("/Portfolio/123456/Read"));
    assertTrue(user.hasPermission("/Portfolio/123456/Write"));
    assertTrue(user.hasPermission("/Portfolio/7890/Read"));
    assertFalse(user.hasPermission("/Portfolio/7890/Write"));
    assertFalse(user.hasPermission("/Portfolio/Foo/Read"));
    assertTrue(user.isAccountNonExpired());
    assertTrue(user.isAccountNonLocked());
    assertTrue(user.isCredentialsNonExpired());
    assertTrue(user.isEnabled());
  }

  /**
   * Tests the password.
   */
  public void testPassword() {
    final String password = "crpty&@\uFFFD9,3 % (4/10)";
    final User user = new User();
    user.setPassword(password);
    try {
      user.getPassword();
      fail();
    } catch (final UnsupportedOperationException e) {
    }
    assertFalse(password.equals(user.getPasswordHash()));
    assertTrue(user.checkPassword(password));
    assertFalse(user.checkPassword("goog"));
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    final Long id = 100L;
    final String username = "user";
    final Set<UserGroup> groups = Collections.singleton(new UserGroup(1000L, "group"));
    final String passwordHash = "eregj2";
    final Date lastLogin = new Date(100000L);
    final User user = new User();
    user.setId(id);
    assertEquals(user.getId(), id);
    user.setUsername(username);
    assertEquals(user.getUsername(), username);
    user.setUserGroups(groups);
    assertEquals(user.getUserGroups(), groups);
    user.setLastLogin(lastLogin);
    assertEquals(user.getLastLogin(), lastLogin);
    user.setPasswordHash(passwordHash);
    assertEquals(user.getPasswordHash(), passwordHash);
  }

  /**
   * Tests that the password can't be retrieved.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetPassword() {
    final User user = new User(100L, "user", "pwd", Collections.singleton(new UserGroup(1000L, "group")), new Date(20000L));
    user.getPassword();
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final User user = new User(100L, "user", "pwd", Collections.singleton(new UserGroup(1000L, "group")), new Date(20000L));
    assertEquals(user, user);
    assertNotEquals(null, user);
    assertNotEquals("user", user);
    assertEquals(user.toString(), "user");
    User other = new User(100L, "user", "pwd", Collections.singleton(new UserGroup(1000L, "group")), new Date(20000L));
    assertEquals(user, other);
    assertEquals(user.hashCode(), other.hashCode());
    other = new User(1000L, "user", "pwd", Collections.singleton(new UserGroup(1000L, "group")), new Date(20000L));
    assertNotEquals(user, other);
    other = new User(100L, "user1", "pwd", Collections.singleton(new UserGroup(1000L, "group")), new Date(20000L));
    assertEquals(user, other);
    other = new User(100L, "user", "pwd1", Collections.singleton(new UserGroup(1000L, "group")), new Date(20000L));
    assertEquals(user, other);
    other = new User(100L, "user", "pwd", Collections.singleton(new UserGroup(10009L, "group")), new Date(20000L));
    assertEquals(user, other);
    other = new User(100L, "user", "pwd", Collections.singleton(new UserGroup(1000L, "group")), new Date(120000L));
    assertEquals(user, other);
  }

  /**
   * Tests getting the authorities.
   */
  @Test
  public void testGetAuthorities() {
    final Authority auth = new Authority(56L, "role");
    final Set<UserGroup> singleton = Collections.singleton(new UserGroup(1000L, "group", Collections.<User>emptySet(),
        Collections.singleton(auth)));
    final User user = new User(100L, "user", "pwd", singleton, new Date(20000L));
    final Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
    assertEquals(authorities.size(), 1);
  }
}
