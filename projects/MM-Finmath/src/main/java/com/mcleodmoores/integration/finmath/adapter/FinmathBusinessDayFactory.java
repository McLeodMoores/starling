/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.adapter;

import org.joda.convert.FromString;

import com.opengamma.util.AbstractNamedInstanceFactory;

/**
 * A factory for {@link FinmathBusinessDay} instances. The class mappings are stored in FinmathBusinessDay.properties
 * in this directory.
 */
public class FinmathBusinessDayFactory extends AbstractNamedInstanceFactory<FinmathBusinessDay> {
  /**
   * A static instance.
   */
  public static final FinmathBusinessDayFactory INSTANCE = new FinmathBusinessDayFactory();

  /**
   * Returns a named instance of {@link FinmathBusinessDay}.
   * @param name The name, not null
   * @return The instance, if found.
   */
  @FromString
  public static FinmathBusinessDay of(final String name) {
    return INSTANCE.instance(name);
  }

  /**
   * Restricted constructor.
   */
  /* protected */ FinmathBusinessDayFactory() {
    super(FinmathBusinessDay.class);
    loadFromProperties();
  }
}
