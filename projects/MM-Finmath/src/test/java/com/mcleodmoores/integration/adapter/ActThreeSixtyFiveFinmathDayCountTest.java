/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.finmath.time.daycount.DayCountConventionInterface;
import net.finmath.time.daycount.DayCountConvention_ACT_365;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link ActThreeSixtyFiveFinmathDayCount}.
 */
@Test
public class ActThreeSixtyFiveFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void test() {
    final FinmathDayCount finmathDayCount = new ActThreeSixtyFiveFinmathDayCount();
    assertEquals("Act/365", finmathDayCount.getName());
    final Calendar firstDate = new GregorianCalendar();
    firstDate.set(2012, 1, 1, 0, 0);
    final Calendar secondDate = new GregorianCalendar();
    secondDate.set(2012, 7, 31, 0, 0);
    final DayCountConventionInterface convention = finmathDayCount.getConvention();
    final double dayCount = convention.getDaycount(firstDate, secondDate);
    final double dayCountFraction = convention.getDaycountFraction(firstDate, secondDate);
    final DayCountConvention_ACT_365 newInstance = new DayCountConvention_ACT_365();
    assertEquals(newInstance.getDaycount(firstDate, secondDate), dayCount);
    assertEquals(212., dayCount);
    assertEquals(newInstance.getDaycountFraction(firstDate, secondDate), dayCountFraction);
    assertEquals(212 / 365., dayCountFraction);
  }
}
