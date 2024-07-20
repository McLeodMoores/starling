/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.trade;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of portfolios and Trades/trades as accessed by the engine.
 * <p>
 * This interface provides a simple view of portfolios and Trades as needed by the engine.
 * This may be backed by a full-featured Trade master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface TradeSource extends ChangeProvider {
  /**
   * Gets a Trade by its object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify a single Trade at a single
   * version-correction that can then be referenced by its unique identifier.
   *
   * @param objectId the object identifier, not null
   * @param versionCorrection the version-correction, not null
   * @return the Trade, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the Trade cannot be found
   * @throws RuntimeException if an error occurs
   */
  Trade getTrade(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets a trade by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single trade at a single version-correction.
   *
   * @param uniqueId  the unique identifier, not null
   * @return the trade, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the trade cannot be found
   * @throws RuntimeException if an error occurs
   */
  Trade getTrade(UniqueId uniqueId);
}
