/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;

/**
 * Formats double values to a fixed number of decimal places.
 */
public class DoubleValueDecimalPlaceFormatter extends DoubleValueFormatter {
  /** A 2 decimal place formatter. */
  public static final DoubleValueDecimalPlaceFormatter NON_CCY_2DP = DoubleValueDecimalPlaceFormatter.of(2, false);
  /** A 3 decimal place formatter. */
  public static final DoubleValueDecimalPlaceFormatter NON_CCY_3DP = DoubleValueDecimalPlaceFormatter.of(3, false);
  /** A 4 decimal place formatter. */
  public static final DoubleValueDecimalPlaceFormatter NON_CCY_4DP = DoubleValueDecimalPlaceFormatter.of(4, false);
  /** A 6 decimal place formatter. */
  public static final DoubleValueDecimalPlaceFormatter NON_CCY_6DP = DoubleValueDecimalPlaceFormatter.of(6, false);
  /** A 2 decimal place formatter with 3-letter ISO currency. */
  public static final DoubleValueDecimalPlaceFormatter CCY_2DP = DoubleValueDecimalPlaceFormatter.of(2, true);
  /** A 4 decimal place formatter with 3-letter ISO currency. */
  public static final DoubleValueDecimalPlaceFormatter CCY_4DP = DoubleValueDecimalPlaceFormatter.of(4, true);
  /** A 6 decimal place formatter with 3-letter ISO currency. */
  public static final DoubleValueDecimalPlaceFormatter CCY_6DP = DoubleValueDecimalPlaceFormatter.of(6, true);

  private final int _decimalPlaces;

  /**
   * Uses the format of the default locale.
   *
   * @param decimalPlaces
   *          the number of decimal places to use
   * @param isCurrencyAmount
   *          true if the value being formatted is a currency amount
   */
  public DoubleValueDecimalPlaceFormatter(final int decimalPlaces, final boolean isCurrencyAmount) {
    this(decimalPlaces, isCurrencyAmount, DecimalFormatSymbols.getInstance());
  }

  /**
   * @param decimalPlaces
   *          the number of decimal places to use
   * @param isCurrencyAmount
   *          true if the value being formatted is a currency amount
   * @param formatSymbols
   *          the formatting symbols
   */
  public DoubleValueDecimalPlaceFormatter(final int decimalPlaces, final boolean isCurrencyAmount, final DecimalFormatSymbols formatSymbols) {
    super(isCurrencyAmount, formatSymbols);
    _decimalPlaces = decimalPlaces;
  }

  /**
   * Uses the format of the default locale.
   *
   * @param decimalPlaces
   *          the number of decimal places to use
   * @param isCurrencyAmount
   *          true if the value being formatted is a currency amount
   * @return the formatter
   */
  public static DoubleValueDecimalPlaceFormatter of(final int decimalPlaces, final boolean isCurrencyAmount) {
    return new DoubleValueDecimalPlaceFormatter(decimalPlaces, isCurrencyAmount);
  }

  @Override
  public BigDecimal process(final BigDecimal value) {
    return value.setScale(_decimalPlaces, RoundingMode.HALF_UP);
  }

}
