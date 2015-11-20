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

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/* package */ class MultipleCurrencyAmountFormatter extends AbstractFormatter<MultipleCurrencyAmount> {

  private static final String DATA = "data";
  private static final String LABELS = "labels";
  private static final String CURRENCY = "Currency";
  private static final String AMOUNT = "Amount";

  private final DoubleFormatter _doubleFormatter;

  /* package */ MultipleCurrencyAmountFormatter(final DoubleFormatter doubleFormatter) {
    super(MultipleCurrencyAmount.class);
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<MultipleCurrencyAmount>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final MultipleCurrencyAmount value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(final MultipleCurrencyAmount value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "Vector (" + value.size() + ")";
  }

  private Map<String, Object> formatExpanded(final MultipleCurrencyAmount value, final ValueSpecification valueSpec) {
    final Map<String, Object> resultsMap = Maps.newHashMap();
    final CurrencyAmount[] currencyAmounts = value.getCurrencyAmounts();
    final List<List<String>> results = Lists.newArrayListWithCapacity(currencyAmounts.length);
    for (final CurrencyAmount currencyAmount : currencyAmounts) {
      final String formattedValue = _doubleFormatter.formatCell(currencyAmount.getAmount(), valueSpec, null);
      final List<String> rowResults = ImmutableList.of(currencyAmount.getCurrency().getCode(), formattedValue);
      results.add(rowResults);
    }
    resultsMap.put(DATA, results);
    resultsMap.put(LABELS, ImmutableList.of(CURRENCY, AMOUNT));
    return resultsMap;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
