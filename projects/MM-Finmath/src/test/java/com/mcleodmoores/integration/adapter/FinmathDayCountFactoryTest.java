/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.integration.adapter.FinmathDayCountFactory;

/**
 * Tests {@link FinmathDayCountFactory}.
 */
@Test
public class FinmathDayCountFactoryTest {

  /**
   * Tests the exception thrown when an unhandled day count name is requested from the factory.
   */
  //TODO find out how to check the message
  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testNotImplemented() {
    FinmathDayCountFactory.of("Act/Act ICMA");
  }

  /**
   * Tests that the properties file maps the names to day counts correctly.
   */
  @Test
  public void testFactory() {
    assertEquals("30/360", FinmathDayCountFactory.of("30E/360 ISDA").getName());
    assertEquals("30/360", FinmathDayCountFactory.of("30/360").getName());
    assertEquals("30E/360", FinmathDayCountFactory.of("30E/360").getName());
    assertEquals("30E+/360", FinmathDayCountFactory.of("30E+/360").getName());
    assertEquals("30U/360", FinmathDayCountFactory.of("30U/360").getName());
    assertEquals("Act/360", FinmathDayCountFactory.of("Act/360").getName());
    assertEquals("Act/360", FinmathDayCountFactory.of("Actual/360").getName());
    assertEquals("Act/365", FinmathDayCountFactory.of("Act/365").getName());
    assertEquals("Act/365", FinmathDayCountFactory.of("Actual/365").getName());
    assertEquals("Act/365A", FinmathDayCountFactory.of("Act/365A").getName());
    assertEquals("Act/365A", FinmathDayCountFactory.of("Actual/365A").getName());
    assertEquals("Act/365L", FinmathDayCountFactory.of("Act/365L").getName());
    assertEquals("Act/365L", FinmathDayCountFactory.of("Actual/365L").getName());
    assertEquals("Act/Act AFB", FinmathDayCountFactory.of("Act/Act AFB").getName());
    assertEquals("Act/Act AFB", FinmathDayCountFactory.of("Actual/Actual AFB").getName());
    assertEquals("Act/Act ISDA", FinmathDayCountFactory.of("Act/Act ISDA").getName());
    assertEquals("Act/Act ISDA", FinmathDayCountFactory.of("Actual/Actual ISDA").getName());
    assertEquals("Act/Act YearFrac", FinmathDayCountFactory.of("Act/Act YearFrac").getName());
    assertEquals("Act/Act YearFrac", FinmathDayCountFactory.of("Actual/Actual YearFrac").getName());
    assertEquals("NL/365", FinmathDayCountFactory.of("NL/365").getName());
    assertEquals("None", FinmathDayCountFactory.of("None").getName());
    assertEquals("Unknown", FinmathDayCountFactory.of("Unknown").getName());
  }
}
