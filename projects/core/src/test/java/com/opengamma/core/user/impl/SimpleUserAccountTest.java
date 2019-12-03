/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.user.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.core.user.UserAccountStatus;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SimpleUserAccount}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleUserAccountTest extends AbstractFudgeBuilderTestCase {
  private static final String USER_NAME = "Bort";
  private static final String PASSWORD_HASH = "jgoirejg98";
  private static final UserAccountStatus STATUS = UserAccountStatus.ENABLED;
  private static final ExternalIdBundle ALTERNATE_IDS = ExternalIdBundle.of("A", "B");
  private static final Set<String> ROLES = Collections.singleton("role");
  private static final Set<String> PERMISSIONS = Collections.singleton("write");
  private static final String EMAIL_ADDRESS = "example@example.com";
  private static final SimpleUserProfile PROFILE = new SimpleUserProfile();
  private static final SimpleUserAccount ACCOUNT = new SimpleUserAccount();

  static {
    ACCOUNT.setAlternateIds(ALTERNATE_IDS);
    ACCOUNT.setEmailAddress(EMAIL_ADDRESS);
    ACCOUNT.setPasswordHash(PASSWORD_HASH);
    ACCOUNT.setPermissions(PERMISSIONS);
    ACCOUNT.setProfile(PROFILE);
    ACCOUNT.setRoles(ROLES);
    ACCOUNT.setStatus(STATUS);
    ACCOUNT.setUserName(USER_NAME);
  }

  /**
   * Tests the copy factory.
   */
  @Test
  public void testCopy() {
    final SimpleUserAccount copy = SimpleUserAccount.from(ACCOUNT);
    assertNotSame(ACCOUNT, copy);
    assertEquals(ACCOUNT, copy);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(ACCOUNT, ACCOUNT);
    assertNotEquals(null, ACCOUNT);
    assertNotEquals(PROFILE, ACCOUNT);
    assertEquals(ACCOUNT.toString(), "SimpleUserAccount{userName=Bort, passwordHash=jgoirejg98, status=ENABLED, alternateIds=Bundle[A~B], "
        + "roles=[role], permissions=[write], emailAddress=example@example.com, profile=SimpleUserProfile{displayName=, locale=en, "
        + "zone=Z, dateStyle=TEXTUAL_MONTH, timeStyle=ISO, extensions={}}}");
    final SimpleUserAccount other = SimpleUserAccount.from(ACCOUNT);
    assertEquals(ACCOUNT, other);
    assertEquals(ACCOUNT.hashCode(), other.hashCode());
    other.setAlternateIds(ExternalIdBundle.EMPTY);
    assertNotEquals(ACCOUNT, other);
    other.setAlternateIds(ALTERNATE_IDS);
    other.setEmailAddress("other@example.com");
    assertNotEquals(ACCOUNT, other);
    other.setEmailAddress(EMAIL_ADDRESS);
    other.setPasswordHash("ojegure");
    assertNotEquals(ACCOUNT, other);
    other.setPasswordHash(PASSWORD_HASH);
    other.setPermissions(Collections.<String>emptySet());
    assertNotEquals(ACCOUNT, other);
    other.setPermissions(PERMISSIONS);
    final SimpleUserProfile profile = new SimpleUserProfile();
    profile.setDisplayName(USER_NAME);
    other.setProfile(profile);
    assertNotEquals(ACCOUNT, other);
    other.setProfile(PROFILE);
    other.setRoles(Collections.<String>emptySet());
    assertNotEquals(ACCOUNT, other);
    other.setRoles(ROLES);
    other.setStatus(UserAccountStatus.DISABLED);
    assertNotEquals(ACCOUNT, other);
    other.setStatus(STATUS);
    other.setUserName("Bart");
    assertNotEquals(ACCOUNT, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertEquals(ACCOUNT.metaBean().alternateIds().get(ACCOUNT), ALTERNATE_IDS);
    assertEquals(ACCOUNT.metaBean().emailAddress().get(ACCOUNT), EMAIL_ADDRESS);
    assertEquals(ACCOUNT.metaBean().passwordHash().get(ACCOUNT), PASSWORD_HASH);
    assertEquals(ACCOUNT.metaBean().permissions().get(ACCOUNT), PERMISSIONS);
    assertEquals(ACCOUNT.metaBean().profile().get(ACCOUNT), PROFILE);
    assertEquals(ACCOUNT.metaBean().roles().get(ACCOUNT), ROLES);
    assertEquals(ACCOUNT.metaBean().status().get(ACCOUNT), STATUS);
    assertEquals(ACCOUNT.metaBean().userName().get(ACCOUNT), USER_NAME);

    assertEquals(ACCOUNT.property("alternateIds").get(), ALTERNATE_IDS);
    assertEquals(ACCOUNT.property("emailAddress").get(), EMAIL_ADDRESS);
    assertEquals(ACCOUNT.property("passwordHash").get(), PASSWORD_HASH);
    assertEquals(ACCOUNT.property("permissions").get(), PERMISSIONS);
    assertEquals(ACCOUNT.property("profile").get(), PROFILE);
    assertEquals(ACCOUNT.property("roles").get(), ROLES);
    assertEquals(ACCOUNT.property("status").get(), STATUS);
    assertEquals(ACCOUNT.property("userName").get(), USER_NAME);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(SimpleUserAccount.class, ACCOUNT), ACCOUNT);
  }
}
