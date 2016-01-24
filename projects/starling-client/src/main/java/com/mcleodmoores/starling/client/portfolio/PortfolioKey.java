/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A key for identifying portfolios.  The key may include ids to speed up resolution and handle
 * duplicate names when interrogating the system.  When searching, a key should only contain the portfolio name.
 * (see {@link PortfolioKey#of(String)}.
 */
public final class PortfolioKey {
  /** The unique id of the portfolio */
  private final UniqueId _uniqueId;
  /** The portfolio name */
  private final String _name;

  /**
   * Restricted constructor.
   * @param name  the portfolio name, not null
   * @param uniqueId  the unique id of the portfolio, can be null
   */
  private PortfolioKey(final String name, final UniqueId uniqueId) {
    _name = ArgumentChecker.notNull(name, "name");
    _uniqueId = uniqueId;
  }

  /**
   * Static factory method used to create instances of PortfolioKey when the unique id of the portfolio is not known.
   * @param name  the name of the portfolio, not null
   * @return the portfolio key, not null
   */
  public static PortfolioKey of(final String name) {
    return new PortfolioKey(name, null);
  }

  /**
   * Static factory method used to create instances of PortfolioKey, typically when the unique id is known.
   * @param name  the name of the portfolio, not null
   * @param uniqueId  the unique id of the portfolio, if known, null otherwise
   * @return the portfolio key, not null
   */
  public static PortfolioKey of(final String name, final UniqueId uniqueId) {
    return new PortfolioKey(name, uniqueId);
  }

  /**
   * Gets the unique id of the portfolio.
   * @return the unique id of the portfolio, or null if not known when this key was created
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Returns true if the key contains a unique id, which can speed up lookup and handles duplicates better.
   * @return true, if this key contains a unique id for the portfolio
   */
  public boolean hasUniqueId() {
    return _uniqueId != null;
  }

  /**
   * Gets the name of the portfolio.
   * @return the name of the portfolio, not null
   */
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof PortfolioKey)) {
      return false;
    }
    final PortfolioKey other = (PortfolioKey) o;
    return other.getName().equals(getName());
  }

  @Override
  public String toString() {
    return "PortfolioKey[" + _name + (hasUniqueId() ? "(" + _uniqueId.toString() + ")]" : "]");
  }
}
