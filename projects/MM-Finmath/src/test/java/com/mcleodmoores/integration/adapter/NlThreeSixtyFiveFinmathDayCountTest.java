/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.finmath.time.daycount.DayCountConventionInterface;
import net.finmath.time.daycount.DayCountConvention_NL_365;

import org.testng.annotations.Test;

import com.mcleodmoores.integration.adapter.FinmathDayCount;
import com.mcleodmoores.integration.adapter.NlThreeSixtyFiveFinmathDayCount;

/**
 * Unit tests for {@link NlThreeSixtyFiveFinmathDayCount}.
 */
@Test
public class NlThreeSixtyFiveFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void test() {
    final FinmathDayCount finmathDayCount = new NlThreeSixtyFiveFinmathDayCount();
    assertEquals("NL/365", finmathDayCount.getName());
    final Calendar firstDate = new GregorianCalendar();
    firstDate.set(2012, 1, 1, 0, 0);
    final Calendar secondDate = new GregorianCalendar();
    secondDate.set(2012, 7, 31, 0, 0);
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
