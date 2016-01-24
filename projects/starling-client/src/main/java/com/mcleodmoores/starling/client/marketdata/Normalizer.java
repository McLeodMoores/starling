/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.NamedInstance;

/**
 * Interface for pre-processing normalizers.  These are to be applied to the data before it is stored. Not to be confused
 * with on-the-fly normalization that converts from e.g. LAST_PRICE to Market_Value.
 */
public interface Normalizer extends NamedInstance {
  /**
   * Normalize the value of a piece of market data.
   * @param idBundle  the identifier bundle, not null
   * @param field  the data field, not null
   * @param source  the data source, not null
   * @param provider  the data provider, not null
   * @param value  the value to be normalized, not null
   * @return the normalized value, not null
   */
  Object normalize(ExternalIdBundle idBundle, DataField field, DataSource source, DataProvider provider, Object value);
}
