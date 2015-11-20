/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotSame;
import net.finmath.time.daycount.DayCountConvention_ACT_360;
import net.finmath.time.daycount.DayCountConvention_NL_365;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link FinmathDayCount}.
 */
@Test
public class FinmathDayCountTest {
  /** The instance to test */
  private static final FinmathDayCount DAY_COUNT = new NlThreeSixtyFiveFinmathDayCount();

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    assertEquals("NL/365", DAY_COUNT.getName());
    assertEquals(DayCountConvention_NL_365.class, DAY_COUNT.getConvention().getClass());
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void hashCodeEquals() {
    FinmathDayCount dayCount = new NlThreeSixtyFiveFinmathDayCount();
    assertEquals(DAY_COUNT, DAY_COUNT);
    assertNotEquals(null, DAY_COUNT); // catch any missed check for null
    assertNotSame(DAY_COUNT, dayCount);
    assertEquals(DAY_COUNT, dayCount);
    assertEquals(DAY_COUNT.hashCode(), dayCount.hashCode());
    dayCount = new FinmathDayCount("NL/365 Test", new DayCountConvention_NL_365()) {
    };
    assertNotEquals(DAY_COUNT, dayCount);
    dayCount = new FinmathDayCount("NL/365", new DayCountConvention_ACT_360()) {
    };
    assertNotEquals(DAY_COUNT, dayCount);
  }
}
