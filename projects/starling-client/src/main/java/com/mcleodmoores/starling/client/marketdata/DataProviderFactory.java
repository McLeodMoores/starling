/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import org.joda.convert.FromString;

import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for creating and registering named instances of DataProvider.
 * This pattern is used because it saves memory and more importantly, UI tools can query available values.
 */
public class DataProviderFactory extends AbstractNamedInstanceFactory<DataProvider> {
  /**
   * Singleton instance.
   */
  public static final DataProviderFactory INSTANCE = new DataProviderFactory();

  /**
   * Protected no-arg constructor.
   */
  protected DataProviderFactory() {
    super(DataProvider.class);
  }

  /**
   * Return the named instance of a DataProvider given a name, and create one if one isn't available.
   * @param name  the name of the DataProvider
   * @return the instance of the dataProvider corresponding to the name
   */
  @FromString
  public DataProvider of(final String name) {
    try {
      return instance(name);
    } catch (final IllegalArgumentException e) {
      ArgumentChecker.notNull(name, "name");
      final DataProvider dataProvider = DataProvider.parse(name);
      return addInstance(dataProvider);
    }
  }
}
