/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collections;
import java.util.List;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents an historical time-series field adjustment.
 */
public class HistoricalTimeSeriesFieldAdjustment {

  private final String _underlyingDataProvider;
  private final List<String> _underlyingDataFields;
  private final HistoricalTimeSeriesAdjuster _adjuster;
  
  /**
   * @deprecated use constructor that takes a list of possible underlying fields
   * Create a HTS field adjustment
   * @param underlyingDataProvider  the original data provider
   * @param underlyingDataField  the field name of the original data provider
   * @param adjuster  the adjuster
   */
  @Deprecated
  public HistoricalTimeSeriesFieldAdjustment(String underlyingDataProvider, String underlyingDataField, HistoricalTimeSeriesAdjuster adjuster) {
    ArgumentChecker.notNull(underlyingDataField, "underlyingDataField");
    _underlyingDataProvider = underlyingDataProvider;
    _underlyingDataFields = Collections.singletonList(underlyingDataField);
    _adjuster = adjuster;
  }
  
  /**
   * Create a HTS field adjustment
   * @param underlyingDataProvider  the original data provider
   * @param underlyingDataFields  the list of possible field names of the original data provider, in order of likelihood
   * @param adjuster  the adjuster
   */
  public HistoricalTimeSeriesFieldAdjustment(String underlyingDataProvider, List<String> underlyingDataFields, HistoricalTimeSeriesAdjuster adjuster) {
    ArgumentChecker.notNull(underlyingDataFields, "underlyingDataFields");
    _underlyingDataProvider = underlyingDataProvider;
    _underlyingDataFields = underlyingDataFields;
    _adjuster = adjuster;
  }
  
  /**
   * Gets the underlying data provider name.
   * 
   * @return the underlying data provider name, null for any
   */
  public String getUnderlyingDataProvider() {
    return _underlyingDataProvider;
  }

  /**
   * @deprecated use getUnderlyingDataFieldList()
   * Gets the underlying data field name.
   * @return the underlying data field name, not null
   */
  @Deprecated
  public String getUnderlyingDataField() {
    return _underlyingDataFields.get(0);
  }
  
  /**
   * Gets a list of possible underlying data fields to try, in order of likelihood.
   * @return the list of underlying data field names, not null
   */
  public List<String> getUnderlyingDataFields() {
    return _underlyingDataFields;
  }

  /**
   * Gets the adjuster to apply.
   * 
   * @return the adjuster to apply, not null
   */
  public HistoricalTimeSeriesAdjuster getAdjuster() {
    return _adjuster;
  }
    
}
