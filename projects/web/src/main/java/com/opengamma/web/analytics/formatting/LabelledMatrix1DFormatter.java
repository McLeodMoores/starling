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
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Formatter.
 */
@SuppressWarnings("rawtypes")
/* package */ class LabelledMatrix1DFormatter extends AbstractFormatter<LabelledMatrix1D> {

  private static final String DATA = "data";
  private static final String LABELS = "labels";
  private static final String LABEL = "Label";
  private static final String VALUE = "Value";

  private final DoubleFormatter _doubleFormatter;

  LabelledMatrix1DFormatter(final DoubleFormatter doubleFormatter) {
    super(LabelledMatrix1D.class);
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<LabelledMatrix1D>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final LabelledMatrix1D value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(final LabelledMatrix1D value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "Vector (" + value.getKeys().length + ")";
  }

  private Map<String, Object> formatExpanded(final LabelledMatrix1D value, final ValueSpecification valueSpec) {
    final Map<String, Object> resultsMap = Maps.newHashMap();
    final int length = value.getKeys().length;
    final List<List<String>> results = Lists.newArrayListWithCapacity(length);
    for (int i = 0; i < length; i++) {
      final Object labelObject = value.getLabels()[i];
      final String label = labelObject instanceof ExternalId ? ((ExternalId) labelObject).getValue() : labelObject.toString();
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

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
