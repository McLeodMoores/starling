/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.user;

import static org.testng.Assert.assertEquals;

import java.util.Locale;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.core.user.DateStyle;
import com.opengamma.core.user.TimeStyle;
import com.opengamma.master.user.impl.InMemoryUserMaster;

/**
 * Tests for {@link UserForm}.
 */
public class UserFormTest {
  private static final String USER_NAME = "username";
  private static final String PLAINTEXT_PASSWORD = "afiu87s'3598d";
  private static final String EMAIL = "me@me.com";
  private static final String DISPLAY_NAME = "you";
  private static final Locale LOCALE = Locale.ENGLISH;
  private static final String ZONE = "Europe/London";
  private static final String DATE_STYLE = DateStyle.ISO.name();
  private static final String TIME_STYLE = TimeStyle.ISO.name();
  private static final ManageableUser BASE_USER = new ManageableUser(USER_NAME);
  static {
    BASE_USER.setUserName(USER_NAME);
    BASE_USER.setEmailAddress(EMAIL);
    BASE_USER.getProfile().setDisplayName(DISPLAY_NAME);
    BASE_USER.getProfile().setLocale(LOCALE);
    BASE_USER.getProfile().setZone(ZoneId.of(ZONE));
    BASE_USER.getProfile().setDateStyle(DateStyle.valueOf(DATE_STYLE));
    BASE_USER.getProfile().setTimeStyle(TimeStyle.valueOf(TIME_STYLE));
  }
  private static final UserForm FORM = new UserForm(USER_NAME, PLAINTEXT_PASSWORD, EMAIL, DISPLAY_NAME, LOCALE.toString(), ZONE, DATE_STYLE, TIME_STYLE);
  private static final UserMaster MASTER = new InMemoryUserMaster();
  private static final PasswordService PWD_SERVICE = new DefaultPasswordService();

  /**
   * Tests that the master cannot be null.
   */
  @Test(expectedExceptions = UserFormException.class)
  public void testNullMasterAdd() {
    FORM.add(null, PWD_SERVICE);
  }

  /**
   * Tests that the master cannot be null.
   */
  @Test(expectedExceptions = UserFormException.class)
  public void testNullMasterUpdate() {
    FORM.update(null, PWD_SERVICE);
  }

  /**
   * Tests that the password service cannot be null.
   */
  @Test(expectedExceptions = UserFormException.class)
  public void testNullPasswordServiceAdd() {
    FORM.add(MASTER, null);
  }

  /**
   * Tests that the password service cannot be null.
   */
  @Test(expectedExceptions = UserFormException.class)
  public void testNullPasswordServiceUpdate() {
    FORM.update(MASTER, null);
  }

  /**
   * Tests that the user name cannot be null or empty.
   */
  @Test
  public void testNullOrEmptyName() {
    final UserForm form = new UserForm(USER_NAME, PLAINTEXT_PASSWORD, EMAIL, DISPLAY_NAME, LOCALE.toString(), ZONE, DATE_STYLE, TIME_STYLE);
    form.setUserName(null);
    try {
      form.validate(MASTER, PWD_SERVICE, true);
    } catch (final UserFormException e) {
      assertEquals(e.getErrors().size(), 1);
      assertEquals(e.getErrors().get(0), UserFormError.USERNAME_MISSING);
    }
    form.setUserName("");
    try {
      form.validate(MASTER, PWD_SERVICE, true);
    } catch (final UserFormException e) {
      assertEquals(e.getErrors().size(), 1);
      assertEquals(e.getErrors().get(0), UserFormError.USERNAME_MISSING);
    }
  }

  /**
   * Tests that the user name cannot be shorter than 5 characters.
   */
  @Test
  public void testShortUserName() {
    final UserForm form = new UserForm(USER_NAME, PLAINTEXT_PASSWORD, EMAIL, DISPLAY_NAME, LOCALE.toString(), ZONE, DATE_STYLE, TIME_STYLE);
    form.setUserName("user");
    try {
      form.validate(MASTER, PWD_SERVICE, true);
    } catch (final UserFormException e) {
      assertEquals(e.getErrors().size(), 1);
      assertEquals(e.getErrors().get(0), UserFormError.USERNAME_TOO_SHORT);
    }
  }

  /**
   * Tests that the user name cannot be longer than 20 characters.
   */
  @Test
  public void testLongUserName() {
    final UserForm form = new UserForm(USER_NAME, PLAINTEXT_PASSWORD, EMAIL, DISPLAY_NAME, LOCALE.toString(), ZONE, DATE_STYLE, TIME_STYLE);
    form.setUserName("useruseruseruseruseruseruser");
    try {
      form.validate(MASTER, PWD_SERVICE, true);
    } catch (final UserFormException e) {
      assertEquals(e.getErrors().size(), 1);
      assertEquals(e.getErrors().get(0), UserFormError.USERNAME_TOO_LONG);
    }
  }

  /**
   * Tests that the user name only contain letters, numbers and hyphens.
   */
  @Test
  public void testInvalidUserName() {
    final UserForm form = new UserForm(USER_NAME, PLAINTEXT_PASSWORD, EMAIL, DISPLAY_NAME, LOCALE.toString(), ZONE, DATE_STYLE, TIME_STYLE);
    //form.setUserName("John Smith-Jones");
    form.validate(MASTER, PWD_SERVICE, true);
    //form.setUserName("John O'Smith");
    form.validate(MASTER, PWD_SERVICE, true);
    form.setUserName("John #Smith");
    try {
      form.validate(MASTER, PWD_SERVICE, true);
    } catch (final UserFormException e) {
      assertEquals(e.getErrors().size(), 1);
      assertEquals(e.getErrors().get(0), UserFormError.USERNAME_INVALID);
    }
  }
}
