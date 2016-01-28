/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

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
   * Tests that the factory is correctly initialized.
   */
  @Test
  public void testFactory() {
    assertEquals("None", FinmathBusinessDayFactory.of("None").getName());
    assertEquals("TARGET", FinmathBusinessDayFactory.of("TARGET").getName());
    assertEquals("Weekend", FinmathBusinessDayFactory.of("Weekend").getName());
    // tests that the name stored in the factory is case-insensitive
    assertEquals("None", FinmathBusinessDayFactory.of("NONE").getName());
    assertEquals("TARGET", FinmathBusinessDayFactory.of("Target").getName());
    assertEquals("Weekend", FinmathBusinessDayFactory.of("WEEKEND").getName());
    // tests one of the aliases
    assertEquals("Weekend", FinmathBusinessDayFactory.of("Saturday / Sunday").getName());
  }
}
