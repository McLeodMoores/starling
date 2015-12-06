/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.money.Currency;

/**
 * Class describing a price index, like the one used in inflation instruments.
 * @deprecated  use {@link PriceIndex}
 */
@Deprecated
public class IndexPrice {
  /** The delegated index */
  private final PriceIndex _index;

  /**
   * Constructor of the price index.
   * @param name The index name. Not null.
   * @param ccy The currency in which the index is computed. Not null.
   */
  public IndexPrice(final String name, final Currency ccy) {
    _index = new PriceIndex(name, ccy);
  }

  /**
   * Gets this index as a {@link PriceIndex}.
   * @return  a price index
   */
  public PriceIndex toPriceIndex() {
    return _index;
  }

  /**
   * Gets the name of the price index.
   * @return The name.
   */
  public String getName() {
    return _index.getName();
  }

  /**
   * Gets the currency in which the index is computed.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _index.getCurrency();
  }

  @Override
  public String toString() {
    return _index.toString();
  }

  @Override
  public int hashCode() {
    return _index.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IndexPrice)) {
      return false;
    }
    final IndexPrice other = (IndexPrice) obj;
    if (!ObjectUtils.equals(getCurrency(), other.getCurrency())) {
      return false;
    }
    if (!ObjectUtils.equals(getName(), other.getName())) {
      return false;
    }
    return true;
  }

}
