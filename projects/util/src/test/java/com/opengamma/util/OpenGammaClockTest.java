/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneOffset;

/**
 * Tests for {@link OpenGammaClock}.
 */
public class OpenGammaClockTest {

  /**
   * Tests the default instance.
   */
  @Test
  public void testGetDefault() {
    assertEquals(OpenGammaClock.getInstance().getClass().getSimpleName(), "SystemClock");
    assertEquals(OpenGammaClock.getInstance().getZone(), ZoneOffset.UTC);
  }

  // shouldn't test setters as has size effects
}
