/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * A set of market data, typically all that is required by a given view execution.  Methods and structure are
 * very similar to a Map&lt;MarketDataKey, Object&gt; so is mutable.
 */
public final class MarketDataSet {
  /** The data */
  private final Map<MarketDataKey, Object> _dataSet;

  /**
   * Restricted constructor.
   * @param dataSet  the data, not null
   */
  private MarketDataSet(final Map<MarketDataKey, Object> dataSet) {
    _dataSet = new HashMap<>(ArgumentChecker.notNull(dataSet, "dataSet"));
  }

  /**
   * Restricted constructor that creates an empty data set.
   */
  private MarketDataSet() {
    _dataSet = new HashMap<>();
  }

  /**
   * Gets a mutable empty market data set.
   * @return an empty data set (mutable)
   */
  public static MarketDataSet empty() {
    return new MarketDataSet();
  }

  /**
   * Create a market data set from a pre-existing map.
   * @param dataSet  a map of MarketDataKey to Object, not null
   * @return a market data set instance
   */
  public static MarketDataSet of(final Map<MarketDataKey, Object> dataSet) {
    ArgumentChecker.notNull(dataSet, "dataSet");
    return new MarketDataSet(dataSet);
  }

  /**
   * Returns true if the market data set contains an entry for the key.
   * @param key  a market data key, not null
   * @return true, if the market data set contains an entry for the key
   */
  public boolean containsKey(final MarketDataKey key) {
    ArgumentChecker.notNull(key, "key");
    return _dataSet.containsKey(key);
  }

  /**
   * Add/update an entry in the market data set.
   * @param key  the market data key, not null
   * @param value  the value, not null
   */
  public void put(final MarketDataKey key, final Object value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _dataSet.put(key, value);
  }

  /**
   * Add all entries in another market data set to this one.
   * @param dataSet  the market data set to merge
   */
  public void putAll(final MarketDataSet dataSet) {
    _dataSet.putAll(dataSet._dataSet);
  }

  /**
   * Retrieve the value for a given key.
   * @param key  the market data key, not null
   * @return the value associated with the key, or null if no value is associated
   */
  public Object get(final MarketDataKey key) {
    ArgumentChecker.notNull(key, "key");
    return _dataSet.get(key);
  }

  /**
   * Remove the market data value associated with the provided key.
   * @param key  the market data key, not null
   * @return the value that was removed, or null if no value matched the key
   */
  public Object remove(final MarketDataKey key) {
    ArgumentChecker.notNull(key, "key");
    return _dataSet.remove(key);
  }

  /**
   * Gets all of the market data keys in this set.
   * @return the set of all market data keys
   */
  public Set<MarketDataKey> keySet() {
    return _dataSet.keySet();
  }

  /**
   * Gets the entry set of this market data set.
   * @return the set of market data key/value entries in this market data set
   */
  public Set<Map.Entry<MarketDataKey, Object>> entrySet() {
    return _dataSet.entrySet();
  }

  /**
   * Gets the number of entries in this set.
   * @return the number of entries in this market data set
   */
  public int size() {
    return _dataSet.size();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    if (!(other instanceof MarketDataSet)) {
      return false;
    }
    final MarketDataSet o = (MarketDataSet) other;
    return o._dataSet.equals(_dataSet);
  }

  @Override
  public int hashCode() {
    return _dataSet.hashCode();
  }

  @Override
  public String toString() {
    return "MarketDataSet[" + _dataSet.toString() + "]";
  }
}
