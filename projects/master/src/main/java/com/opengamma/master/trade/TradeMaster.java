/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose Trade master.
 * <p>
 * The Trade master provides a uniform view over a set of Trade definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface TradeMaster extends AbstractChangeProvidingMaster<TradeDocument> {

  /**
   * Searches for Trades matching the specified search criteria.
   *
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  TradeSearchResult search(TradeSearchRequest request);

  /**
   * Queries the history of a single Trade.
   * <p>
   * The request must contain an object identifier to identify the Trade.
   *
   * @param request  the history request, not null
   * @return the Trade history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  TradeHistoryResult history(TradeHistoryRequest request);

  /**
   * Gets a trade by unique identifier.
   * <p>
   * If the master supports history then the version in the identifier will be
   * used to return the requested historic version.
   *
   * @param tradeId
   *          the trade unique identifier, not null
   * @return the trade, not null
   * @throws IllegalArgumentException
   *           if the request is invalid
   * @throws com.opengamma.DataNotFoundException
   *           if there is no trade with that unique identifier
   */
  ManageableTrade getTrade(UniqueId tradeId);

}
