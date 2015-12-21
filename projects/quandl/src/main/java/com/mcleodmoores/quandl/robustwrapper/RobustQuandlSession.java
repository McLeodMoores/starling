package com.mcleodmoores.quandl.robustwrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.jimmoores.quandl.DataSetRequest;
import com.jimmoores.quandl.DataSetRequest.Builder;
import com.jimmoores.quandl.HeaderDefinition;
import com.jimmoores.quandl.MetaDataRequest;
import com.jimmoores.quandl.MetaDataResult;
import com.jimmoores.quandl.MultiDataSetRequest;
import com.jimmoores.quandl.MultiMetaDataRequest;
import com.jimmoores.quandl.QuandlCodeRequest;
import com.jimmoores.quandl.QuandlSession;
import com.jimmoores.quandl.Row;
import com.jimmoores.quandl.SearchRequest;
import com.jimmoores.quandl.SearchResult;
import com.jimmoores.quandl.SortOrder;
import com.jimmoores.quandl.TabularResult;
import com.jimmoores.quandl.util.QuandlRuntimeException;
import com.jimmoores.quandl.util.QuandlTooManyRequestsException;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;

/**
 *
 */
public class RobustQuandlSession {
  private static final Logger s_logger = LoggerFactory.getLogger(RobustQuandlSession.class);
  private final QuandlSession _session;

  private static final long BACKOFF_PERIOD_MILLIS = 60 * 1000;

  /**
   * Public constructor.
   * Takes a QuandlSession that it wraps.
   * @param session  the underlying quandl session
   */
  public RobustQuandlSession(final QuandlSession session) {
    _session = session;
  }

  /**
   * Get a tabular data set from Quandl.
   * @param request the request object containing details of what is required
   * @return a TabularResult set
   */
  public TabularResult getDataSet(final DataSetRequest request) {
    return _session.getDataSet(request);
  }

  /**
   * Get meta data from Quandl about a particular quandlCode.
   * @param request the request object containing details of what is required
   * @return a MetaDataResult
   */
  public MetaDataResult getMetaData(final MetaDataRequest request) {
    return _session.getMetaData(request);
  }

  /**
   * Get a multiple data sets from quandl and return as single tabular result.
   * @param request the multi data set request object containing details of what is required
   * @return a single TabularResult set containing all requested results
   */
  public TabularResult getDataSets(final MultiDataSetRequest request) {
    TabularResult tabularResult = null;
    final Integer retries = 0;
    do {
      try {
        tabularResult = _session.getDataSets(request);
      } catch (final QuandlTooManyRequestsException tooManyReqs) {
        backOff(retries); // note this modifies retries.
      } catch (final QuandlRuntimeException qre) {
        return getDataSetsSlow(request);
      }
    } while (tabularResult == null);
    return tabularResult;
  }

  private TabularResult getDataSetsSlow(final MultiDataSetRequest request) {
    final List<QuandlCodeRequest> quandlCodeRequests = request.getQuandlCodeRequests();
    final Map<QuandlCodeRequest, TabularResult> results = new LinkedHashMap<>();
    for (final QuandlCodeRequest quandlCodeRequest : quandlCodeRequests) {
      final Builder builder = DataSetRequest.Builder.of(quandlCodeRequest.getQuandlCode());
      if (quandlCodeRequest.isSingleColumnRequest()) {
        builder.withColumn(quandlCodeRequest.getColumnNumber());
      }
      if (request.getEndDate() != null) {
        builder.withEndDate(request.getEndDate());
      }
      if (request.getStartDate() != null) {
        builder.withStartDate(request.getStartDate());
      }
      if (request.getFrequency() != null) {
        builder.withFrequency(request.getFrequency());
      }
      if (request.getMaxRows() != null) {
        builder.withMaxRows(request.getMaxRows());
      }
      if (request.getSortOrder() != null) {
        builder.withSortOrder(request.getSortOrder());
      }
      if (request.getTransform() != null) {
        builder.withTransform(request.getTransform());
      }
      final DataSetRequest dataSetRequest = builder.build();
      TabularResult tabularResult = null;
      final int maxRetries = 10;
      int count = 0;
      final Integer retries = 0;
      do {
        try {
          tabularResult = _session.getDataSet(dataSetRequest);
        } catch (final QuandlTooManyRequestsException tooManyReqs) {
          backOff(retries);
          count++;
        } catch (final QuandlRuntimeException qre) {
          count++;
          s_logger.error("Can't process request for {}, giving up and skipping.  Full request is {}", quandlCodeRequest.getQuandlCode(), dataSetRequest);
          if (count > maxRetries) {
            s_logger.error("Problem getting data from Quandl for {}. Full request is {}", quandlCodeRequest.getQuandlCode(), dataSetRequest);
            break;
          }
          continue;
        }
      } while (tabularResult == null);
      if (count > maxRetries) {
        s_logger.error("Problem getting data from Quandl for {}. Full request is {}", quandlCodeRequest.getQuandlCode(), dataSetRequest);
        break;
      }
      results.put(quandlCodeRequest, tabularResult);
    }
    return mergeTables(results, request.getSortOrder());
  }

