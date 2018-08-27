/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class MultipleCurrencyAmountConverter implements ResultConverter<MultipleCurrencyAmount> {

  private final DoubleConverter _doubleConverter;

  public MultipleCurrencyAmountConverter(final DoubleConverter doubleConverter) {
    _doubleConverter = doubleConverter;
  }

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final MultipleCurrencyAmount value, final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();
    final int length = value.size();
    result.put("summary", length);

    if (mode == ConversionMode.FULL) {
      final Map<Object, Object> labelledValues = new LinkedHashMap<>();
      final Iterator<CurrencyAmount> iter = value.iterator();
      while (iter.hasNext()) {
        final CurrencyAmount ca = iter.next();
        final String label = ca.getCurrency().getCode();
        final Object currentLabel = context.convert(label, ConversionMode.SUMMARY);
        final Object currentValue = _doubleConverter.convertForDisplay(context, valueSpec, ca.getAmount(), ConversionMode.SUMMARY);
        labelledValues.put(currentLabel, currentValue);
      }
      result.put("full", labelledValues);
    }
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final MultipleCurrencyAmount value) {
    return null;
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_1D";
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final MultipleCurrencyAmount value) {
    return value.toString();
  }

}
