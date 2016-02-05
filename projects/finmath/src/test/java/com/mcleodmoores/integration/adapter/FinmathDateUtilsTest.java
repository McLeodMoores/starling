/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

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
    final org.joda.time.LocalDate jodaTimeDate = FinmathDateUtils.convertToJodaLocalDate(localDate);
    assertEquals(jodaTimeDate, new org.joda.time.LocalDate(year, month, day));
    assertEquals(localDate, FinmathDateUtils.convertFromJodaLocalDateDate(jodaTimeDate));
  }

}
