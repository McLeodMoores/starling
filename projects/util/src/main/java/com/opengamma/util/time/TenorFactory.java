/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.util.Locale;

import org.joda.convert.FromString;

import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for {@link Tenor} named instances.
 */
public final class TenorFactory extends AbstractNamedInstanceFactory<Tenor> {

  /**
   * Singleton instance of {@code TenorFactory}.
   */
  public static final TenorFactory INSTANCE = new TenorFactory();

  /**
   * Restricted constructor.
   */
  private TenorFactory() {
    super(Tenor.class);
    addInstance(Tenor.TN, "TN", "T/N");
    addInstance(Tenor.ON, "ON", "O/N");
    addInstance(Tenor.SN, "SN", "S/N");
    for (int i = 1; i <= 30; i++) {
      addInstance(Tenor.ofDays(i), "P" + i + "D", i + "D");

    }
    for (int i = 1; i <= 6; i++) {
      addInstance(Tenor.ofDays(i * 7), "P" + i + "W", i + "W");

    }
    for (int i = 1; i <= 30; i++) {
      addInstance(Tenor.ofMonths(i), "P" + i + "M", i + "M");

    }
    for (int i = 1; i <= 60; i++) {
      addInstance(Tenor.ofYears(i), "P" + i + "Y", i + "Y");

    }
  }

  /**
   * Finds a tenor by name, ignoring case.
   * <p>
   * This method dynamically creates the tenor if it is missing.
   * 
   * @param name
   *          the name of the instance to find, not null
   * @return the tenor, null if not found
   */
  @FromString
  public Tenor of(final String name) {
    try {
      return INSTANCE.instance(name);
    } catch (final IllegalArgumentException e) {
      ArgumentChecker.notNull(name, "name");
      final Tenor tenor = Tenor.parse(name.toUpperCase(Locale.ENGLISH));
      return addInstance(tenor);
    }
  }
}