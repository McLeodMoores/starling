/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedInstance;

/**
 * A class representing a data field name, e.g. Market_Value or LAST_PRICE for a piece of market data.
 * This represents the type of data for a given identifier (price, yeild, rate, bid, ask, etc).
 */
public final class DataField implements NamedInstance {
  /**
   * Default pre-normalized price field for Starling.
   */
  public static final DataField PRICE = DataField.of(MarketDataRequirementNames.MARKET_VALUE);
  /** The field name */
  private final String _fieldName;

  /**
   * Restricted constructor.
   * @param fieldName  the field name, not null
   */
  private DataField(final String fieldName) {
    _fieldName = fieldName;
  }

  /**
   * Static factory method for creating instances.
   * @param fieldName  the name of the field, not null
   * @return a DataField instance, not null
   */
  public static DataField of(final String fieldName) {
    return DataFieldFactory.INSTANCE.of(ArgumentChecker.notNull(fieldName, "fieldName"));
  }

  /**
   * For internal use only, hence package protected, use of() instead.  This call will not add
   * a previously unknown instance to the underlying factory, which is why it should be used,
   * except by the factory itself.
   * @param fieldName  the name of the field, not null
   * @return a DataField instance, not null
   */
  static DataField parse(final String fieldName) {
    return new DataField(ArgumentChecker.notNull(fieldName, "fieldName"));
  }

  /**
   * @return the name of the field
   */
  @Override
  public String getName() {
    return _fieldName;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof DataField)) { // remember instanceof handles null...
      return false;
    }
    final DataField dataField = (DataField) other;
    return _fieldName.equals(dataField._fieldName);
  }

  @Override
  public int hashCode() {
    return _fieldName.hashCode();
  }

  @Override
  public String toString() {
    return getName();
  }
}
