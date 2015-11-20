/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

/**
 * A target key for access to market data based results such as curves and surfaces.  They are distinguished by different ResultKeys.
 */
public class MarketDataTargetKey implements TargetKey {
  private MarketDataTargetKey() {
  }
  /**
   * Static factory method used to create instances.
   * @return the market data target key, not null
   */
  public static MarketDataTargetKey instance() {
    return new MarketDataTargetKey();
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof MarketDataTargetKey)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "MarketDataTargetKey[SINGLETON]";
  }
}
