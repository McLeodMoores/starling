/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;


import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * A source of portfolios and positions/trades as accessed by the engine.
 * <p>
 * This interface provides a simple view of portfolios and positions as needed by the engine.
 * This may be backed by a full-featured position master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface PositionSource extends ChangeProvider {

  /**
   * Finds a specific portfolio by unique identifier.
   * 
   * @param uniqueId  the unique identifier, null returns null
   * @return the portfolio, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Portfolio getPortfolio(UniqueId uniqueId);

  /**
   * Finds a specific node from any portfolio by unique identifier.
   * 
   * @param uniqueId  the unique identifier, null returns null
   * @return the node, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  PortfolioNode getPortfolioNode(UniqueId uniqueId);

  /**
   * Finds a specific position from any portfolio by unique identifier.
   * 
   * @param uniqueId  the unique identifier, null returns null
   * @return the position, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Position getPosition(UniqueId uniqueId);

  /**
   * Finds a specific trade from any portfolio by unique identifier.
   * 
   * @param uniqueId  the unique identifier, null returns null
   * @return the trade, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Trade getTrade(UniqueId uniqueId);

}
