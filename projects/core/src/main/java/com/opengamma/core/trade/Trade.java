/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.trade;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.Attributable;
import com.opengamma.core.position.Counterparty;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * A single trade
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicSPI
public interface Trade extends Attributable, UniqueIdentifiable {
  /**
   * Gets the unique id for the trade.
   * @return the unique id of the trade, can be null
   */
  UniqueId getUniqueId();

  /**
   * External ids for this trade
   * @return the id bundle, can be null.
   */
  ExternalIdBundle getExternalTradeIds();
}
