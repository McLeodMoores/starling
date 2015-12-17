/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
    final Calendar firstDate = new GregorianCalendar();
    firstDate.set(2012, 1, 1, 0, 0);
    final Calendar secondDate = new GregorianCalendar();
    secondDate.set(2012, 7, 31, 0, 0);
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
