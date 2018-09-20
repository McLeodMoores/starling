/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.user.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SimpleUserPrincipals}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleUserPrincipalsTest extends AbstractFudgeBuilderTestCase {
  private static final String USER_NAME = "name";
  private static final ExternalIdBundle ALTERNATE_IDS = ExternalIdBundle.of("A", "B");
  private static final String NETWORK_ADDRESS = "127.0.0.0";
  private static final String EMAIL_ADDRESS = "example@example.com";
  private static final SimpleUserPrincipals USER = new SimpleUserPrincipals();

  static {
    USER.setAlternateIds(ALTERNATE_IDS);
    USER.setEmailAddress(EMAIL_ADDRESS);
    USER.setNetworkAddress(NETWORK_ADDRESS);
    USER.setUserName(USER_NAME);
  }

  /**
   * Tests the factory.
   */
  @Test
  public void testFactory() {
    final SimpleUserPrincipals copy = SimpleUserPrincipals.from(USER);
    assertNotSame(USER, copy);
    assertEquals(USER, copy);
  }

  /**
   * Tests creation from an account.
   */
  @Test
  public void testFromAccount() {
    final SimpleUserPrincipals user = new SimpleUserPrincipals();
    user.setAlternateIds(ALTERNATE_IDS);
    user.setUserName(USER_NAME);
    user.setEmailAddress(EMAIL_ADDRESS);
    final SimpleUserAccount account = new SimpleUserAccount();
    account.setAlternateIds(ALTERNATE_IDS);
    account.setUserName(USER_NAME);
    account.setEmailAddress(EMAIL_ADDRESS);
    final SimpleUserPrincipals other = SimpleUserPrincipals.from(account);
    assertEquals(user, other);
  }

  /**
   * Test the object.
   */
  @Test
  public void testObject() {
    assertEquals(USER, USER);
    assertNotEquals(null, USER);
    assertNotEquals(USER_NAME, USER);
    assertEquals(USER_NAME.toString(), USER_NAME);
    final SimpleUserPrincipals other = new SimpleUserPrincipals();
    other.setAlternateIds(ALTERNATE_IDS);
    other.setEmailAddress(EMAIL_ADDRESS);
    other.setNetworkAddress(NETWORK_ADDRESS);
    other.setUserName(USER_NAME);
    assertEquals(USER, other);
    assertEquals(USER.hashCode(), other.hashCode());
    other.setAlternateIds(ExternalIdBundle.EMPTY);
    assertNotEquals(USER, other);
    other.setAlternateIds(ALTERNATE_IDS);
    other.setEmailAddress("other@example.com");
    assertNotEquals(USER, other);
    other.setEmailAddress(EMAIL_ADDRESS);
    other.setNetworkAddress("0.0.0.0");
    assertNotEquals(USER, other);
    other.setNetworkAddress(NETWORK_ADDRESS);
    other.setUserName("other name");
    assertNotEquals(USER, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertEquals(USER.metaBean().alternateIds().get(USER), ALTERNATE_IDS);
    assertEquals(USER.metaBean().emailAddress().get(USER), EMAIL_ADDRESS);
    assertEquals(USER.metaBean().networkAddress().get(USER), NETWORK_ADDRESS);
    assertEquals(USER.metaBean().userName().get(USER), USER_NAME);

    assertEquals(USER.property("alternateIds").get(), ALTERNATE_IDS);
    assertEquals(USER.property("emailAddress").get(), EMAIL_ADDRESS);
    assertEquals(USER.property("networkAddress").get(), NETWORK_ADDRESS);
    assertEquals(USER.property("userName").get(), USER_NAME);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(SimpleUserPrincipals.class, USER), USER);
  }
}
