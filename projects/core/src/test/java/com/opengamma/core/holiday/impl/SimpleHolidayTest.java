/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link SimpleHoliday}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleHolidayTest extends AbstractFudgeBuilderTestCase {
  private static final LocalDate DATE1 = LocalDate.of(2013, 6, 1);
  private static final LocalDate DATE2 = LocalDate.of(2013, 6, 2);
  private static final LocalDate DATE3 = LocalDate.of(2013, 6, 3);
  private static final LocalDate DATE4 = LocalDate.of(2013, 6, 4);
  private static final LocalDate DATE5 = LocalDate.of(2013, 6, 5);

  /**
   * Tests the addition of holiday dates.
   */
  @Test
  public void testAddHolidayDate() {
    final SimpleHoliday holidays = new SimpleHoliday();
    holidays.addHolidayDate(DATE3);
    holidays.addHolidayDate(DATE2);
    holidays.addHolidayDate(DATE4);
    holidays.addHolidayDate(DATE2);
    holidays.addHolidayDate(DATE1);

    assertEquals(4, holidays.getHolidayDates().size());
    assertEquals(DATE1, holidays.getHolidayDates().get(0));
    assertEquals(DATE2, holidays.getHolidayDates().get(1));
    assertEquals(DATE3, holidays.getHolidayDates().get(2));
    assertEquals(DATE4, holidays.getHolidayDates().get(3));
  }

  /**
   * Tests the addition of holiday dates.
   */
  @Test
  public void testAddHolidayDatesIterable() {
    final SimpleHoliday holidays = new SimpleHoliday();
    holidays.addHolidayDate(DATE3);
    holidays.addHolidayDates(ImmutableList.of(DATE4, DATE3, DATE2, DATE5));

    assertEquals(4, holidays.getHolidayDates().size());
    assertEquals(DATE2, holidays.getHolidayDates().get(0));
    assertEquals(DATE3, holidays.getHolidayDates().get(1));
    assertEquals(DATE4, holidays.getHolidayDates().get(2));
    assertEquals(DATE5, holidays.getHolidayDates().get(3));
  }

  /**
   * Tests constructing a holiday with dates.
   */
  @Test
  public void testConstructorIterable() {
    final SimpleHoliday holidays = new SimpleHoliday(ImmutableList.of(DATE4, DATE3, DATE2, DATE3, DATE5));

    assertEquals(4, holidays.getHolidayDates().size());
    assertEquals(DATE2, holidays.getHolidayDates().get(0));
    assertEquals(DATE3, holidays.getHolidayDates().get(1));
    assertEquals(DATE4, holidays.getHolidayDates().get(2));
    assertEquals(DATE5, holidays.getHolidayDates().get(3));
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SimpleHoliday holidays = new SimpleHoliday(ImmutableList.of(DATE4, DATE3, DATE2, DATE3, DATE5));
    holidays.setCurrency(Currency.USD);
    assertEncodeDecodeCycle(SimpleHoliday.class, holidays);
  }

}
