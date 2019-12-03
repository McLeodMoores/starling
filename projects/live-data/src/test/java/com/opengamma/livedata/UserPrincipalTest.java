/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.net.InetAddress;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link UserPrincipal} and its Fudge builder.
 */
@Test(groups = TestGroup.UNIT)
public class UserPrincipalTest extends AbstractFudgeBuilderTestCase {
  private static final String USER_NAME = "me";
  private static final String IP_ADDRESS = "127.127.127.127";

  /**
   * Tests that the local host is used if no IP address is supplied.
   *
   * @throws Exception
   *           if the local host cannot be found
   */
  public void testGetLocalUserIpAddress() throws Exception {
    final UserPrincipal userPrincipal = UserPrincipal.getLocalUser(USER_NAME);
    assertEquals(userPrincipal, new UserPrincipal(USER_NAME, InetAddress.getLocalHost().toString()));
  }

  /**
   * Tests that a system property is used to get the user name if none is
   * supplied.
   *
   * @throws Exception
   *           if the local host cannot be found
   */
  public void testGetLocalUserName() throws Exception {
    final UserPrincipal userPrincipal = UserPrincipal.getLocalUser();
    assertEquals(userPrincipal, new UserPrincipal(System.getProperty("user.name"), InetAddress.getLocalHost().toString()));
  }

  /**
   * Tests the fields in the test user.
   */
  public void testTestUser() {
    assertEquals(UserPrincipal.getTestUser().getUserName(), "Test user");
    assertEquals(UserPrincipal.getTestUser().getIpAddress(), "127.0.0.1");
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new UserPrincipal(null, IP_ADDRESS);
  }

  /**
   * Tests that the IP address cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIpAddress() {
    new UserPrincipal(USER_NAME, null);
  }

  /**
   * Tests the object.
   */
  public void testObject() {
    final UserPrincipal user = new UserPrincipal(USER_NAME, IP_ADDRESS);
    assertEquals(user.getUserName(), USER_NAME);
    assertEquals(user.getIpAddress(), IP_ADDRESS);
    assertEquals(user.toString(), "UserPrincipal[me, 127.127.127.127]");
    assertEquals(user, user);
    assertNotEquals(null, user);
    assertNotEquals(USER_NAME, user);
    UserPrincipal other = new UserPrincipal(USER_NAME, IP_ADDRESS);
    assertEquals(user, other);
    assertEquals(user.hashCode(), other.hashCode());
    other = new UserPrincipal(IP_ADDRESS, IP_ADDRESS);
    assertNotEquals(user, other);
    other = new UserPrincipal(USER_NAME, USER_NAME);
    assertNotEquals(user, other);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final UserPrincipal user = new UserPrincipal(USER_NAME, IP_ADDRESS);
    assertEncodeDecodeCycle(UserPrincipal.class, user);
  }
}
