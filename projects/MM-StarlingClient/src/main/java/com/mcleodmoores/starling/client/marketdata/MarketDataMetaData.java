/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

/**
 * Common interface for MarketDataMetaData implementations.
 */
public interface MarketDataMetaData {
  /**
   * @return the type of market data required, typically Double or LocalDateDoubleTimeSeries
   */
  Class<?> getType();
}
