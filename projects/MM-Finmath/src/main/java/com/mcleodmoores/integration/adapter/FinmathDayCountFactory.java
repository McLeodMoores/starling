/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.adapter;

import org.joda.convert.FromString;

import com.opengamma.util.AbstractNamedInstanceFactory;

/**
 * A factory for {@link FinmathDayCount} instances. The class mappings are stored in FinmathDayCount.properties
 * in this directory.
 */
public final class FinmathDayCountFactory extends AbstractNamedInstanceFactory<FinmathDayCount> {
  /**
   * A static instance.
   */
  public static final FinmathDayCountFactory INSTANCE = new FinmathDayCountFactory();

  /**
   * Returns a named instance of {@link FinmathDayCount}.
   * @param name The name, not null
   * @return The instance, if found.
   */
  @FromString
  public static FinmathDayCount of(final String name) {
    return INSTANCE.instance(name);
  }

  /**
   * Restricted constructor.
   */
  /* package */ FinmathDayCountFactory() {
    super(FinmathDayCount.class);
    loadFromProperties();
  }
}