  private TabularResult mergeTables(final Map<QuandlCodeRequest, TabularResult> results, final SortOrder sortOrder) {
    int resultTableWidth = 1; // the date!
    final Map<QuandlCodeRequest, Integer> initialOffset = new HashMap<>();
    final List<String> columnNames = new ArrayList<String>();
    columnNames.add("Date");
    for (final Map.Entry<QuandlCodeRequest, TabularResult> entry : results.entrySet()) {
      final QuandlCodeRequest codeRequest = entry.getKey();
      final TabularResult table = entry.getValue();
      if (!initialOffset.containsKey(codeRequest)) {
        initialOffset.put(codeRequest, resultTableWidth); // record the offset for each table
      }
      resultTableWidth += table.getHeaderDefinition().size() - 1; // exclude the date column.
      final List<String> names = table.getHeaderDefinition().getColumnNames();
      final Iterator<String> iter = names.iterator();
      if (!iter.hasNext()) { throw new Quandl4OpenGammaRuntimeException("table has no columns, expected at least date"); }
      iter.next(); // discard date column name
      while (iter.hasNext()) {
        final String colName = iter.next();
        columnNames.add(codeRequest.getQuandlCode() + " - " + colName);
      }
    }
    final SortedMap<LocalDate, String[]> rows =
        new TreeMap<>(sortOrder == SortOrder.ASCENDING ? LocalDate.timeLineOrder() : Collections.reverseOrder(LocalDate.timeLineOrder()));
    for (final Map.Entry<QuandlCodeRequest, TabularResult> mapEntry : results.entrySet()) {
      final QuandlCodeRequest codeRequest = mapEntry.getKey();
      final TabularResult table1 = mapEntry.getValue();
      final Iterator<Row> rowIter = table1.iterator();
      while (rowIter.hasNext()) {
        final Row row = rowIter.next();
        final LocalDate date = row.getLocalDate(0);
        final String dateStr = row.getString(0);
        if (date != null) {
          String[] bigRow;
          if (rows.containsKey(date)) {
            bigRow = rows.get(date);
          } else {
            bigRow = new String[resultTableWidth];
            rows.put(date, bigRow);
          }
          for (int i = 1; i < row.size(); i++) {
            bigRow[initialOffset.get(codeRequest) + (i - 1)] = row.getString(i); // (i-1 is becuase initialOffset index already includes initial 1 offset)
          }
          bigRow[0] = dateStr; // (re)write the date string at the start of the big table.
        }
      }
    }
    final List<Row> combinedRows = new ArrayList<>();
    final HeaderDefinition headerDefinition = HeaderDefinition.of(columnNames);
    for (final Entry<LocalDate, String[]> entry : rows.entrySet()) {
      final Row row = Row.of(headerDefinition, entry.getValue());
      combinedRows.add(row);
    }
    return TabularResult.of(headerDefinition, combinedRows);
  }

  /**
   * Get meta data from Quandl about a range of quandlCodes returned as a single MetaDataResult.
   * @param request the request object containing details of what is required
   * @return a TabularResult set
   */
  public MetaDataResult getMetaData(final MultiMetaDataRequest request) {
    return _session.getMetaData(request);
  }

  /**
   * Get header definitions from Quandl about a range of quandlCodes returned as a Map of Quandl code to HeaderDefinition.
   * The keys of the map will retain the order of the request and are backed by an unmodifiable LinkedHashMap.
   * Throws a QuandlRuntimeException if it can't find a parsable quandl code or Date column in the result.
   *
   * This method handles errors from Quandl4J and splits up bulk requests if they fail.  In particular if
   * Quandl indicates there have been too many requests, it first backs off for a minute and does five
   * retries.  It will then fail.  If there are other errors, it will split the request up into a sequence
   * of single requests in an attempt to stop a 'bad apple' spoiling the whole request.
   *
   * @param request the request object containing details of what is required, not null
   * @return an unmodifiable Map of Quandl codes to MetaDataResult for each code, keys ordered according to request, not null
   */
  public Map<String, HeaderDefinition> getMultipleHeaderDefinition(final MultiMetaDataRequest request) {
    Map<String, HeaderDefinition> bulkMetaData = null;
    final Integer retries = 0;
    do {
      try {
        bulkMetaData = _session.getMultipleHeaderDefinition(request);
      } catch (final QuandlTooManyRequestsException tooManyReqs) {
        backOff(retries); // note this modifies retries.
      } catch (final QuandlRuntimeException qre) {
        s_logger.warn("There was an error performing a bulk request, falling back to single requests");
        return getMultipleHeaderDefinitionSlow(request);
      }
    } while (bulkMetaData == null);
    return bulkMetaData;
  }

  private Map<String, HeaderDefinition> getMultipleHeaderDefinitionSlow(final MultiMetaDataRequest request) {
    final Map<String, HeaderDefinition> bulkMetaData = new LinkedHashMap<>();
    for (final String quandlCode : request.getQuandlCodes()) {
      Integer retries = 0;
      MetaDataResult metaData = null;
      do {
        try {
          metaData = _session.getMetaData(MetaDataRequest.of(quandlCode));
          bulkMetaData.put(quandlCode, metaData.getHeaderDefinition());
          retries++;
        } catch (final QuandlTooManyRequestsException tooManyReqs) {
          backOff(retries); // note this modifies retries.
        } catch (final QuandlRuntimeException qre) {
          s_logger.error("There was a problem requesting metadata for {}, skipping", quandlCode);
          break;
        }
      } while (metaData == null || retries < 5);
    }
    return bulkMetaData;
  }

  private static void backOff(Integer retries) {
    try {
      if (retries++ < 5) {
        s_logger.warn("Quandl indicated too many requests have been made.  Backing off for one minute.");
        Thread.sleep(BACKOFF_PERIOD_MILLIS);
      } else {
        s_logger.warn("Quandl indicated too many requests have been made.  Giving up because tried 5 retries and limit unlikely to be reset until tomorrow.");
        throw new Quandl4OpenGammaRuntimeException("Giving up because request limit unlikely to be reset until tomorrow.");
      }
    } catch (final InterruptedException ie) { }

  }

  /**
   * Get search results from Quandl.
   * @param request the search query parameter, not null
   * @return the search result, not null
   */
  public SearchResult search(final SearchRequest request) {
    return _session.search(request);
  }
}
