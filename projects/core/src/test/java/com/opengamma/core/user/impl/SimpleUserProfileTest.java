/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.user.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.user.DateStyle;
import com.opengamma.core.user.TimeStyle;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SimpleUserProfile}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleUserProfileTest extends AbstractFudgeBuilderTestCase {
  private static final String DISPLAY_NAME = "J Smith";
  private static final Locale LOCALE = Locale.FRANCE;
  private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
  private static final DateStyle DATE_STYLE = DateStyle.STANDARD_EU;
  private static final TimeStyle TIME_STYLE = TimeStyle.LOCALIZED_FULL;
  private static final Map<String, String> EXTENSIONS = new HashMap<>();;
  private static final SimpleUserProfile PROFILE = new SimpleUserProfile();

  static {
    EXTENSIONS.put("office", "a");
    PROFILE.setDateStyle(DATE_STYLE);
    PROFILE.setDisplayName(DISPLAY_NAME);
    PROFILE.setExtensions(EXTENSIONS);
    PROFILE.setLocale(LOCALE);
    PROFILE.setTimeStyle(TIME_STYLE);
    PROFILE.setZone(ZONE);
  }

  /**
   * Test the copy factory.
   */
  @Test
  public void testCopy() {
    final SimpleUserProfile copy = SimpleUserProfile.from(PROFILE);
    assertNotSame(PROFILE, copy);
    assertEquals(PROFILE, copy);
  }

  /**
   * Tests the object methods.
   */
  @Test
  public void testObject() {
    assertEquals(PROFILE, PROFILE);
    assertNotEquals(null, PROFILE);
    assertNotEquals(DISPLAY_NAME, PROFILE);
    assertEquals(PROFILE.toString(), "SimpleUserProfile{displayName=J Smith, locale=fr_FR, zone=Europe/Paris, "
        + "dateStyle=STANDARD_EU, timeStyle=LOCALIZED_FULL, extensions={office=a}}");
    final SimpleUserProfile other = SimpleUserProfile.from(PROFILE);
    assertEquals(PROFILE, other);
    assertEquals(PROFILE.hashCode(), other.hashCode());
    other.setDateStyle(DateStyle.ISO);
    assertNotEquals(PROFILE, other);
    other.setDateStyle(DATE_STYLE);
    other.setDisplayName("NAME");
    assertNotEquals(PROFILE, other);
    other.setDisplayName(DISPLAY_NAME);
    other.setExtensions(Collections.<String, String>emptyMap());
    assertNotEquals(PROFILE, other);
    other.setExtensions(EXTENSIONS);
    other.setLocale(Locale.CANADA);
    assertNotEquals(PROFILE, other);
    other.setLocale(LOCALE);
    other.setTimeStyle(TimeStyle.LOCALIZED_SHORT);
    assertNotEquals(PROFILE, other);
    other.setTimeStyle(TIME_STYLE);
    other.setZone(ZoneOffset.UTC);
    assertNotEquals(PROFILE, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertEquals(PROFILE.metaBean().dateStyle().get(PROFILE), DATE_STYLE);
    assertEquals(PROFILE.metaBean().displayName().get(PROFILE), DISPLAY_NAME);
    assertEquals(PROFILE.metaBean().extensions().get(PROFILE), EXTENSIONS);
    assertEquals(PROFILE.metaBean().locale().get(PROFILE), LOCALE);
    assertEquals(PROFILE.metaBean().timeStyle().get(PROFILE), TIME_STYLE);
    assertEquals(PROFILE.metaBean().zone().get(PROFILE), ZONE);

    assertEquals(PROFILE.property("dateStyle").get(), DATE_STYLE);
    assertEquals(PROFILE.property("displayName").get(), DISPLAY_NAME);
    assertEquals(PROFILE.property("extensions").get(), EXTENSIONS);
    assertEquals(PROFILE.property("locale").get(), LOCALE);
    assertEquals(PROFILE.property("timeStyle").get(), TIME_STYLE);
    assertEquals(PROFILE.property("zone").get(), ZONE);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(SimpleUserProfile.class, PROFILE), PROFILE);
  }
}
