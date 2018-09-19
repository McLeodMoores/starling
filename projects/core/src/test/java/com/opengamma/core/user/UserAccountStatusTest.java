/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.user;

import org.apache.shiro.authc.DisabledAccountException;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link UserAccountStatus}.
 */
@Test(groups = TestGroup.UNIT)
public class UserAccountStatusTest {

  /**
   * Tests whether an account is disabled.
   */
  @Test(expectedExceptions = DisabledAccountException.class)
  public void testDisabled() {
    UserAccountStatus.DISABLED.check();
  }

  /**
   * Tests whether an account is locked.
   */
  @Test(expectedExceptions = DisabledAccountException.class)
  public void testLocked() {
    UserAccountStatus.LOCKED.check();
  }

  /**
   * Tests that no errors are thrown.
   */
  @Test
  public void testEnabled() {
    UserAccountStatus.ENABLED.check();
  }
}
