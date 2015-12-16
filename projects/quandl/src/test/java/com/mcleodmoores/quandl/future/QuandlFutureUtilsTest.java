/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.quandl.future.QuandlFutureUtils;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link QuandlFutureUtils}.
 */
public class QuandlFutureUtilsTest {

  /**
   * Tests behaviour when the future tenor is null.
   */
  @Test
  public void testNullFutureTenor() {
    try {
      QuandlFutureUtils.getCodeForFuture(null, 3, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getExpiryYear(null, 3, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getMonthCode(null, 3, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
  }

  /**
   * Tests the behaviour when the future number is not positive.
   */
  @Test
  public void testNonPositiveFutureNumber() {
    try {
      QuandlFutureUtils.getCodeForFuture(Tenor.THREE_MONTHS, 0, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getCodeForFuture(Tenor.THREE_MONTHS, -1, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getExpiryYear(Tenor.THREE_MONTHS, 0, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getExpiryYear(Tenor.THREE_MONTHS, -1, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getMonthCode(Tenor.THREE_MONTHS, 0, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getMonthCode(Tenor.THREE_MONTHS, -1, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
  }

  /**
   * Tests the behaviour when the date is null.
   */
  @Test
  public void testNullDate() {
    try {
      QuandlFutureUtils.getCodeForFuture(Tenor.THREE_MONTHS, 1, null);
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getExpiryYear(Tenor.THREE_MONTHS, 1, null);
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getMonthCode(Tenor.THREE_MONTHS, 1, null);
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
  }

  /**
   * Tests the behaviour when an unsupported tenor is supplied.
   */
  @Test
  public void testUnsupportedTenor() {
    try {
      QuandlFutureUtils.getCodeForFuture(Tenor.TWO_MONTHS, 1, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getExpiryYear(Tenor.TWO_MONTHS, 1, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
    try {
      QuandlFutureUtils.getMonthCode(Tenor.TWO_MONTHS, 1, LocalDate.of(2015, 1, 1));
      fail();
    } catch (final Quandl4OpenGammaRuntimeException e) {
      // expected
    }
  }

  /**
   * Tests expiry year generation.
   */
  @Test
  public void testExpiryYearGeneration() {
    final LocalDate date = LocalDate.of(2015, 1, 1);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.THREE_MONTHS, 1, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.THREE_MONTHS, 2, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.THREE_MONTHS, 3, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.THREE_MONTHS, 4, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.THREE_MONTHS, 5, date), 2016);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 1, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 2, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 3, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 4, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 5, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 6, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 7, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 8, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 9, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 10, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 11, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 12, date), 2015);
    assertEquals(QuandlFutureUtils.getExpiryYear(Tenor.ONE_MONTH, 13, date), 2016);
  }

  /**
   * Tests expiry year generation.
   */
  @Test
  public void testMonthCodeGeneration() {
    final LocalDate date = LocalDate.of(2015, 1, 1);
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.THREE_MONTHS, 1, date).charValue(), 'H');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.THREE_MONTHS, 2, date).charValue(), 'M');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.THREE_MONTHS, 3, date).charValue(), 'U');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.THREE_MONTHS, 4, date).charValue(), 'Z');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.THREE_MONTHS, 5, date).charValue(), 'H');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 1, date).charValue(), 'F');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 2, date).charValue(), 'G');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 3, date).charValue(), 'H');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 4, date).charValue(), 'J');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 5, date).charValue(), 'K');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 6, date).charValue(), 'M');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 7, date).charValue(), 'N');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 8, date).charValue(), 'Q');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 9, date).charValue(), 'U');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 10, date).charValue(), 'V');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 11, date).charValue(), 'X');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 12, date).charValue(), 'Z');
    assertEquals(QuandlFutureUtils.getMonthCode(Tenor.ONE_MONTH, 13, date).charValue(), 'F');
  }
}
