/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collection;

import com.google.common.collect.Iterables;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;

/**
 *
 */
public class TestHistoricalTimeSeriesSelector implements HistoricalTimeSeriesSelector {

  private Collection<ManageableHistoricalTimeSeriesInfo> _lastCandidates;

  @Override
  public ManageableHistoricalTimeSeriesInfo select(final Collection<ManageableHistoricalTimeSeriesInfo> candidates, final String selectionKey) {
    _lastCandidates = candidates;
    return Iterables.getFirst(candidates, null);
  }

  /**
   * @return the last candidates
   */
  public Collection<ManageableHistoricalTimeSeriesInfo> getLastCandidates() {
    return _lastCandidates;
  }

}
