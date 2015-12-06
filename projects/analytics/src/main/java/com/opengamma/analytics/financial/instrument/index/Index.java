/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.Objects;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Base class for objects that describe indices.
 */
public abstract class Index {
  /** The index name */
  private final String _name;
  /** The index currency */
  private final Currency _currency;

  /**
   * @param name  the index name, not null
   * @param currency  the index currency, not null
   */
  public Index(final String name, final Currency currency) {
    _name = ArgumentChecker.notNull(name, "name");
    _currency = ArgumentChecker.notNull(currency, "currency");
  }

  /**
   * Gets the index name.
   * @return  the index name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the index currency.
   * @return  the index currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Index[");
    sb.append(_name);
    sb.append(", currency=");
    sb.append(_currency.getCode());
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _name.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Index)) {
      return false;
    }
    final Index other = (Index) obj;
    if (!Objects.equals(_currency, other._currency)) {
      return false;
    }
    if (!Objects.equals(_name, other._name)) {
      return false;
    }
    return true;
  }
}
