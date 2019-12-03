/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.timeseries.precise.zdt;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoField;

/**
 * Tests for {@link ZonedDateTimeToLongConverter}.
 */
public class ZonedDateTimeToLongConverterTest {
  private static final ZoneId ZONE = ZoneId.systemDefault();

  /**
   * Provides dates and their long equivalents.
   *
   * @return  dates and longs
   */
  @DataProvider(name = "conversions")
  Object[][] dataConversions() {
    return new Object[][] {
      { ZonedDateTime.ofInstant(Instant.ofEpochSecond(1234L), ZONE), 1234000000000L},
      { ZonedDateTime.ofInstant(Instant.ofEpochSecond(2234L), ZONE), 2234000000000L},
      { ZonedDateTime.ofInstant(Instant.ofEpochSecond(3234L), ZONE), 3234000000000L},
      { ZonedDateTime.ofInstant(Instant.ofEpochSecond(4234L), ZONE), 4234000000000L},
      { ZonedDateTime.ofInstant(Instant.ofEpochSecond(5234L), ZONE), 5234000000000L},
      { ZonedDateTime.ofInstant(Instant.ofEpochSecond(6234L), ZONE), 6234000000000L},
    };
  }

  /**
   * Tests conversion of a date to long.
   *
   * @param input  the input date as long
   * @param expected  the expected long
   */
  @Test(dataProvider = "conversions")
  public void testConvertToLong(final ZonedDateTime input, final long expected) {
    assertEquals(ZonedDateTimeToLongConverter.convertToLong(input), expected);
    final ZonedDateTime greaterThanMaxDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1000L), ZONE).with(ChronoField.YEAR, 2000000);
    assertEquals(ZonedDateTimeToLongConverter.convertToLong(greaterThanMaxDate), Long.MAX_VALUE);
    final ZonedDateTime lessThanMinDate = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1000L), ZONE).with(ChronoField.YEAR, -2000000);
    assertEquals(ZonedDateTimeToLongConverter.convertToLong(lessThanMinDate), Long.MIN_VALUE);
  }

  /**
   * Tests conversion of a long to date.
   *
   * @param expected  the expected date
   * @param input  the input date as long
   */
  @Test(dataProvider = "conversions")
  public void testConvertToZonedDateTime(final ZonedDateTime expected, final long input) {
    assertEquals(ZonedDateTimeToLongConverter.convertToZonedDateTime(input, ZONE), expected);
    assertEquals(ZonedDateTimeToLongConverter.convertToZonedDateTime(Long.MAX_VALUE, ZONE), LocalDateTime.MAX.atZone(ZONE));
    assertEquals(ZonedDateTimeToLongConverter.convertToZonedDateTime(Long.MIN_VALUE, ZONE), LocalDateTime.MIN.atZone(ZONE));
  }
}
