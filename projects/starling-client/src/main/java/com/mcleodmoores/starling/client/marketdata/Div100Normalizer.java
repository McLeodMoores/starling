/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.id.ExternalIdBundle;

/**
 * A normalizer that divides any Numbers by 100.  Other objects are left unchanged.
 */
@MarketData(group = "Normalization", description = "Div100Normalizer")
public final class Div100Normalizer implements Normalizer {
  /**
   * Singleton instance.
   */
  public static final Div100Normalizer INSTANCE = new Div100Normalizer();

  /**
   * Restricted constructor.
   */
  private Div100Normalizer() { }

  @Override
  public Object normalize(final ExternalIdBundle idBundle, final DataField field, final DataSource source,
      final DataProvider provider, final Object value) {
    if (value instanceof Number) {
      final Number number = (Number) value;
      return number.doubleValue() / 100d;
    }
    return value;
  }

  /**
   * @return the name of the normalizer
   */
  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 100;
  }
}
