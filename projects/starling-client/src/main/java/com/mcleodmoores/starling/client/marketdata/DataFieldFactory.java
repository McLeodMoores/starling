/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import org.joda.convert.FromString;

import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for creating and registering named instances of DataField.
 * This pattern is used because it saves memory and more importantly, UI tools can query available values.
 */
public class DataFieldFactory extends AbstractNamedInstanceFactory<DataField> {
  /**
   * Singleton instance.
   */
  public static final DataFieldFactory INSTANCE = new DataFieldFactory();

  /**
   * Protected no-arg constructor.
   */
  protected DataFieldFactory() {
    super(DataField.class);
  }

  /**
   * Return the named instance of a DataField given a name, and create one if one isn't available
   * @param name  the name of the DataField
   * @return the instance of the data field corresponding to the name
   */
  @FromString
  public DataField of(final String name) {
    try {
      return instance(name);
    } catch (final IllegalArgumentException e) {
      ArgumentChecker.notNull(name, "name");
      final DataField dataField = DataField.parse(name);
      return addInstance(dataField);
    }
  }
}
