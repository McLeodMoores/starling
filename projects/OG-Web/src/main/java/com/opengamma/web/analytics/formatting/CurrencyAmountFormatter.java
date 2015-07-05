/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import java.math.BigDecimal;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
/* package */ class CurrencyAmountFormatter extends AbstractFormatter<CurrencyAmount> {

  private final BigDecimalFormatter _bigDecimalFormatter;
  private final ResultsFormatter.CurrencyDisplay _currencyDisplay;

  /* package */ CurrencyAmountFormatter(final ResultsFormatter.CurrencyDisplay currencyDisplay,
                                        final BigDecimalFormatter bigDecimalFormatter) {
    super(CurrencyAmount.class);
    ArgumentChecker.notNull(bigDecimalFormatter, "bigDecimalFormatter");
    ArgumentChecker.notNull(currencyDisplay, "currencyDisplay");
    _bigDecimalFormatter = bigDecimalFormatter;
    _currencyDisplay = currencyDisplay;
    addFormatter(new Formatter<CurrencyAmount>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final CurrencyAmount value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
    addFormatter(new Formatter<CurrencyAmount>(Format.HISTORY) {
      @Override
      protected Object formatValue(final CurrencyAmount value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatHistory(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(final CurrencyAmount value, final ValueSpecification valueSpec, final Object inlineKey) {
    final double amount = value.getAmount();
    final BigDecimal bigDecimal = convertToBigDecimal(amount);
    return bigDecimal == null ?
        Double.toString(amount) :
        formatValue(value, valueSpec, inlineKey, bigDecimal);
  }

  private String formatValue(final CurrencyAmount value,
                             final ValueSpecification valueSpec,
                             final Object inlineKey,
                             final BigDecimal bigDecimal) {

    final String prefix = _currencyDisplay == ResultsFormatter.CurrencyDisplay.DISPLAY_CURRENCY ?
        value.getCurrency().getCode() + " " :
        "";
    return prefix + _bigDecimalFormatter.formatCell(bigDecimal, valueSpec, inlineKey);
  }

  private Object formatExpanded(final CurrencyAmount value, final ValueSpecification valueSpec) {
    final double amount = value.getAmount();
    final BigDecimal bigDecimal = convertToBigDecimal(amount);
    if (bigDecimal == null) {
      return Double.toString(amount);
    } else {
      return _bigDecimalFormatter.format(bigDecimal, valueSpec, Format.EXPANDED, null);
    }
  }

  private Object formatHistory(final CurrencyAmount history, final ValueSpecification valueSpec) {
    final double amount = history.getAmount();
    final BigDecimal bigDecimal = convertToBigDecimal(amount);
    if (bigDecimal == null) {
      return null;
    } else {
      return _bigDecimalFormatter.format(bigDecimal, valueSpec, Format.HISTORY, null);
    }
  }

  @Override
  public DataType getDataType() {
    return DataType.DOUBLE;
  }

  /**
   * @param value A double value, not null
   * @return The value converted to a {@link BigDecimal}, null if the value is infinite or not a number
   */
  private static BigDecimal convertToBigDecimal(final Double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return null;
    } else {
      return new BigDecimal(value.toString());
    }
  }
}
