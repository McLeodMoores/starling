/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.finmath.time.daycount.DayCountConventionInterface;
import net.finmath.time.daycount.DayCountConvention_NONE;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link NoneFinmathDayCount}.
 */
@Test
public class NoneFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void test() {
    final NoneFinmathDayCount finmathDayCount = new NoneFinmathDayCount();
    assertEquals("None", finmathDayCount.getName());
    final Calendar firstDate = new GregorianCalendar();
    firstDate.set(2012, 1, 1, 0, 0);
    final Calendar secondDate = new GregorianCalendar();
    secondDate.set(2012, 7, 31, 0, 0);
    final DayCountConventionInterface convention = finmathDayCount.getConvention();
    final double dayCount = convention.getDaycount(firstDate, secondDate);
    final double dayCountFraction = convention.getDaycountFraction(firstDate, secondDate);
    final DayCountConvention_NONE newInstance = new DayCountConvention_NONE();
    assertEquals(newInstance.getDaycount(firstDate, secondDate), dayCount);
    assertEquals(0., dayCount);
    assertEquals(newInstance.getDaycountFraction(firstDate, secondDate), dayCountFraction);
    assertEquals(1., dayCountFraction);
  }
}
