/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.id.ExternalIdBundle;

/**
 * A normalizer that leaves data unchanged.
 */
@MarketData(type = "Normalization", description = "Unit")
public final class UnitNormalizer implements Normalizer {
  /**
   * Singleton instance.
   */
  public static final UnitNormalizer INSTANCE = new UnitNormalizer();

  /**
   * Restricted constructor.
   */
  private UnitNormalizer() {
  }

  @Override
  public Object normalize(final ExternalIdBundle idBundle, final DataField field, final DataSource source,
      final DataProvider provider, final Object value) {
    return value;
  }

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
    return 1;
  }

}
