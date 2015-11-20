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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.cashflow.FloatingPaymentMatrix;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * Formatter.
 */
/* package */ class FloatingPaymentMatrixFormatter extends AbstractFormatter<FloatingPaymentMatrix> {

  /* package */ static final String X_LABELS = "xLabels";
  /* package */ static final String Y_LABELS = "yLabels";
  /* package */ static final String MATRIX = "matrix";
  private final CurrencyAmountFormatter _caFormatter;

  /* package */ FloatingPaymentMatrixFormatter(final CurrencyAmountFormatter caFormatter) {
    super(FloatingPaymentMatrix.class);
    addFormatter(new Formatter<FloatingPaymentMatrix>(Format.EXPANDED) {
      @Override
      protected Map<String, Object> formatValue(final FloatingPaymentMatrix value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
    _caFormatter = caFormatter;
  }

  @Override
  public String formatCell(final FloatingPaymentMatrix value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "FloatingPaymentMatrix (" + value.getDatesAsArray().length + ")";
  }

  private Map<String, Object> formatExpanded(final FloatingPaymentMatrix value, final ValueSpecification valueSpec) {
    final Map<LocalDate, List<Pair<CurrencyAmount, String>>> values = value.getValues();
    final int columnCount = value.getMaxEntries();
    final int rowCount = values.size();

    final Map<String, Object> results = Maps.newHashMap();
    final String[] xLabels = new String[columnCount];
    final String[] yLabels = new String[rowCount];
    final String[][] matrix = new String[rowCount][columnCount];
    int row = 0;
    Arrays.fill(yLabels, StringUtils.EMPTY);
    for (final Map.Entry<LocalDate, List<Pair<CurrencyAmount, String>>> entry : values.entrySet()) {
      yLabels[row] = entry.getKey().toString();
      final List<Pair<CurrencyAmount, String>> ca = entry.getValue();
      for (int i = 0; i < columnCount; i++) {
        final StringBuilder sb = new StringBuilder(_caFormatter.formatCell(ca.get(i).getFirst(), valueSpec, null));
        sb.append(" (");
        sb.append(ca.get(i).getSecond());
        sb.append(")");
        matrix[row][i] = sb.toString();
      }
      row++;
    }
    results.put(X_LABELS, xLabels);
    results.put(Y_LABELS, yLabels);
    results.put(MATRIX, matrix);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }

}
