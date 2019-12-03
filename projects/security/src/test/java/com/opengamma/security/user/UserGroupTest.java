/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.security.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link UserGroup}.
 */
@Test(groups = TestGroup.UNIT)
public class UserGroupTest {
  private static final Long ID_1 = 100L;
  private static final Long ID_2 = 120L;
  private static final String NAME_1 = "group1";
  private static final String NAME_2 = "group2";
  private static final UserGroup GROUP_1 = new UserGroup(ID_1, NAME_1);
  private static final UserGroup GROUP_2 = new UserGroup(ID_2, NAME_2);
  private static final Set<Authority> AUTHORITIES = new HashSet<>(Arrays.asList(
      new Authority(234L, "*/boo/*"), new Authority(36L, "*")));
  private static final Set<User> USERS = new HashSet<>(Arrays.asList(
      new User(123L, "user1", "owforg-0ivd", Collections.singleton(GROUP_1), new Date(1230943575L)),
      new User(124L, "user2", "jgiga;orij[", new HashSet<>(Arrays.asList(GROUP_1, GROUP_2)), new Date(123094357L)),
      new User(125L, "user3", "wpef09u'", new HashSet<UserGroup>(), new Date(12309435L))));
  static {
    GROUP_2.setAuthorities(AUTHORITIES);
    GROUP_2.setUsers(USERS);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(GROUP_2.getAuthorities(), AUTHORITIES);
    assertEquals(GROUP_2.getId(), ID_2);
    assertEquals(GROUP_2.getName(), NAME_2);
    assertEquals(GROUP_2.getUsers(), USERS);
    assertEquals(GROUP_2, GROUP_2);
    assertNotEquals(null, GROUP_2);
    assertNotEquals(USERS, GROUP_2);
    assertEquals(GROUP_2.toString(), NAME_2);
    final UserGroup other = new UserGroup();
    other.setAuthorities(AUTHORITIES);
    other.setId(ID_2);
    other.setName(NAME_2);
    other.setUsers(USERS);
    assertEquals(GROUP_2, other);
    assertEquals(GROUP_2.hashCode(), other.hashCode());
    other.setAuthorities(Collections.<Authority>emptySet());
    assertEquals(GROUP_2, other);
    other.setAuthorities(AUTHORITIES);
    other.setId(ID_1);
    assertNotEquals(GROUP_2, other);
    other.setAuthorities(AUTHORITIES);
    other.setId(null);
    assertNotEquals(GROUP_2, other);
    other.setId(ID_2);
    other.setName(NAME_1);
    assertEquals(GROUP_2, other);
    other.setName(NAME_2);
    other.setUsers(Collections.<User>emptySet());
    assertEquals(GROUP_2, other);
  }
}
