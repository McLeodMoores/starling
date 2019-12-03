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
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link SimpleHolidayWithWeekend}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleHolidayWithWeekendTest extends AbstractFudgeBuilderTestCase {
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
    final SimpleHolidayWithWeekend holidays = new SimpleHolidayWithWeekend();
    holidays.addHolidayDate(DATE3);
    holidays.addHolidayDate(DATE2);
    holidays.addHolidayDate(DATE4);
    holidays.addHolidayDate(DATE2);
    holidays.addHolidayDate(DATE1);
    holidays.setWeekendType(WeekendType.THURSDAY_FRIDAY);

    assertEquals(4, holidays.getHolidayDates().size());
    assertEquals(DATE1, holidays.getHolidayDates().get(0));
    assertEquals(DATE2, holidays.getHolidayDates().get(1));
    assertEquals(DATE3, holidays.getHolidayDates().get(2));
    assertEquals(DATE4, holidays.getHolidayDates().get(3));
    assertEquals(WeekendType.THURSDAY_FRIDAY, holidays.getWeekendType());
  }

  /**
   * Tests the addition of holiday dates.
   */
  @Test
  public void testAddHolidayDatesIterable() {
    final SimpleHolidayWithWeekend holidays = new SimpleHolidayWithWeekend();
    holidays.addHolidayDate(DATE3);
    holidays.addHolidayDates(ImmutableList.of(DATE4, DATE3, DATE2, DATE5));

    assertEquals(4, holidays.getHolidayDates().size());
    assertEquals(DATE2, holidays.getHolidayDates().get(0));
    assertEquals(DATE3, holidays.getHolidayDates().get(1));
    assertEquals(DATE4, holidays.getHolidayDates().get(2));
    assertEquals(DATE5, holidays.getHolidayDates().get(3));
    assertEquals(WeekendType.SATURDAY_SUNDAY, holidays.getWeekendType());
  }

  /**
   * Tests constructing a holiday with dates.
   */
  @Test
  public void testConstructorIterable() {
    final SimpleHolidayWithWeekend holidays =
        new SimpleHolidayWithWeekend(ImmutableList.of(DATE4, DATE3, DATE2, DATE3, DATE5), WeekendType.FRIDAY_SATURDAY);

    assertEquals(4, holidays.getHolidayDates().size());
    assertEquals(DATE2, holidays.getHolidayDates().get(0));
    assertEquals(DATE3, holidays.getHolidayDates().get(1));
    assertEquals(DATE4, holidays.getHolidayDates().get(2));
    assertEquals(DATE5, holidays.getHolidayDates().get(3));
    assertEquals(WeekendType.FRIDAY_SATURDAY, holidays.getWeekendType());
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final SimpleHolidayWithWeekend holidays =
        new SimpleHolidayWithWeekend(ImmutableList.of(DATE4, DATE3, DATE2, DATE3, DATE5), WeekendType.NONE);
    holidays.setCurrency(Currency.USD);
    assertEncodeDecodeCycle(SimpleHolidayWithWeekend.class, holidays);
  }

}
