/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;

/**
 * Historical time series resolver that can expose synthetic data fields by mapping these onto a real, underlying data
 * field together with an optional adjuster.
 */
public class FieldMappingHistoricalTimeSeriesResolver extends DefaultHistoricalTimeSeriesResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(FieldMappingHistoricalTimeSeriesResolver.class);

  private final Map<String, HistoricalTimeSeriesFieldAdjustmentMap> _fieldMaps;

  public FieldMappingHistoricalTimeSeriesResolver(final Collection<HistoricalTimeSeriesFieldAdjustmentMap> fieldMaps, final HistoricalTimeSeriesSelector selector, final HistoricalTimeSeriesMaster master) {
    super(selector, master);
    _fieldMaps = getFieldMaps(fieldMaps);
  }

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String dataSource, final String dataProvider, final String dataField, final String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    // Apply any field mappings
    final Map<String, HistoricalTimeSeriesFieldAdjustment> fieldMappings = getFieldAdjustments(dataSource, dataField);
    final String multiDataSource, multiDataProvider, multiDataField;
    if (fieldMappings.size() == 1) {
      // Optimisation - might as well restrict the search results
      final Map.Entry<String, HistoricalTimeSeriesFieldAdjustment> fieldMappingEntry = Iterables.getOnlyElement(fieldMappings.entrySet());
      multiDataSource = fieldMappingEntry.getKey();
      multiDataProvider = fieldMappingEntry.getValue().getUnderlyingDataProvider();
      multiDataField = fieldMappingEntry.getValue().getUnderlyingDataField();
    } else if (fieldMappings.size() > 1) {
      // Could have been mapped to multiple underlying providers/fields
      multiDataField = null;
      multiDataProvider = null;
      multiDataSource = dataSource;
    } else {
      multiDataField = dataField;
      multiDataProvider = dataProvider;
      multiDataSource = dataSource;
    }
    if (identifierBundle != null) {
      final Collection<ManageableHistoricalTimeSeriesInfo> timeSeriesCandidates =
          search(identifierBundle, identifierValidityDate, multiDataSource, multiDataProvider, multiDataField);
      if (!fieldMappings.isEmpty()) {
        final Iterator<ManageableHistoricalTimeSeriesInfo> it = timeSeriesCandidates.iterator();
        while (it.hasNext()) {
          final ManageableHistoricalTimeSeriesInfo candidate = it.next();
          final HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMappings.get(candidate.getDataSource());
          if (fieldAdjustment == null ||
              fieldAdjustment.getUnderlyingDataProvider() != null && !fieldAdjustment.getUnderlyingDataProvider().equals(candidate.getDataProvider())
              || !fieldAdjustment.getUnderlyingDataField().equals(candidate.getDataField())) {
            // Incompatible
            it.remove();
          }
        }
      }
      final ManageableHistoricalTimeSeriesInfo selectedResult = select(timeSeriesCandidates, resolutionKey);
      if (selectedResult == null) {
        LOGGER.debug("Resolver failed to find any time-series for {} using {}/{}", new Object[] {identifierBundle, multiDataField, resolutionKey });
        return null;
      }
      final HistoricalTimeSeriesFieldAdjustment fieldAdjustment = fieldMappings.get(selectedResult.getDataSource());
      final HistoricalTimeSeriesAdjuster adjuster = fieldAdjustment != null ? fieldAdjustment.getAdjuster() : null;
      return new HistoricalTimeSeriesResolutionResult(selectedResult, adjuster);
    } else {
      return search(multiDataSource, multiDataProvider, multiDataField);
    }
  }

  public Collection<HistoricalTimeSeriesFieldAdjustmentMap> getFieldMaps() {
    return _fieldMaps.values();
  }

  //-------------------------------------------------------------------------
  private Map<String, HistoricalTimeSeriesFieldAdjustmentMap> getFieldMaps(final Collection<HistoricalTimeSeriesFieldAdjustmentMap> fieldMaps) {
    final Map<String, HistoricalTimeSeriesFieldAdjustmentMap> result = new HashMap<>();
    for (final HistoricalTimeSeriesFieldAdjustmentMap fieldMap : fieldMaps) {
      if (result.put(fieldMap.getDataSource(), fieldMap) != null) {
        throw new IllegalArgumentException("Only one field map per data source is permitted. Found multiple for data source " + fieldMap.getDataSource());
      }
    }
    return result;
  }

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
