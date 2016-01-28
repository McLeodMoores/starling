/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.AssertJUnit.assertEquals;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

import net.finmath.time.daycount.DayCountConventionInterface;
import net.finmath.time.daycount.DayCountConvention_30E_360_ISDA;

/**
 * Unit tests for {@link ThirtyEThreeSixtyIsdaWithTerminationDateFinmathDayCount}.
 */
@Test(groups = TestGroup.UNIT)
public class ThirtyEThreeSixtyIsdaWithTerminationDateFinmathDayCountTest {

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void test() {
    final FinmathDayCount finmathDayCount = new ThirtyEThreeSixtyIsdaWithTerminationDateFinmathDayCount();
    assertEquals("30E/360 ISDA Termination", finmathDayCount.getName());
    final LocalDate firstDate = new LocalDate(2012, 1, 1);
    final LocalDate secondDate = new LocalDate(2012, 7, 31);
    final DayCountConventionInterface convention = finmathDayCount.getConvention();
    final double dayCount = convention.getDaycount(firstDate, secondDate);
    final double dayCountFraction = convention.getDaycountFraction(firstDate, secondDate);
    final DayCountConvention_30E_360_ISDA newInstance = new DayCountConvention_30E_360_ISDA(true);
    assertEquals(newInstance.getDaycount(firstDate, secondDate), dayCount);
    assertEquals(209., dayCount);
    assertEquals(newInstance.getDaycountFraction(firstDate, secondDate), dayCountFraction);
    assertEquals(209 / 360., dayCountFraction);
  }

  /**
   * Tests that the expected day count is constructed.
   */
  @Test
  public void testTerminationDate() {
    final FinmathDayCount finmathDayCount = new ThirtyEThreeSixtyIsdaWithTerminationDateFinmathDayCount();
    assertEquals("30E/360 ISDA Termination", finmathDayCount.getName());
    final LocalDate firstDate = new LocalDate(2012, 1, 1);
    final LocalDate secondDate = new LocalDate(2012, 2, 29);
    final DayCountConventionInterface convention = finmathDayCount.getConvention();
    final double dayCount = convention.getDaycount(firstDate, secondDate);
    final double dayCountFraction = convention.getDaycountFraction(firstDate, secondDate);
    final DayCountConvention_30E_360_ISDA newInstance = new DayCountConvention_30E_360_ISDA(true);
    assertEquals(newInstance.getDaycount(firstDate, secondDate), dayCount);
    assertEquals(58., dayCount);
    assertEquals(newInstance.getDaycountFraction(firstDate, secondDate), dayCountFraction);
    assertEquals(58 / 360., dayCountFraction);
  }
}
