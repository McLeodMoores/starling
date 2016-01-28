/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

import net.finmath.time.daycount.DayCountConventionInterface;

/**
 * Unit tests for {@link UnknownFinmathDayCount}.
 */
@Test(groups = TestGroup.UNIT)
public class UnknownFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void test() {
    final UnknownFinmathDayCount finmathDayCount = new UnknownFinmathDayCount();
    assertEquals("Unknown", finmathDayCount.getName());
    final LocalDate firstDate = new LocalDate(2012, 1, 1);
    final LocalDate secondDate = new LocalDate(2012, 7, 31);
    final DayCountConventionInterface convention = finmathDayCount.getConvention();
    try {
      convention.getDaycount(firstDate, secondDate);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
    try {
      convention.getDaycountFraction(firstDate, secondDate);
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }
}
