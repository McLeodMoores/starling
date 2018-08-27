/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.core.security.Security;
import com.opengamma.financial.security.lookup.SecurityAttribute;
import com.opengamma.financial.security.lookup.SecurityAttributeMapper;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 *
 */
/* package */ class BlotterColumnRenderer implements GridColumn.CellRenderer {

  /** Maps the shared blotter columns to the fields of each different security type. */
  private final SecurityAttributeMapper _columnMappings;
  /** The column whose values are handled by this renderer. */
  private final SecurityAttribute _column;
  /** The rows in the grid. */
  private final List<PortfolioGridRow> _rows;

  public BlotterColumnRenderer(final SecurityAttribute column,
                               final SecurityAttributeMapper columnMappings,
                               final List<PortfolioGridRow> rows) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(columnMappings, "blotterColumnMappings");
    ArgumentChecker.notNull(rows, "rows");
    _rows = rows;
    _columnMappings = columnMappings;
    _column = column;
  }

  @Override
  public ResultsCell getResults(final int rowIndex,
                                final TypeFormatter.Format format,
                                final ResultsCache cache,
                                final Class<?> columnType,
                                final Object inlineKey) {
    final PortfolioGridRow row = _rows.get(rowIndex);
    final UniqueId securityId = row.getSecurityId();
    Security security;
    ResultsCache.Result result;
    boolean updated;
    if (securityId != null) {
      result = cache.getEntity(securityId.getObjectId());
      security = (Security) result.getValue();
      updated = result.isUpdated();
    } else {
      security = null;
      updated = false;
    }
    return ResultsCell.forStaticValue(_columnMappings.valueFor(_column, security), columnType, format, updated);
  }
}
