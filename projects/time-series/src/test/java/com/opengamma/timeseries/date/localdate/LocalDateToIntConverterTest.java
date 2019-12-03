/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * Tests for {@link LocalDateToIntConverter}.
 */
@Test(groups = "unit")
public class LocalDateToIntConverterTest {

  /**
   * Provides dates and their int equivalents.
   *
   * @return  dates and ints
   */
  @DataProvider(name = "conversions")
  Object[][] dataConversions() {
    return new Object[][] {
        {LocalDate.of(2012, 1, 1), 20120101},
        {LocalDate.of(2012, 6, 30), 20120630},
        {LocalDate.of(2012, 12, 31), 20121231},
        {LocalDate.of(2012, 2, 29), 20120229},
        {LocalDate.of(9999, 12, 31), 99991231},
        {LocalDate.of(0, 1, 1), 101},
        {LocalDate.MAX, Integer.MAX_VALUE},
        {LocalDate.MIN, Integer.MIN_VALUE},
    };
  }

  /**
   * Tests conversion of a date to int.
   *
   * @param input  the input date as integer
   * @param expected  the expected int
   */
  @Test(dataProvider = "conversions")
  public void testConvertToInt(final LocalDate input, final int expected) {
    assertEquals(LocalDateToIntConverter.convertToInt(input), expected);
  }

  /**
   * Tests conversion of a int to date.
   *
   * @param expected  the expected date
   * @param input  the input date as integer
   */
  @Test(dataProvider = "conversions")
  public void testConvertToLocalDate(final LocalDate expected, final int input) {
    assertEquals(LocalDateToIntConverter.convertToLocalDate(input), expected);
  }

  /**
   * Tests that a year greater than 9999 is not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConvertToLocalDateTooBig() {
    LocalDateToIntConverter.convertToInt(LocalDate.of(10_000, 1, 1));
  }

  /**
   * Tests that a negative year is not allowed.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConvertToLocalDateTooSmall() {
    LocalDateToIntConverter.convertToInt(LocalDate.of(-1, 1, 1));
  }

  /**
   * Provides invalid dates.
   *
   * @return  dates
   */
  @DataProvider(name = "invalid")
  Object[][] dataInvalid() {
    return new Object[][] {
        {20120100},
        {20120132},
        {20120001},
        {20121301},
        {20120230},
        {20120231},
        {20120431},
        {20120631},
        {20120931},
        {20121131},
    };
  }

  /**
   * Checks invalid dates.
   *
   * @param date  a date
   */
  @Test(dataProvider = "invalid", expectedExceptions = IllegalArgumentException.class)
  public void testCheckInvalid(final int date) {
    LocalDateToIntConverter.checkValid(date);
  }

  /**
   * Checks that a negative year is not valid.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDateInvalid() {
    LocalDateToIntConverter.checkValid(-20120101);
  }

  /**
   * Checks that the integer min value is not valid.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIntMinValueInvalid() {
    LocalDateToIntConverter.checkValid(Integer.MIN_VALUE);
  }

  /**
   * Checks that the maximum year is 9999.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTooLargeDateInvalid() {
    LocalDateToIntConverter.checkValid(100000101);
  }

  /**
   * Checks that the integer max value is not valid.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIntMaxValueInvalid() {
    LocalDateToIntConverter.checkValid(Integer.MAX_VALUE);
  }

}
