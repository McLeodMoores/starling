/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.historicaltimeseries;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.FieldMappingHistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustment;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesFieldAdjustmentMap;

/**
 * A historical time series resolver for Quandl data that allows multiple mappings from Quandl data fields to
 * a single OpenGamma field (e.g. {@link com.opengamma.core.value.MarketDataRequirementNames#MARKET_VALUE} to
 * ({@link com.mcleodmoores.quandl.QuandlConstants#RATE_FIELD_NAME}, {@link com.mcleodmoores.quandl.QuandlConstants#VALUE_FIELD_NAME}
 * and {@link com.mcleodmoores.quandl.QuandlConstants#LAST_FIELD_NAME}).
 */
public class QuandlFieldMappingHistoricalTimeSeriesResolver extends DefaultHistoricalTimeSeriesResolver {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(FieldMappingHistoricalTimeSeriesResolver.class);
  /** The field adjustment maps */
  private final Map<String, HistoricalTimeSeriesFieldAdjustmentMap> _fieldMaps;

  /**
   * Creates an instance.
   * @param fieldMaps  the field adjustment maps, not null
   * @param selector  the selector, not null
   * @param master  the master, not null
   */
  public QuandlFieldMappingHistoricalTimeSeriesResolver(final Collection<HistoricalTimeSeriesFieldAdjustmentMap> fieldMaps,
      final HistoricalTimeSeriesSelector selector, final HistoricalTimeSeriesMaster master) {
    super(selector, master);
    ArgumentChecker.notNull(selector, "selector");
    ArgumentChecker.notNull(fieldMaps, "fieldMaps");
    _fieldMaps = getFieldMaps(fieldMaps);
  }

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource,
      final String dataProvider, final String dataField, final String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    // Apply any field mappings
    final Collection<String> dataFields;
    final String mappedDataSource, mappedDataProvider;
    final Map<String, HistoricalTimeSeriesFieldAdjustment> fieldMappings = getFieldAdjustments(dataSource, dataField);
    if (fieldMappings.size() == 1) {
      // Optimisation - might as well restrict the search results
      final Map.Entry<String, HistoricalTimeSeriesFieldAdjustment> fieldMappingEntry = Iterables.getOnlyElement(fieldMappings.entrySet());
      mappedDataSource = fieldMappingEntry.getKey();
      mappedDataProvider = fieldMappingEntry.getValue().getUnderlyingDataProvider();
      dataFields = fieldMappingEntry.getValue().getUnderlyingDataFields();
    } else if (fieldMappings.size() > 1) {
      // Could have been mapped to multiple underlying providers/fields
      dataFields = Collections.singleton(null);
      mappedDataSource = null;
      mappedDataProvider = null;
    } else {
      dataFields = Collections.emptySet();
      mappedDataSource = null;
      mappedDataProvider = null;
    }
    for (final String field : dataFields) {
      if (identifierBundle != null) {
        final Collection<ManageableHistoricalTimeSeriesInfo> timeSeriesCandidates = search(identifierBundle, identifierValidityDate,
            mappedDataSource, mappedDataProvider, field);
        if (timeSeriesCandidates.isEmpty()) {
          // try with next data field
          continue;
        }
        if (!fieldMappings.isEmpty()) {
          final Iterator<ManageableHistoricalTimeSeriesInfo> it = timeSeriesCandidates.iterator();
          while (it.hasNext()) {
            final ManageableHistoricalTimeSeriesInfo candidate = it.next();
            final HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMappings.get(candidate.getDataSource());
            if (fieldAdjustment == null
                || ((fieldAdjustment.getUnderlyingDataProvider() != null && !fieldAdjustment.getUnderlyingDataProvider().equals(candidate.getDataProvider()))
                    || (field != null && !field.equals(candidate.getDataField())))) {
              // Incompatible
              it.remove();
            }
          }
        }
        final ManageableHistoricalTimeSeriesInfo selectedResult = select(timeSeriesCandidates, resolutionKey);
        if (selectedResult == null) {
          LOGGER.debug("Resolver failed to find any time-series for {} using {}/{}", new Object[] {identifierBundle, dataFields, resolutionKey });
          return null;
        }
        final HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMappings.get(selectedResult.getDataSource());
        final HistoricalTimeSeriesAdjuster adjuster = fieldAdjustment != null ? fieldAdjustment.getAdjuster() : null;
        return new HistoricalTimeSeriesResolutionResult(selectedResult, adjuster);
      }
      return search(dataSource, dataProvider, field);
    }
    LOGGER.debug("Resolver failed to find any time-series for {} using {}/{}", new Object[] {identifierBundle, dataFields, resolutionKey });
    return search(dataSource, dataProvider, dataField);
  }

  /**
   * Gets the field maps.
   * @return The field maps
   */
  public Collection<HistoricalTimeSeriesFieldAdjustmentMap> getFieldMaps() {
    return _fieldMaps.values();
  }

  /**
   * Checks that there is not more than one field map per data source.
   * @param fieldMaps The field maps
   * @return The field maps
   */
  private static Map<String, HistoricalTimeSeriesFieldAdjustmentMap> getFieldMaps(final Collection<HistoricalTimeSeriesFieldAdjustmentMap> fieldMaps) {
    final Map<String, HistoricalTimeSeriesFieldAdjustmentMap> result = new HashMap<>();
    for (final HistoricalTimeSeriesFieldAdjustmentMap fieldMap : fieldMaps) {
      if (result.put(fieldMap.getDataSource(), fieldMap) != null) {
        throw new IllegalArgumentException("Only one field map per data source is permitted. Found multiple for data source " + fieldMap.getDataSource());
      }
    }
    return result;
  }

  /**
   * Gets the field adjustments for a data source and field name.
   * @param dataSource The data source
   * @param dataField The data field name
   * @return The field adjustments
   */
  private Map<String, HistoricalTimeSeriesFieldAdjustment> getFieldAdjustments(final String dataSource, final String dataField) {
    if (dataSource != null) {
      final HistoricalTimeSeriesFieldAdjustmentMap fieldMap = _fieldMaps.get(dataSource);
      final HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMap != null ? fieldMap.getFieldAdjustment(dataField) : null;
      return fieldAdjustment != null ? ImmutableMap.of(dataSource, fieldAdjustment) : ImmutableMap.<String, HistoricalTimeSeriesFieldAdjustment>of();
    }
    final Map<String, HistoricalTimeSeriesFieldAdjustment> results = new HashMap<>();
    for (final Map.Entry<String, HistoricalTimeSeriesFieldAdjustmentMap> fieldMapEntry : _fieldMaps.entrySet()) {
      final HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMapEntry.getValue().getFieldAdjustment(dataField);
      if (fieldAdjustment != null) {
        results.put(fieldMapEntry.getKey(), fieldAdjustment);
      }
    }
    return results;
  }
}