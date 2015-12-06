/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import com.opengamma.util.money.Currency;

/**
 * A class describing a price index, for example the UK CPI index.
 */
public class PriceIndex extends Index {
  /** This object is used as a key within the curve system, so the hash code should be pre-calculated */
  private final int _hashCode;

  /**
   * Constructs a price index.
   * @param name  the name of the index, not null
   * @param currency  the index currency, not null
   */
  public PriceIndex(final String name, final Currency currency) {
    super(name, currency);
    _hashCode = generateHashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("PriceIndex[");
    sb.append(getName());
    sb.append(", currency=");
    sb.append(getCurrency().getCode());
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  /**
   * Generates the hash code, which is stored internally.
   * @return  the hash code
   */
  private int generateHashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getCurrency().hashCode();
    result = prime * result + getName().hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PriceIndex)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return true;
  }
}
