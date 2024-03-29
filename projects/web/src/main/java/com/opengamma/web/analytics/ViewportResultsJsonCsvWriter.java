/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static com.opengamma.web.analytics.formatting.DataType.UNKNOWN;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogWithContext;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogLevel;
import com.opengamma.web.analytics.formatting.DataType;
import com.opengamma.web.analytics.formatting.ResultsFormatter;
import com.opengamma.web.analytics.formatting.TypeFormatter;
import com.opengamma.web.server.conversion.DoubleValueOptionalDecimalPlaceFormatter;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Creates a JSON/CSV object from an instance of {@link ViewportResults}.
 */
public class ViewportResultsJsonCsvWriter {

  private static final String VERSION = "version";
  private static final String VALUE = "v";
  private static final String HISTORY = "h";
  private static final String TYPE = "t";
  private static final String DATA = "data";
  private static final String ERROR = "error";
  private static final String CALCULATION_DURATION = "calculationDuration";
  private static final String LOG_LEVEL = "logLevel";
  private static final String LOG_OUTPUT = "logOutput";
  private static final String EXCEPTION_CLASS = "exceptionClass";
  private static final String EXCEPTION_MESSAGE = "exceptionMessage";
  private static final String EXCEPTION_STACK_TRACE = "exceptionStackTrace";
  private static final String EVENTS = "events";
  private static final String LEVEL = "level";
  private static final String MESSAGE = "message";
  private static final String FUNCTION_NAME = "functionName";
  private static final String TARGET = "target";

  private final ResultsFormatter _formatter;
  private final DoubleValueOptionalDecimalPlaceFormatter _durationFormatter = new DoubleValueOptionalDecimalPlaceFormatter();

  public ViewportResultsJsonCsvWriter(final ResultsFormatter formatter) {
    _formatter = formatter;
  }

  public String getCsv(final ViewportResults viewportResults) {
    final GridColumnGroups columnGroups = viewportResults.getColumns();
    final String[] header1 = new String[columnGroups.getGroups().size()];
    final String[] header2 = new String[columnGroups.getColumnCount()];

    int index = 0;
    for (final GridColumnGroup gridColumnGroup : columnGroups.getGroups()) {
      header1[index++] = gridColumnGroup.getName();
    }

    final List<GridColumn> columns = columnGroups.getColumns();
    index = 0;
    for (final GridColumn gridColumn : columns) {
      header2[index++] = gridColumn.getHeader();
    }

    final StringWriter stringWriter = new StringWriter();
    @SuppressWarnings("resource")
    final
    CSVWriter csvWriter = new CSVWriter(stringWriter);

    csvWriter.writeNext(header1);
    csvWriter.writeNext(header2);

    final List<ResultsCell> viewportCells = viewportResults.getResults();
    final Iterable<List<ResultsCell>> results = Iterables.partition(viewportCells, columnGroups.getColumnCount());
    for (final List<ResultsCell> row : results) {
      final String[] rowArray = new String[row.size()];
      int col = 0;
      for (final ResultsCell cell : row) {
        final Object cellValue = cell.getValue();
        if (cellValue instanceof RowTarget) {
          rowArray[col++] = ((RowTarget) cellValue).getName();
          continue;
        }

        final ValueSpecification cellValueSpec = cell.getValueSpecification();
        final Object formattedValue = _formatter.format(cellValue, cellValueSpec, cell.getFormat(), cell.getInlineKey());
        if (formattedValue instanceof String) {
          rowArray[col++] = (String) formattedValue;
        } else {
          rowArray[col++] = formattedValue.toString();
        }
      }
      csvWriter.writeNext(rowArray);
    }
    return stringWriter.toString();
  }

