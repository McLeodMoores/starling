/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class LocalDateLabelledMatrix1DFormatter extends AbstractFormatter<LocalDateLabelledMatrix1D> {

  private static final String DATA = "data";
  private static final String LABELS = "labels";
  private static final String LABEL = "Label";
  private static final String VALUE = "Value";

  private final DoubleFormatter _doubleFormatter;

  /* package */ LocalDateLabelledMatrix1DFormatter(final DoubleFormatter doubleFormatter) {
    super(LocalDateLabelledMatrix1D.class);
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<LocalDateLabelledMatrix1D>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final LocalDateLabelledMatrix1D value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
    addFormatter(new Formatter<LocalDateLabelledMatrix1D>(Format.HISTORY) {
      @Override
      protected Object formatValue(final LocalDateLabelledMatrix1D value, final ValueSpecification valueSpec, final Object inlineKey) {
        if (inlineKey == null) {
          return ResultsFormatter.VALUE_UNAVAILABLE;
        } else {
          return formatInline(value, valueSpec, Format.HISTORY, inlineKey);
        }
      }
    });
  }

  @Override
  public Object formatCell(final LocalDateLabelledMatrix1D value, final ValueSpecification valueSpec, final Object inlineKey) {
    if (inlineKey == null) {
      return "Vector (" + value.getKeys().length + ")";
    } else {
      return formatInline(value, valueSpec, Format.CELL, inlineKey);
    }
  }

  private Map<String, Object> formatExpanded(final LocalDateLabelledMatrix1D value, final ValueSpecification valueSpec) {
    final Map<String, Object> resultsMap = Maps.newHashMap();
    final int length = value.getKeys().length;
    final List<List<String>> results = Lists.newArrayListWithCapacity(length);
    for (int i = 0; i < length; i++) {
      final String label = value.getLabels()[i].toString();
      final String formattedValue = _doubleFormatter.formatCell(value.getValues()[i], valueSpec, null);
      final List<String> rowResults = ImmutableList.of(label, formattedValue);
      results.add(rowResults);
    }
    resultsMap.put(DATA, results);
    final String labelsTitle = value.getLabelsTitle() != null ? value.getLabelsTitle() : LABEL;
    final String valuesTitle = value.getValuesTitle() != null ? value.getValuesTitle() : VALUE;
    resultsMap.put(LABELS, ImmutableList.of(labelsTitle, valuesTitle));
    return resultsMap;
  }

  private Object formatInline(final LocalDateLabelledMatrix1D matrix,
                              final ValueSpecification valueSpec,
                              final Format format,
                              final Object inlineKey) {
    // if there are matrices of different lengths on different rows then the shorter ones will be missing values for
    // the last columns
    final LocalDate dateKey = (LocalDate) inlineKey;
    int index = 0;
    for (final LocalDate localDate : matrix.getKeys()) {
      if (dateKey.equals(localDate)) {
        return _doubleFormatter.format(matrix.getValues()[index], valueSpec, format, inlineKey);
      }
      index++;
    }
    return ResultsFormatter.VALUE_UNAVAILABLE;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
