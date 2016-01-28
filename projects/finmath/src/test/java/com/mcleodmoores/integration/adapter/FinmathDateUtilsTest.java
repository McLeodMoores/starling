/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertEquals;

import java.util.Calendar;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

/**
 * Unit tests for {@link FinmathDayCount}.
 */
public class FinmathDateUtilsTest {

  /**
   * Tests conversion of {@link LocalDate}.
   */
  @Test
  public void testLocalDateConversion() {
    final int year = 2014;
    final int month = 6;
    final int day = 15;
    final LocalDate localDate = LocalDate.of(year, month, day);
    final Calendar calendar = FinmathDateUtils.convertLocalDate(localDate);
    assertEquals(year, calendar.get(Calendar.YEAR));
    assertEquals(month - 1, calendar.get(Calendar.MONTH));
    assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
  }

  /**
   * Tests conversion of {@link LocalDate}.
   */
  @Test
  public void testZonedDateTimeConversion() {
    final int year = 2014;
    final int month = 6;
    final int day = 15;
    final int hour = 10;
    final int minute = 45;
    final int second = 30;
    final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.of(year, month, day, hour, minute, second), ZoneOffset.UTC);
    final Calendar calendar = FinmathDateUtils.convertZonedDateTime(zonedDateTime);
    assertEquals(year, calendar.get(Calendar.YEAR));
    assertEquals(month - 1, calendar.get(Calendar.MONTH));
    assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
    assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(minute, calendar.get(Calendar.MINUTE));
    assertEquals(second, calendar.get(Calendar.SECOND));
    assertEquals("Greenwich Mean Time", calendar.getTimeZone().getDisplayName());
  }
}