  // TODO use a Freemarker template - will that perform well enough?
  public String getJson(final ViewportResults viewportResults) {
    final List<ResultsCell> viewportCells = viewportResults.getResults();
    final List<Object> results = Lists.newArrayListWithCapacity(viewportCells.size());
    for (final ResultsCell cell : viewportCells) {
      final Object cellValue = cell.getValue();
      final ValueSpecification cellValueSpec = cell.getValueSpecification();
      final Object formattedValue = _formatter.format(cellValue, cellValueSpec, cell.getFormat(), cell.getInlineKey());
      final Collection<Object> history = cell.getHistory();
      final Class<?> columnType = cell.getType();
      final DataType columnFormat = _formatter.getDataType(columnType);
      final Map<String, Object> valueMap = Maps.newHashMap();
      final AggregatedExecutionLog executionLog = cell.getExecutionLog();
      final LogLevel logLevel = maxLogLevel(executionLog);

      valueMap.put(VALUE, formattedValue);
      if (columnFormat == UNKNOWN) {
        // if the the column type isn't known then send the type with the value
        valueMap.put(TYPE, _formatter.getDataTypeForValue(cellValue, cellValueSpec).name());
      }
      if (history != null) {
        final List<Object> formattedHistoy = formatHistory(cellValueSpec, cell.getInlineKey(), history);
        if (formattedHistoy != null) {
          valueMap.put(HISTORY, formattedHistoy);
        }
      }
      if (cell.isError() || isError(formattedValue)) {
        valueMap.put(ERROR, true);
      }
      if (logLevel != null) {
        valueMap.put(LOG_LEVEL, logLevel);
      }
      if (hasLogOutput(executionLog)) {
        valueMap.put(LOG_OUTPUT, formatLogOutput(executionLog));
      }
      results.add(valueMap);
    }
    final String duration = _durationFormatter.format(new BigDecimal(viewportResults.getCalculationDuration().toMillis()));
    final ImmutableMap<String, Object> resultsMap = ImmutableMap.of(VERSION, viewportResults.getVersion(),
        CALCULATION_DURATION, duration,
        DATA, results);
    return new JSONObject(resultsMap).toString();
  }

  private static boolean isError(final Object value) {
    return value instanceof MissingValue;
  }

  private static LogLevel maxLogLevel(final AggregatedExecutionLog log) {
    if (log == null) {
      return null;
    }
    final EnumSet<LogLevel> logLevels = log.getLogLevels();
    if (logLevels.isEmpty()) {
      return null;
    }
    final List<LogLevel> logLevelList = Lists.newArrayList(logLevels);
    Collections.sort(logLevelList);
    return logLevelList.get(logLevelList.size() - 1);
  }

  private static boolean hasLogOutput(final AggregatedExecutionLog aggregatedLog) {
    return aggregatedLog != null && aggregatedLog.getLogs() != null && !aggregatedLog.getLogs().isEmpty();
  }

  private static List<Map<String, Object>> formatLogOutput(final AggregatedExecutionLog aggregatedLog) {
    final List<Map<String, Object>> output = Lists.newArrayList();
    for (final ExecutionLogWithContext logWithContext : aggregatedLog.getLogs()) {
      final Map<String, Object> logMap = Maps.newHashMap();
      final ComputationTargetSpecification target = logWithContext.getTargetSpecification();
      logMap.put(FUNCTION_NAME, logWithContext.getFunctionName());
      logMap.put(TARGET, target.getType() + " " + target.getUniqueId());
      final ExecutionLog log = logWithContext.getExecutionLog();
      if (log.hasException()) {
        logMap.put(EXCEPTION_CLASS, log.getExceptionClass());
        logMap.put(EXCEPTION_MESSAGE, log.getExceptionMessage());
        logMap.put(EXCEPTION_STACK_TRACE, log.getExceptionStackTrace());
      }
      final List<Map<String, Object>> events = Lists.newArrayList();
      logMap.put(EVENTS, events);
      if (log.getEvents() != null) {
        for (final LogEvent logEvent : log.getEvents()) {
          events.add(ImmutableMap.<String, Object>of(LEVEL, logEvent.getLevel(), MESSAGE, logEvent.getMessage()));
        }
      }
      output.add(logMap);
    }
    return output;
  }

  /**
   * Formats history data as a JSON list.
   *
   * @param cellValueSpec The cell's value specification, can be null
   * @param inlineKey If the cell contains a single value derived from an inlined value, this object is the key
   * to obtain the cell's value from the underlying value.
   *@param history The history values, not null  @return The formatted history
   */
  private List<Object> formatHistory(final ValueSpecification cellValueSpec, final Object inlineKey, final Collection<Object> history) {
    final List<Object> formattedHistory = Lists.newArrayListWithCapacity(history.size());
    for (final Object historyValue : history) {
      final Object formattedValue = _formatter.format(historyValue, cellValueSpec, TypeFormatter.Format.HISTORY, inlineKey);
      if (formattedValue != ResultsFormatter.VALUE_UNAVAILABLE) {
        formattedHistory.add(formattedValue);
      }
    }
    // it's possible for an object to have history but not to have any formatted history if it's inlined. some inline
    // keys won't have a value for some inlined results. in that case the cell will have empty history although
    // there is history for the underlying value
    if (formattedHistory.isEmpty()) {
      return null;
    }
    return formattedHistory;
  }
}
