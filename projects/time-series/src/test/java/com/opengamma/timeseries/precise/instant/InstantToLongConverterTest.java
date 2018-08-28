/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.timeseries.precise.instant;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

/**
 * Tests for {@link InstantToLongConverter}.
 */
public class InstantToLongConverterTest {

  /**
   * Provides instants and their long equivalents.
   *
   * @return  instants and longs
   */
  @DataProvider(name = "conversions")
  Object[][] dataConversions() {
    return new Object[][] {
      { Instant.ofEpochSecond(1234L), 1234000000000L},
      { Instant.ofEpochSecond(2234L), 2234000000000L},
      { Instant.ofEpochSecond(3234L), 3234000000000L},
      { Instant.ofEpochSecond(4234L), 4234000000000L},
      { Instant.ofEpochSecond(5234L), 5234000000000L},
      { Instant.ofEpochSecond(6234L), 6234000000000L},
      { Instant.MAX, Long.MAX_VALUE },
      { Instant.MIN, Long.MIN_VALUE }
    };
  }

  /**
   * Tests conversion of a instant to long.
   *
   * @param input  the input instant as long
   * @param expected  the expected long
   */
  @Test(dataProvider = "conversions")
  public void testConvertToLong(final Instant input, final long expected) {
    assertEquals(InstantToLongConverter.convertToLong(input), expected);
  }

  /**
   * Tests conversion of a long to instant.
   *
   * @param expected  the expected instant
   * @param input  the input instant as long
   */
  @Test(dataProvider = "conversions")
  public void testConvertToZonedDateTime(final Instant expected, final long input) {
    assertEquals(InstantToLongConverter.convertToInstant(input), expected);
  }

}
