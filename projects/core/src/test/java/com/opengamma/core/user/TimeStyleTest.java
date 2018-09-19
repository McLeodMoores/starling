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
 * Tests for {@link TimeStyle}.
 */
@Test(groups = TestGroup.UNIT)
public class TimeStyleTest {

  /**
   * Tests the style.
   */
  public void test() {
    final DateTimeFormatter formatter = TimeStyle.ISO.formatter(Locale.CANADA);
    assertEquals(formatter.getLocale(), Locale.CANADA);
  }
}
