/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.Comparator;

import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;

/**
 * Comparator for ManagableHistoricalTimeSeriesInfo that excludes UniqueIds and TS ObjectIds.
 */
public class ManageableHistoricalTimeSeriesInfoComparator implements Comparator<ManageableHistoricalTimeSeriesInfo> {

  @Override
  public int compare(final ManageableHistoricalTimeSeriesInfo first, final ManageableHistoricalTimeSeriesInfo second) {
    final int name = first.getName().compareTo(second.getName());
    if (name != 0) { return name; }
    final int dataField = first.getDataField().compareTo(second.getDataField());
    if (dataField != 0) { return dataField; }
    final int dataSource = first.getDataSource().compareTo(second.getDataSource());
    if (dataSource != 0) { return dataSource; }
    final int dataProvider = first.getDataProvider().compareTo(second.getDataProvider());
    if (dataProvider != 0) { return dataProvider; }
    final int observationTime = first.getObservationTime().compareTo(second.getObservationTime());
    if (observationTime != 0) { return observationTime; }
    final int externalIdBundle = first.getExternalIdBundle().compareTo(second.getExternalIdBundle());
    return externalIdBundle;
  }

}
