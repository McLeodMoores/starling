/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.opengamma.util.ArgumentChecker;

/**
 * A class for holding information about market data and meta-data that can distinguish between scalar
 * and historical time series data. This object is mutable.
 */
public final class MarketDataInfo {
  /** Scalar market data information */
  private final Map<MarketDataKey, ScalarMarketDataMetaData> _scalars;
  /** Time series information */
  private final Map<MarketDataKey, TimeSeriesMarketDataMetaData> _timeSeries;

  /**
   * Creates am empty instance.
   * @return  an empty instance
   */
  public static MarketDataInfo empty() {
    return new MarketDataInfo(new HashMap<MarketDataKey, ScalarMarketDataMetaData>(), new HashMap<MarketDataKey, TimeSeriesMarketDataMetaData>());
  }

  /**
   * Populates an instance with scalar and time series data. The maps are copied on construction.
   * @param scalars  scalar data, not null
   * @param timeSeries  time series data, not null
   * @return  an instance
   */
  public static MarketDataInfo of(final Map<MarketDataKey, ? extends ScalarMarketDataMetaData> scalars, 
      final Map<MarketDataKey, ? extends TimeSeriesMarketDataMetaData> timeSeries) {
    return new MarketDataInfo(scalars, timeSeries);
  }

  /**
   * Populates an instance with scalar and / or time series data. The data in the map is split according to type. 
   * If there is an unhandled type of meta-data, an exception is thrown.
   * <p>
   * This method is useful when the type of the meta-data is not tracked but is slower than {@link #of(Map, Map)}.
   * @param data  scalar and time series data, not null
   * @return  an instance
   */
  public static MarketDataInfo of(final Map<MarketDataKey, ? extends MarketDataMetaData> data) {
    return new MarketDataInfo(data);
  }

  /**
   * Restricted constructor.
   * @param scalars  scalar data, not null
   * @param timeSeries  time series data, not null
   */
  private MarketDataInfo(final Map<MarketDataKey, ? extends ScalarMarketDataMetaData> scalars, 
      final Map<MarketDataKey, ? extends TimeSeriesMarketDataMetaData> timeSeries) {
    ArgumentChecker.notNull(scalars, "scalars");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    _scalars = new HashMap<>(scalars);
    _timeSeries = new HashMap<>(timeSeries);
  }

  /**
   * Restricted constructor.
   * @param data  scalar and time series data, not null
   */
  private MarketDataInfo(final Map<MarketDataKey, ? extends MarketDataMetaData> data) {
    ArgumentChecker.notNull(data, "data");
    _scalars = new HashMap<>();
    _timeSeries = new HashMap<>();
    for (final Map.Entry<MarketDataKey, ? extends MarketDataMetaData> entry : data.entrySet()) {
      final MarketDataMetaData value = entry.getValue();
      if (value instanceof ScalarMarketDataMetaData) {
        _scalars.put(entry.getKey(), (ScalarMarketDataMetaData) entry.getValue());
      } else if (entry.getValue() instanceof TimeSeriesMarketDataMetaData) {
        _timeSeries.put(entry.getKey(), (TimeSeriesMarketDataMetaData) entry.getValue());
      } else {
        throw new IllegalArgumentException("Cannot handle meta-data: " + value);
      }
    }
  }

  /**
   * Adds information for a single scalar market data point.
   * @param key  the market data key, not null
   * @param metaData  the meta data, not null
   * @return  true if the key was already present
   */
  public boolean addScalarInfo(final MarketDataKey key, final ScalarMarketDataMetaData metaData) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(metaData, "metaData");
    return _scalars.put(key, metaData) != null;
  }

  /**
   * Adds information for a single time series.
   * @param key  the market data key, not null
   * @param metaData  the meta data, not null
   * @return  true if the key was already present
   */
  public boolean addTimeSeriesInfo(final MarketDataKey key, final TimeSeriesMarketDataMetaData metaData) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(metaData, "metaData");
    return _timeSeries.put(key, metaData) != null;
  }

  /**
   * Adds information for a single scalar data point or time series or throws an exception if the meta-data type
   * is not handled.
   * @param key  the market data key, not null
   * @param metaData  the meta-data, not null, must be scalar or time series
   * @return  true if the key was already present
   */
  public boolean addInfo(final MarketDataKey key, final MarketDataMetaData metaData) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(metaData, "metaData");
    if (metaData instanceof ScalarMarketDataMetaData) {
      return _scalars.put(key, (ScalarMarketDataMetaData) metaData) != null;
    } else if (metaData instanceof TimeSeriesMarketDataMetaData) {
      return _timeSeries.put(key, (TimeSeriesMarketDataMetaData) metaData) != null;
    }
    throw new IllegalArgumentException("Cannot handle meta-data: " + metaData);
  }

  /**
   * Adds information for multiple scalar market data points.
   * @param data  the market data key, not null
   */
  public void addScalarInfo(final Map<MarketDataKey, ScalarMarketDataMetaData> data) {
    ArgumentChecker.notNull(data, "data");
    _scalars.putAll(data);
  }

  /**
   * Adds information for multiple time series.
   * @param data  the market data key, not null
   */
  public void addTimeSeriesInfo(final Map<MarketDataKey, TimeSeriesMarketDataMetaData> data) {
    ArgumentChecker.notNull(data, "data");
    _timeSeries.putAll(data);
  }

  /**
   * Adds information for multiple scalar data points or time series.
   * @param data  the market data key, not null
   */
  public void addInfo(final Map<MarketDataKey, ? extends MarketDataMetaData> data) {
    ArgumentChecker.notNull(data, "data");
    for (final Map.Entry<MarketDataKey, ? extends MarketDataMetaData> entry : data.entrySet()) {
      final MarketDataMetaData value = entry.getValue();
      if (value instanceof ScalarMarketDataMetaData) {
        _scalars.put(entry.getKey(), (ScalarMarketDataMetaData) entry.getValue());
      } else if (entry.getValue() instanceof TimeSeriesMarketDataMetaData) {
        _timeSeries.put(entry.getKey(), (TimeSeriesMarketDataMetaData) entry.getValue());
      } else {
        throw new IllegalArgumentException("Cannot handle meta-data: " + value);
      }
    }
  }

  /**
   * Gets all scalar market data information. Returns an empty map if there is no scalar data.
   * @return  the scalar market data information
   */
  public Map<MarketDataKey, ? extends MarketDataMetaData> getScalars() {
    return _scalars;
  }

  /**
   * Gets all time series market data information. Returns an empty map if there is no time series data.
   * @return  the time series data information
   */
  public Map<MarketDataKey, ? extends MarketDataMetaData> getTimeSeries() {
    return _timeSeries;
  }

  /**
   * Gets the number of scalar and time series keys for which information is available.
   * @return  the number of keys
   */
  public int size() {
    return _scalars.size() + _timeSeries.size();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _scalars.hashCode();
    result = prime * result + _timeSeries.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MarketDataInfo)) {
      return false;
    }
    final MarketDataInfo other = (MarketDataInfo) obj;
    if (!Objects.equals(_scalars, other._scalars)) {
      return false;
    }
    if (!Objects.equals(_timeSeries, other._timeSeries)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("MarketDataInfo[scalars=");
    // scalar data types don't contain distinguishing information
    sb.append(_scalars.keySet().toString());
    sb.append(", timeSeries=");
    sb.append(_timeSeries.toString());
    sb.append("]");
    return sb.toString();
  }
}
