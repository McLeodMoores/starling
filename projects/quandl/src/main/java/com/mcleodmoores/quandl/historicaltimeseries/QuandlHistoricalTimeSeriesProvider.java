/**
 * Copyright (C) 2014 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.mcleodmoores.quandl.historicaltimeseries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.threeten.bp.LocalDate;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.jimmoores.quandl.HeaderDefinition;
import com.jimmoores.quandl.MultiDataSetRequest;
import com.jimmoores.quandl.MultiDataSetRequest.Builder;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.robustwrapper.RobustQuandlSession;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.jimmoores.quandl.MultiMetaDataRequest;
import com.jimmoores.quandl.QuandlCodeRequest;
import com.jimmoores.quandl.QuandlSession;
import com.jimmoores.quandl.Row;
import com.jimmoores.quandl.TabularResult;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.provider.historicaltimeseries.impl.AbstractHistoricalTimeSeriesProvider;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.time.LocalDateRange;

/**
 * Gets historical time series from Quandl.
 */
public class QuandlHistoricalTimeSeriesProvider extends AbstractHistoricalTimeSeriesProvider implements Lifecycle {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlHistoricalTimeSeriesProvider.class);
  /** Date column name */
  private static final String DATE_COLUMN = "Date";
  /** Maximum chunk size */
  private static final int MAX_CHUNK = 20;
  /** The session */
  private RobustQuandlSession _session;
  /** The authorisation token */
  private final String _authToken;

  /**
   * Creates an instance.
   * @param authToken The authorisation token, not null
   */
  public QuandlHistoricalTimeSeriesProvider(final String authToken) {
    ArgumentChecker.notNull(authToken, "authToken");
    _authToken = authToken;
  }

  @Override
  public void start() {
    _session = new RobustQuandlSession(QuandlSession.create(_authToken));
  }

  @Override
  public void stop() {
  }

  @Override
  public boolean isRunning() {
    return true;
  }

  @Override
  protected HistoricalTimeSeriesProviderGetResult doBulkGet(final HistoricalTimeSeriesProviderGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    final Set<ExternalIdBundle> externalIdBundles = request.getExternalIdBundles();
    final Iterator<ExternalIdBundle> iter = externalIdBundles.iterator();
    final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> results = new LinkedHashMap<>();
    while (iter.hasNext()) {
      final Set<ExternalIdBundle> chunk = new LinkedHashSet<>();
      for (int i = 0; i < MAX_CHUNK && iter.hasNext(); i++) {
        chunk.add(iter.next());
      }
      results.putAll(processChunk(chunk, request));
    }
    return new HistoricalTimeSeriesProviderGetResult(results);
  }

  private Map<ExternalIdBundle, LocalDateDoubleTimeSeries> processChunk(final Set<ExternalIdBundle> chunk,
      final HistoricalTimeSeriesProviderGetRequest request) {
    final String fieldName = request.getDataField();
    final Multimap<String, ExternalIdBundle> quandlCodes = convertChunkToQuandlCodes(chunk, request);
    final List<String> codes = new ArrayList<>(quandlCodes.keySet());
    final Map<String, HeaderDefinition> bulkMetaData = _session.getMultipleHeaderDefinition(MultiMetaDataRequest.of(codes));
    final List<QuandlCodeRequest> codeRequests = buildCodeRequests(bulkMetaData, fieldName);
    final MultiDataSetRequest multiRequest = buildRequest(codeRequests, request);
    final TabularResult dataSets = _session.getDataSets(multiRequest);
    final Map<String, LocalDateDoubleTimeSeries> timeSeriesDataSets = parseTabularResult(dataSets);
    return convertQuandlCodesToExternalIdBundles(quandlCodes, timeSeriesDataSets);
  }

  private MultiDataSetRequest buildRequest(final List<QuandlCodeRequest> codeRequests, final HistoricalTimeSeriesProviderGetRequest request) {
    final Builder builder = MultiDataSetRequest.Builder.of(codeRequests);
    final LocalDateRange dateRange = request.getDateRange();
    if (!dateRange.equals(LocalDateRange.ALL)) {
      if (!dateRange.isStartDateMinimum()) {
        builder.withStartDate(dateRange.getStartDateInclusive());
      }
      if (!dateRange.isEndDateMaximum()) {
        // bug in the local date range that means the previous day is asked for
        builder.withEndDate(dateRange.getEndDateExclusive());
      }
    }
    return builder.build();
  }

  private Map<ExternalIdBundle, LocalDateDoubleTimeSeries> convertQuandlCodesToExternalIdBundles(
      final Multimap<String, ExternalIdBundle> quandlCodes,
      final Map<String, LocalDateDoubleTimeSeries> timeSeriesDataSets) {
    final Map<ExternalIdBundle, LocalDateDoubleTimeSeries> result = new LinkedHashMap<>();
    for (final String quandlCode : timeSeriesDataSets.keySet()) {
      final LocalDateDoubleTimeSeries timeSeries = timeSeriesDataSets.get(quandlCode);
      final Collection<ExternalIdBundle> bundles = quandlCodes.get(quandlCode);
      for (final ExternalIdBundle idBundle : bundles) { // there might be more than one
        result.put(idBundle, timeSeries);
      }
    }
    return result;
  }

  private Map<String, LocalDateDoubleTimeSeries> parseTabularResult(
      final TabularResult dataSets) {
    final List<String> columnNames = dataSets.getHeaderDefinition().getColumnNames();
    final Map<String, Integer> codesToIndices = new LinkedHashMap<>();
    final Map<String, LocalDateDoubleTimeSeriesBuilder> timeSeriesBuilders = new LinkedHashMap<>();
    int i = 0;
    for (final String columnName : columnNames) {
      final String[] parts = columnName.split(" - ");
      if (parts.length == 2) {
        codesToIndices.put(parts[0].replace('.', '/'), i);
        timeSeriesBuilders.put(parts[0].replace('.', '/'), ImmutableLocalDateDoubleTimeSeries.builder());
      } else if (!columnName.equals(DATE_COLUMN)) {
        LOGGER.error("Problem parsing column header {}, cannot remove field name, skipping");
      }
      i++;
    }
    final Iterator<Row> rowIter = dataSets.iterator();
    while (rowIter.hasNext()) {
      final Row row = rowIter.next();
      final LocalDate date = row.getLocalDate(0);
      for (final Map.Entry<String, Integer> entry : codesToIndices.entrySet()) {
        final String quandlCode = entry.getKey();
        final int index = entry.getValue();
        final Double value = row.getDouble(index);
        if (value != null) {
          final LocalDateDoubleTimeSeriesBuilder seriesBuilder = timeSeriesBuilders.get(quandlCode);
          seriesBuilder.put(date, value);
        }
      }
    }
    final Map<String, LocalDateDoubleTimeSeries> timeSeriesMap = new LinkedHashMap<>();
    for (final String quandlCode : timeSeriesBuilders.keySet()) {
      final LocalDateDoubleTimeSeries timeSeries = timeSeriesBuilders.get(quandlCode).build();
      if (!timeSeries.isEmpty()) {
        timeSeriesMap.put(quandlCode, timeSeries);
        LOGGER.info("Successfully loaded Quandl data {}, containing {} data points from {} to {}",
            quandlCode, timeSeries.size(), timeSeries.getEarliestTime(), timeSeries.getLatestTime());
      } else {
        LOGGER.info("No data Quandl data available for {}", quandlCode);
      }
    }
    return timeSeriesMap;
  }

  private List<QuandlCodeRequest> buildCodeRequests(
      final Map<String, HeaderDefinition> bulkMetaData, final String field) {
    final List<QuandlCodeRequest> codeRequests = new ArrayList<>();
    for (final Map.Entry<String, HeaderDefinition> entry : bulkMetaData.entrySet()) {
      final String quandlCode = entry.getKey();
      final HeaderDefinition headerDefinition = entry.getValue();
      try {
        final int columnIndex = headerDefinition.columnIndex(field);
        codeRequests.add(QuandlCodeRequest.singleColumn(quandlCode, columnIndex));
      } catch (final IllegalArgumentException iae) {
        LOGGER.info("No field {} available for Quandl code {}, skipping");
        // column doesn't exist in this data set.
      }
    }
    return codeRequests;
  }

  private Multimap<String, ExternalIdBundle> convertChunkToQuandlCodes(final Set<ExternalIdBundle> idBundles,
      final HistoricalTimeSeriesProviderGetRequest request) {
    final Multimap<String, ExternalIdBundle> map = LinkedHashMultimap.create(MAX_CHUNK, 1);
    for (final ExternalIdBundle bundle : idBundles) {
      final ExternalId quandlCodeId = bundle.getExternalId(QuandlConstants.QUANDL_CODE);
      if (quandlCodeId != null) {
        map.put(quandlCodeId.getValue(), bundle);
      } else {
        LOGGER.info("Couldn't find a Quandl code for bundle {}, skipping", bundle.toString());
      }
    }
    return map;
  }

}
