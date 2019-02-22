/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;

/**
 *
 */
public class DoubleValueSignificantFiguresFormatter extends DoubleValueFormatter {
  /** A 5 significant figure formatter. */
  public static final DoubleValueSignificantFiguresFormatter NON_CCY_5SF = DoubleValueSignificantFiguresFormatter.of(5, false);

  private final BigDecimal _maxValueForSigFig;
  private final MathContext _sigFigMathContext;

  /**
   * Uses the format of the default locale.
   *
   * @param significantFigures
   *          the number of significant figures to use
   * @param isCurrencyAmount
   *          true if the value being formatted is a currency amount
   */
  public DoubleValueSignificantFiguresFormatter(final int significantFigures, final boolean isCurrencyAmount) {
    this(significantFigures, isCurrencyAmount, DecimalFormatSymbols.getInstance());
  }

  /**
   * @param significantFigures
   *          the number of significant figures to use
   * @param isCurrencyAmount
   *          true if the value being formatted is a currency amount
   * @param formatSymbols
   *          the formatting symbols
   */
  public DoubleValueSignificantFiguresFormatter(final int significantFigures, final boolean isCurrencyAmount, final DecimalFormatSymbols formatSymbols) {
    super(isCurrencyAmount, formatSymbols);
    _maxValueForSigFig = BigDecimal.TEN.pow(significantFigures - 1);
    _sigFigMathContext = new MathContext(significantFigures, RoundingMode.HALF_UP);
  }

  /**
   * Uses the format of the default locale.
   *
   * @param significantFigures
   *          the number of significant figures to use
   * @param isCurrencyAmount
   *          true if the value being formatted is a currency amount
   * @return the formatter
   */
  public static DoubleValueSignificantFiguresFormatter of(final int significantFigures, final boolean isCurrencyAmount) {
    return new DoubleValueSignificantFiguresFormatter(significantFigures, isCurrencyAmount);
  }

  @Override
  public BigDecimal process(final BigDecimal bigDecimalValue) {
    if (bigDecimalValue.abs().compareTo(_maxValueForSigFig) > 0) {
      return bigDecimalValue.setScale(0, RoundingMode.HALF_UP);
    }
    return bigDecimalValue.round(_sigFigMathContext);
  }

}
