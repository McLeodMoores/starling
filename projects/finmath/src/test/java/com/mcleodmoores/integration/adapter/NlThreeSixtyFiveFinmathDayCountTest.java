/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

import net.finmath.time.daycount.DayCountConventionInterface;
import net.finmath.time.daycount.DayCountConvention_NL_365;

/**
 * Unit tests for {@link NlThreeSixtyFiveFinmathDayCount}.
 */
@Test(groups = TestGroup.UNIT)
public class NlThreeSixtyFiveFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void test() {
    final FinmathDayCount finmathDayCount = new NlThreeSixtyFiveFinmathDayCount();
    assertEquals("NL/365", finmathDayCount.getName());
    final LocalDate firstDate = new LocalDate(2012, 1, 1);
    final LocalDate secondDate = new LocalDate(2012, 7, 31);
    final DayCountConventionInterface convention = finmathDayCount.getConvention();
    final double dayCount = convention.getDaycount(firstDate, secondDate);
    final double dayCountFraction = convention.getDaycountFraction(firstDate, secondDate);
    final DayCountConvention_NL_365 newInstance = new DayCountConvention_NL_365();
    assertEquals(newInstance.getDaycount(firstDate, secondDate), dayCount);
    assertEquals(211., dayCount);
    assertEquals(newInstance.getDaycountFraction(firstDate, secondDate), dayCountFraction);
    assertEquals(211 / 365., dayCountFraction);
  }
}
