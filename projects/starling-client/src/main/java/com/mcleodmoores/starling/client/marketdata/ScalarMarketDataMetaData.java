/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

/**
 * Class to show that the data in question is a piece of scalar meta-data.
 */
//TODO  Might want to refactor this to an enum.
public final class ScalarMarketDataMetaData implements MarketDataMetaData {
  /**
   * Singleton instance.
   */
  public static final ScalarMarketDataMetaData INSTANCE = new ScalarMarketDataMetaData();

  /**
   * Protected no-arg constructor.
   */
  private ScalarMarketDataMetaData() {
  }

  /**
   * Gets the type of the data, always a Double.
   * @return the type of data, in this case Double
   */
  @Override
  public Class<?> getType() {
    return Double.class;
  }

  @Override
  public String toString() {
    return "ScalarMarketDataMetaData[Double]";
  }

}
