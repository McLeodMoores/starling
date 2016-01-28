/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

import net.finmath.time.daycount.DayCountConventionInterface;
import net.finmath.time.daycount.DayCountConvention_30U_360;

/**
 * Unit tests for {@link ThirtyUThreeSixtyNotEomFinmathDayCount}.
 */
@Test(groups = TestGroup.UNIT)
public class ThirtyUThreeSixtyNotEomFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void test() {
    final FinmathDayCount finmathDayCount = new ThirtyUThreeSixtyNotEomFinmathDayCount();
    assertEquals("30U/360 not EOM", finmathDayCount.getName());
    final LocalDate firstDate = new LocalDate(2012, 1, 1);
    final LocalDate secondDate = new LocalDate(2012, 7, 31);
    final DayCountConventionInterface convention = finmathDayCount.getConvention();
    final double dayCount = convention.getDaycount(firstDate, secondDate);
    final double dayCountFraction = convention.getDaycountFraction(firstDate, secondDate);
    final DayCountConvention_30U_360 newInstance = new DayCountConvention_30U_360(false);
    assertEquals(newInstance.getDaycount(firstDate, secondDate), dayCount);
    assertEquals(210., dayCount);
    assertEquals(newInstance.getDaycountFraction(firstDate, secondDate), dayCountFraction);
    assertEquals(7 / 12., dayCountFraction);
  }

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void testEom() {
    final FinmathDayCount finmathDayCount = new ThirtyUThreeSixtyNotEomFinmathDayCount();
    assertEquals("30U/360 not EOM", finmathDayCount.getName());
    final LocalDate firstDate = new LocalDate(2012, 2, 29);
    final LocalDate secondDate = new LocalDate(2012, 3, 29);
    final DayCountConventionInterface convention = finmathDayCount.getConvention();
    final double dayCount = convention.getDaycount(firstDate, secondDate);
    final double dayCountFraction = convention.getDaycountFraction(firstDate, secondDate);
    final DayCountConvention_30U_360 newInstance = new DayCountConvention_30U_360(false);
    assertEquals(newInstance.getDaycount(firstDate, secondDate), dayCount);
    assertEquals(30., dayCount);
    assertEquals(newInstance.getDaycountFraction(firstDate, secondDate), dayCountFraction);
    assertEquals(30 / 360., dayCountFraction);
  }
}
