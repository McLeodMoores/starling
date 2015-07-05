/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.generator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Security generator that wraps a collection of securities. The securities are generated
 * with repeated calls to {{@link #createSecurity()} and an exception is thrown if more
 * securities are requested than were provided in the original collection. The order in
 * which the securities are returned depends on the original collection.
 * @param <T> The type of the securities
 */
public class CollectionSecuritiesGenerator<T extends ManageableSecurity> extends SecurityGenerator<T> {
  /** The iterator */
  private final Iterator<T> _iterator;

  /**
   * @param securities The securities, not null
   */
  public CollectionSecuritiesGenerator(final Collection<T> securities) {
    ArgumentChecker.notNull(securities, "securities");
    _iterator = securities.iterator();
  }

  /**
   * @param securities The securities, not null
   */
  public CollectionSecuritiesGenerator(final T[] securities) {
    ArgumentChecker.notNull(securities, "securities");
    _iterator = Arrays.asList(securities).iterator();
  }

  @Override
  public T createSecurity() {
    if (_iterator.hasNext()) {
      return _iterator.next();
    }
    throw new IllegalStateException("Asked for more securities than were provided to the generator");
  }
}
