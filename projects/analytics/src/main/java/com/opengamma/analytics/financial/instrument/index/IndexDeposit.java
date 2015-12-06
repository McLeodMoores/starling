/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.money.Currency;

/**
 * Class describing an deposit-like index (in particular Ibor and OIS).
 * @deprecated  use {@link Index}, to which this class delegates
 */
@Deprecated
public abstract class IndexDeposit extends Index {

  /**
   * Constructor.
   * @param name The index name.
   * @param currency The underlying currency.
   */
  public IndexDeposit(final String name, final Currency currency) {
    super(name, currency);
  }

  @Override
  public String toString() {
    return getName() + "-" + getCurrency().toString();
  }

  @Override
  public int hashCode() {
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final IndexDeposit other = (IndexDeposit) obj;
    if (!ObjectUtils.equals(getCurrency(), other.getCurrency())) {
      return false;
    }
    if (!ObjectUtils.equals(getName(), other.getName())) {
      return false;
    }
    return true;
  }

}
