/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;

/**
 * The information about a historical time-series.
 * <p>
 * This is used to hold the information about a time-series in the master. The actual time-series is held separately.
 */
@PublicSPI
public interface HistoricalTimeSeriesInfo extends UniqueIdentifiable {

  /**
   * The identifiers associated with the time series.
   *
   * @return  the identifiers
   */
  ExternalIdBundleWithDates getExternalIdBundle();

  /**
   * Gets the name of this information.
   *
   * @return  the name
   */
  String getName();

  /**
   * Gets the data field of the time series e.g. last close.
   *
   * @return  the data field
   */
  String getDataField();

  /**
   * Gets the data source of the time series e.g. CMPL.
   *
   * @return  the data source
   */
  String getDataSource();

  /**
   * Gets the data provider of the time series e.g. Bloomberg.
   *
   * @return  the data provider
   */
  String getDataProvider();

  /**
   * Gets the observation time e.g. London close.
   *
   * @return  the observation time
   */
  String getObservationTime();

  /**
   * Gets the identifier of the time series data.
   *
   * @return  the identifier
   */
  ObjectId getTimeSeriesObjectId();

}
