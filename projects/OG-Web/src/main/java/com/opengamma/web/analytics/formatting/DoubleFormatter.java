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

/**
 *
 */
/* package */ class DoubleFormatter extends AbstractFormatter<Double> {

  private final BigDecimalFormatter _bigDecimalFormatter;

  DoubleFormatter(final BigDecimalFormatter bigDecimalFormatter) {
    super(Double.class);
    ArgumentChecker.notNull(bigDecimalFormatter, "bigDecimalFormatter");
    _bigDecimalFormatter = bigDecimalFormatter;
    addFormatter(new Formatter<Double>(Format.HISTORY) {
      @Override
      protected Object formatValue(final Double value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatHistory(value, valueSpec);
      }
    });
    addFormatter(new Formatter<Double>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final Double value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(final Double value, final ValueSpecification valueSpec, final Object inlineKey) {
    final BigDecimal bigDecimal = convertToBigDecimal(value);
    if (bigDecimal == null) {
      return Double.toString(value);
    } else {
      return _bigDecimalFormatter.formatCell(bigDecimal, valueSpec, inlineKey);
    }
  }

  private Object formatExpanded(final Double value, final ValueSpecification valueSpec) {
    final BigDecimal bigDecimal = convertToBigDecimal(value);
    if (bigDecimal == null) {
      return Double.toString(value);
    } else {
      return _bigDecimalFormatter.format(bigDecimal, valueSpec, Format.EXPANDED, null);
    }
  }

  private Object formatHistory(final Double history, final ValueSpecification valueSpec) {
    final BigDecimal bigDecimal = convertToBigDecimal(history);
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

  private static BigDecimal convertToBigDecimal(final Double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return null;
    } else {
      return new BigDecimal(value.toString());
    }
  }
}
