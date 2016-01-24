/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.util.NamedInstance;

/**
 * Class to represent an entity that provides data to a data source.  Examples might be Barclays as a provider to Bloomberg as
 * a data source.
 */
public final class DataProvider implements NamedInstance {
  /**
   * Default value.
   */
  public static final DataProvider DEFAULT = DataProvider.of("DEFAULT");
  /** The data provider name */
  private final String _providerName;

  /**
   * Restricted constructor.
   * @param providerName  the provider name, not null
   */
  private DataProvider(final String providerName) {
    _providerName = providerName;
  }

  /**
   * Public factory method for looking up instances of DataProvider by name, or creating new instances if they don't exist.
   * @param providerName  the name of the data provider, not null
   * @return an existing instance if one already available or a new instance if one not available, not null
   */
  public static DataProvider of(final String providerName) {
    return DataProviderFactory.INSTANCE.of(providerName);
  }

  /**
   * Package protected parse method, used by factory to create instances.  Do not call except from Factory.
   * Specifically, this method will create new instances rather than returning existing instances.
   * @param providerName  the name of the provider
   * @return an instance.
   */
  static DataProvider parse(final String providerName) {
    return new DataProvider(providerName);
  }

  /**
   * @return the name of the data provider
   */
  @Override
  public String getName() {
    return _providerName;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof DataProvider)) { // remember instanceof handles null...
      return false;
    }
    final DataProvider dataField = (DataProvider) other;
    return _providerName.equals(dataField._providerName);
  }

  @Override
  public int hashCode() {
    return _providerName.hashCode();
  }

  @Override
  public String toString() {
    return getName();
  }
}
