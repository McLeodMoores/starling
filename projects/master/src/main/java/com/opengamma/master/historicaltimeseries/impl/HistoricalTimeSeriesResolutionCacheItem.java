/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents a cache item for storing {@link HistoricalTimeSeriesResolutionResult} instances for the same {@link ExternalId} with differing validity periods.
 */
public class HistoricalTimeSeriesResolutionCacheItem {

  private final ExternalId _externalId;
  private final AtomicBoolean _allInvalid = new AtomicBoolean();
  private final Set<LocalDate> _invalidDates = Collections.newSetFromMap(new ConcurrentHashMap<LocalDate, Boolean>());
  private final ConcurrentMap<ExternalIdWithDates, HistoricalTimeSeriesResolutionResult> _results = new ConcurrentHashMap<>();

  /**
   * @param externalId
   *          the identifier
   */
  public HistoricalTimeSeriesResolutionCacheItem(final ExternalId externalId) {
    _externalId = externalId;
  }

  // -------------------------------------------------------------------------
  /**
   * @param validityDate
   *          the validity date
   * @return true if the date is invalid
   */
  public boolean isInvalid(final LocalDate validityDate) {
    if (_allInvalid.get()) {
      return true;
    }
    if (validityDate == null) {
      return false;
    }
    return _invalidDates.contains(validityDate);
  }

  /**
   * @param validityDate
   *          the validity date
   */
  public void putInvalid(final LocalDate validityDate) {
    if (validityDate == null) {
      if (_results.size() != 0) {
        throw new OpenGammaRuntimeException(
            "Already have " + _results.size() + " valid results for " + _externalId + " but attempted to mark every date as invalid");
      }
      _allInvalid.set(true);
      _invalidDates.clear();
    } else {
      _invalidDates.add(validityDate);
    }
  }

  // -------------------------------------------------------------------------
  /**
   * @param validityDate
   *          the validity date
   * @return the resolution result
   */
  public HistoricalTimeSeriesResolutionResult get(final LocalDate validityDate) {
    for (final Map.Entry<ExternalIdWithDates, HistoricalTimeSeriesResolutionResult> result : _results.entrySet()) {
      if (result.getKey().isValidOn(validityDate)) {
        return result.getValue();
      }
    }
    return null;
  }

  /**
   * @param externalIdWithDates
   *          the identifiers, not null
   * @param result
   *          the resolution result, not ull
   */
  public void put(final ExternalIdWithDates externalIdWithDates, final HistoricalTimeSeriesResolutionResult result) {
    ArgumentChecker.notNull(externalIdWithDates, "externalIdWithDates");
    ArgumentChecker.notNull(result, "result");
    if (!_externalId.equals(externalIdWithDates.getExternalId())) {
      throw new IllegalArgumentException(externalIdWithDates + " is not compatible with " + _externalId);
    }
    if (!result.getHistoricalTimeSeriesInfo().getExternalIdBundle().contains(externalIdWithDates)) {
      throw new IllegalArgumentException("Result " + result + " does not contain " + externalIdWithDates);
    }
    _results.putIfAbsent(externalIdWithDates, result);
  }

}
