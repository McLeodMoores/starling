/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;
import org.joda.convert.FromString;

/**
 * Factory for creating and registering named instances of Normalizer.
 * This pattern is used because it saves memory and more importantly, UI tools can query available values.
 */
public class NormalizerFactory extends AbstractNamedInstanceFactory<Normalizer> {
  /**
   * Singleton instance.
   */
  public static final NormalizerFactory INSTANCE = new NormalizerFactory();

  /**
   * Protected no-arg constructor.
   */
  protected NormalizerFactory() {
    super(Normalizer.class);
  }

  /**
   * Return the named instance of a Normalizer given a name, and create one if one isn't available.
   * @param name  the name of the Normalizer
   * @return the instance of the normalizer corresponding to the name
   */
  @FromString
  public Normalizer of(final String name) {
    return INSTANCE.instance(name);
  }
}
