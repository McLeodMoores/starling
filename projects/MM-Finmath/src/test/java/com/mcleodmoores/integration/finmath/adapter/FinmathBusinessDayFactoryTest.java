/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * Tests {@link FinmathBusinessDayFactory}.
 */
public class FinmathBusinessDayFactoryTest {

  /**
   * Tests the exception thrown when an unhandled business day calendar is requested from the
   * factory.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNotImplemented() {
    FinmathBusinessDayFactory.of("US");
  }

  /**
   * Tests that the properties file maps the names to business day calendar correctly.
   */
  @Test
  public void testFactory() {
    assertEquals("None", FinmathBusinessDayFactory.of("None").getName());
    assertEquals("TARGET", FinmathBusinessDayFactory.of("TARGET").getName());
    assertEquals("Weekend", FinmathBusinessDayFactory.of("Weekend").getName());
  }
}
