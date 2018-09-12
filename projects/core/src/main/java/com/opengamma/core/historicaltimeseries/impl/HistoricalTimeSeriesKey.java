/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalIdBundle;

/**
 * Key to represent time-series data in a hash-map or cache.
 */
/* package */final class HistoricalTimeSeriesKey implements Serializable {

  private static final long serialVersionUID = 1L;

  private final ExternalIdBundle _externalIdBundle;
  private final LocalDate _currentDate;
  private final String _dataSource;
  private final String _dataProvider;
  private final String _dataField;
  private final String _configName;

  /* package */ HistoricalTimeSeriesKey(final String configName, final LocalDate currentDate, final ExternalIdBundle bundle,
      final String dataSource, final String dataProvider, final String field) {
    _externalIdBundle = bundle;
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _dataField = field;
    _currentDate = currentDate;
    _configName = configName;
  }

  public ExternalIdBundle getExternalIdBundle() {
    return _externalIdBundle;
  }

  public LocalDate getCurrentDate() {
    return _currentDate;
  }

  public String getDataSource() {
    return _dataSource;
  }

  public String getDataProvider() {
    return _dataProvider;
  }

  public String getDataField() {
    return _dataField;
  }

  public String getConfigName() {
    return _configName;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof HistoricalTimeSeriesKey) {
      final HistoricalTimeSeriesKey other = (HistoricalTimeSeriesKey) object;
      return ObjectUtils.equals(_externalIdBundle, other._externalIdBundle)
          && ObjectUtils.equals(_currentDate, other._currentDate)
          && ObjectUtils.equals(_dataProvider, other._dataProvider)
          && ObjectUtils.equals(_dataSource, other._dataSource)
          && ObjectUtils.equals(_dataField, other._dataField)
          && ObjectUtils.equals(_configName, other._configName);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(_externalIdBundle)
           ^ ObjectUtils.hashCode(_currentDate)
           ^ ObjectUtils.hashCode(_dataProvider)
           ^ ObjectUtils.hashCode(_dataSource)
           ^ ObjectUtils.hashCode(_dataField)
           ^ ObjectUtils.hashCode(_configName);
  }

}
