/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.util.NamedInstance;

/**
 * Class to represent a data source. An example could be Bloomberg.
 */
public final class DataSource implements NamedInstance {
  /**
   * A default data source.
   */
  public static final DataSource DEFAULT = DataSource.of("DEFAULT");
  /** The data source name */
  private final String _sourceName;

  /**
   * Restricted constructor.
   * @param sourceName  the source name, not null
   */
  private DataSource(final String sourceName) {
    _sourceName = sourceName;
  }

  /**
   * Static factory method for creating instances.
   * @param sourceName  the name of the source, not null
   * @return a DataField instance, not null
   */
  public static DataSource of(final String sourceName) {
    return DataSourceFactory.INSTANCE.of(sourceName);
  }

  /**
   * For internal use only, hence package protected, use of() instead.  This call will not add
   * a previously unknown instance to the underlying factory, which is why it should not be used,
   * except by the factory itself.
   * @param sourceName  the name of the field, not null
   * @return a DataSource instance, not null
   */
  static DataSource parse(final String sourceName) {
    return new DataSource(sourceName);
  }

  /**
   * @return the name of the source
   */
  @Override
  public String getName() {
    return _sourceName;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof DataSource)) { // remember instanceof handles null...
      return false;
    }
    final DataSource dataField = (DataSource) other;
    return _sourceName.equals(dataField._sourceName);
  }

  @Override
  public int hashCode() {
    return _sourceName.hashCode();
  }

  @Override
  public String toString() {
    return getName();
  }
}
