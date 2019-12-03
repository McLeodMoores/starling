/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.user;

import static org.testng.Assert.assertEquals;

import java.util.Locale;

import org.testng.annotations.Test;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link DateStyle}.
 */
@Test(groups = TestGroup.UNIT)
public class DateStyleTest {

  /**
   * Tests the style.
   */
  public void test() {
    final DateTimeFormatter formatter = DateStyle.ISO.formatter(Locale.CANADA);
    assertEquals(formatter.getLocale(), Locale.CANADA);
  }
}
