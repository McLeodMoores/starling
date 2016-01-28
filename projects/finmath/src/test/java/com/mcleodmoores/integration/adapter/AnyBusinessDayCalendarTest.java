/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

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
    LocalDate date = new LocalDate(2000, 1, 1);
    final LocalDate end = new LocalDate(2015, 1, 1);
    while (date.isBefore(end)) {
      assertTrue(BUSINESS_DAY.isBusinessday(date));
      date = date.plusDays(1);
    }
  }
}
