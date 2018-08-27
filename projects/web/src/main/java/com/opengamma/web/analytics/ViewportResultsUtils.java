/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

/**
 * This class is for debugging.
 */
/* package */ class ViewportResultsUtils {

  /* package */ static String dumpResults(final ViewportResults viewportResults, final int startRow, final int rowCount) {
    final ViewportDefinition viewportDefinition = viewportResults.getViewportDefinition();
    if (!(viewportDefinition instanceof RectangularViewportDefinition)) {
      return "dumpResults only implemented for RectangularViewportDefinition";
    }
    final RectangularViewportDefinition def = (RectangularViewportDefinition) viewportDefinition;
    final int colCount = def.getColumns().size();
    final int startIndex = startRow * colCount;
    final int endIndex = startIndex + rowCount * colCount;
    final List<ResultsCell> results = viewportResults.getResults().subList(startIndex, endIndex);
    final StringBuilder sb = new StringBuilder();
    for (final ResultsCell cell : results) {
      final Object value = cell.getValue();
      if (value instanceof RowTarget) {
        sb.append("\n").append(((RowTarget) value).getName());
      } else {
        sb.append(value);
      }
      sb.append(", ");
    }
    sb.setLength(sb.length() - 2);
    return sb.toString();
  }

}
