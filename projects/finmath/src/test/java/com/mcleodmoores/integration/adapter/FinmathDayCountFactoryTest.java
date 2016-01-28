/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link FinmathDayCountFactory}.
 */
@Test(groups = TestGroup.UNIT)
public class FinmathDayCountFactoryTest {

  /**
   * Tests the exception thrown when an unhandled day count name is requested from the factory.
   */
  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testNotImplemented() {
    FinmathDayCountFactory.of("Act/Act ICMA");
  }

  /**
   * Tests that factory is correctly populated and that aliases work.
   */
  @Test
  public void testFactory() {
    assertEquals(FinmathDayCountFactory.of("Act/Act AFB").getName(), "Act/Act AFB");
    assertEquals(FinmathDayCountFactory.of("Actual/Actual AFB").getName(), "Act/Act AFB");
    assertEquals(FinmathDayCountFactory.of("Act/Act ISDA").getName(), "Act/Act ISDA");
    assertEquals(FinmathDayCountFactory.of("Actual/Actual ISDA").getName(), "Act/Act ISDA");
    assertEquals(FinmathDayCountFactory.of("Act/Act YearFrac").getName(), "Act/Act YearFrac");
    assertEquals(FinmathDayCountFactory.of("Actual/Actual YearFrac").getName(), "Act/Act YearFrac");
    assertEquals(FinmathDayCountFactory.of("Actual/Actual Year Fraction").getName(), "Act/Act YearFrac");
    assertEquals(FinmathDayCountFactory.of("Act/360").getName(), "Act/360");
    assertEquals(FinmathDayCountFactory.of("Actual/360").getName(), "Act/360");
    assertEquals(FinmathDayCountFactory.of("Act/365A").getName(), "Act/365A");
    assertEquals(FinmathDayCountFactory.of("Actual/365A").getName(), "Act/365A");
    assertEquals(FinmathDayCountFactory.of("Act/365").getName(), "Act/365");
    assertEquals(FinmathDayCountFactory.of("Actual/365").getName(), "Act/365");
    assertEquals(FinmathDayCountFactory.of("Act/365L").getName(), "Act/365L");
    assertEquals(FinmathDayCountFactory.of("Actual/365L").getName(), "Act/365L");
    assertEquals(FinmathDayCountFactory.of("NL/365").getName(), "NL/365");
    assertEquals(FinmathDayCountFactory.of("None").getName(), "None");
    assertEquals(FinmathDayCountFactory.of("30E+/360").getName(), "30E+/360");
    assertEquals(FinmathDayCountFactory.of("Eurobond basis").getName(), "30E+/360");
    assertEquals(FinmathDayCountFactory.of("30/360 ISMA").getName(), "30E+/360");
    assertEquals(FinmathDayCountFactory.of("30E/360").getName(), "30E/360");
    assertEquals(FinmathDayCountFactory.of("30E/360 ISDA").getName(), "30E/360 ISDA");
    assertEquals(FinmathDayCountFactory.of("30/360").getName(), "30E/360 ISDA");
    assertEquals(FinmathDayCountFactory.of("30E/360 ISDA Termination").getName(), "30E/360 ISDA Termination");
    assertEquals(FinmathDayCountFactory.of("30/360 Termination").getName(), "30E/360 ISDA Termination");
    assertEquals(FinmathDayCountFactory.of("30U/360 not EOM").getName(), "30U/360 not EOM");
    assertEquals(FinmathDayCountFactory.of("Bond Basis not EOM").getName(), "30U/360 not EOM");
    assertEquals(FinmathDayCountFactory.of("30U/360").getName(), "30U/360");
    assertEquals(FinmathDayCountFactory.of("Bond Basis").getName(), "30U/360");
    assertEquals(FinmathDayCountFactory.of("30U/360 EOM").getName(), "30U/360");
    assertEquals(FinmathDayCountFactory.of("Bond Basis EOM").getName(), "30U/360");
    assertEquals(FinmathDayCountFactory.of("Unknown").getName(), "Unknown");
    assertEquals(FinmathDayCountFactory.of("UNKNOWN").getName(), "Unknown"); //should be case insensitive
  }
}
