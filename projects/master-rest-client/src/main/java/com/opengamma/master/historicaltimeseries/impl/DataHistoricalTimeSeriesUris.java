/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import com.opengamma.master.AbstractDataDocumentUris;

/**
 * RESTful URIs for HistoricalTimeSeries.
 */
public class DataHistoricalTimeSeriesUris extends AbstractDataDocumentUris {

  @Override
  protected String getResourceName() {
    return "infos";
  }

}
