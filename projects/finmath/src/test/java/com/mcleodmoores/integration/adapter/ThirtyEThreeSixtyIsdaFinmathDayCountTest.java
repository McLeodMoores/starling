/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.finmath.time.daycount.DayCountConventionInterface;
import net.finmath.time.daycount.DayCountConvention_30E_360_ISDA;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link ThirtyEThreeSixtyIsdaFinmathDayCount}.
 */
@Test
public class ThirtyEThreeSixtyIsdaFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void test() {
    final FinmathDayCount finmathDayCount = new ThirtyEThreeSixtyIsdaFinmathDayCount();
    assertEquals("30/360", finmathDayCount.getName());
    final Calendar firstDate = new GregorianCalendar();
    firstDate.set(2012, 1, 1);
    final Calendar secondDate = new GregorianCalendar();
    secondDate.set(2012, 7, 31);
    final DayCountConventionInterface convention = finmathDayCount.getConvention();
    final double dayCount = convention.getDaycount(firstDate, secondDate);
    final double dayCountFraction = convention.getDaycountFraction(firstDate, secondDate);
    final DayCountConvention_30E_360_ISDA newInstance = new DayCountConvention_30E_360_ISDA();
    assertEquals(newInstance.getDaycount(firstDate, secondDate), dayCount);
    assertEquals(210., dayCount);
    assertEquals(newInstance.getDaycountFraction(firstDate, secondDate), dayCountFraction);
    assertEquals(7 / 12., dayCountFraction);
  }
}
