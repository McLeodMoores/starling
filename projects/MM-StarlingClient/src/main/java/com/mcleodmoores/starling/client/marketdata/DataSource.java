/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.util.NamedInstance;

/**
 * Created by jim on 08/05/15.
 */
public final class DataSource implements NamedInstance {
  /**
   * A default data source.
   */
  public static final DataSource DEFAULT = DataSource.of("DEFAULT");
  private final String _fieldName;

  private DataSource(final String fieldName) {
    _fieldName = fieldName;
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
   * a previously unknown instance to the underlying factory, which is why it should be used,
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
    return _fieldName;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof DataSource)) { // remember instanceof handles null...
      return false;
    }
    DataSource dataField = (DataSource) other;
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
