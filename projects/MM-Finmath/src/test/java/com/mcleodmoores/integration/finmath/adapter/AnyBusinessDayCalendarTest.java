/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Units tests for {@link AnyBusinessDayCalendar}.
 */
public class AnyBusinessDayCalendarTest {
  /** The business day calendar */
  private static final FinmathBusinessDay BUSINESS_DAY = new AnyBusinessDayCalendar();

  /**
   * Tests that all days in a period are business days.
   */
  @Test
  public void test() {
    LocalDate date = LocalDate.of(2000, 1, 1);
    final LocalDate end = LocalDate.of(2015, 1, 1);
    while (date.isBefore(end)) {
      assertTrue(BUSINESS_DAY.isBusinessday(FinmathDateUtils.convertLocalDate(date)));
      date = date.plusDays(1);
    }
  }
}
