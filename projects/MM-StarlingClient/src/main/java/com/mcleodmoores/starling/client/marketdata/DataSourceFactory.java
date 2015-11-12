/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;
import org.joda.convert.FromString;

/**
 * Factory for creating and registering named instances of DataSource.
 * This pattern is used because it saves memory and more importantly, UI tools can query available values.
 */
public class DataSourceFactory extends AbstractNamedInstanceFactory<DataSource> {
  /**
   * Singleton instance.
   */
  public static final DataSourceFactory INSTANCE = new DataSourceFactory();

  /**
   * Protected no-arg constructor.
   */
  protected DataSourceFactory() {
    super(DataSource.class);
  }

  /**
   * Return the named instance of a DataSource given a name, and create one if one isn't available
   * @param name  the name of the DataSource
   * @return the instance of the dataSource corresponding to the name
   */
  @FromString
  public DataSource of(final String name) {
    try {
      return instance(name);
    } catch (final IllegalArgumentException e) {
      ArgumentChecker.notNull(name, "name");
      final DataSource dataSource = DataSource.parse(name);
      return addInstance(dataSource);
    }
  }
}
