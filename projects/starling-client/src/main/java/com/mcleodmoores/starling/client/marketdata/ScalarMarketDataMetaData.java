/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

/**
 * Class to show that the data in question is a piece of scalar meta-data.
 * Might want to refactor this to an enum.
 */
public class ScalarMarketDataMetaData implements MarketDataMetaData {
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
   * @return the type of data, in this case Double
   */
  public Class<?> getType() {
    return Double.class;
  }

  @Override
  public String toString() {
    return "ScalarMarketDataMetaData[Double]";
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
