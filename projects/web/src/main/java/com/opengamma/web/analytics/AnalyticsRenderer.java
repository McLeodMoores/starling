/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * A renderer for analytics.
 */
/* package */ class AnalyticsRenderer implements GridColumn.CellRenderer {

  private final ColumnSpecification _columnKey;
  private final TargetLookup _targetLookup;

  /* package */ AnalyticsRenderer(final ColumnSpecification columnKey, final TargetLookup targetLookup) {
    ArgumentChecker.notNull(columnKey, "columnKey");
    ArgumentChecker.notNull(targetLookup, "targetLookup");
    _targetLookup = targetLookup;
    _columnKey = columnKey;
  }

  @Override
  public ResultsCell getResults(final int rowIndex,
                                final TypeFormatter.Format format,
                                final ResultsCache cache,
                                final Class<?> columnType,
                                final Object inlineKey) {
    final Pair<String, ValueSpecification> cellTarget = _targetLookup.getTargetForCell(rowIndex, _columnKey);
    if (cellTarget != null) {
      final String calcConfigName = cellTarget.getFirst();
      final ValueSpecification valueSpec = cellTarget.getSecond();
      final ResultsCache.Result cacheResult = cache.getResult(calcConfigName, valueSpec, columnType);
      final Object value = cacheResult.getValue();
      return ResultsCell.forCalculatedValue(value,
                                            valueSpec,
                                            cacheResult.getHistory(),
                                            cacheResult.getAggregatedExecutionLog(),
                                            cacheResult.isUpdated(),
                                            columnType,
                                            inlineKey, format);
    } else {
      final Collection<Object> emptyHistory = cache.emptyHistory(columnType);
      return ResultsCell.empty(emptyHistory, columnType);
    }
  }
}
