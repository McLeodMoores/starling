/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ComputedValueResult;

/**
 * Interface defining the operations available on a result from the Analytics service.
 */
public interface ResultModel {
  /**
   * Separator character between portfolio nodes when specifying a portfolio path.
   */
  String PORTFOLIO_SEPARATOR = "/";

  /**
   * @return the set of result keys as were requested according to the view definition associated with the portfolio (portfolio nodes, positions, trades,
   * securities)
   */
  Set<ResultKey> getRequestedPortfolioResultKeys();

  /**
   * @return the set of result keys as were requested according to the view definition associated with market data (curves, surfaces, etc)
   */
  Set<ResultKey> getRequestedMarketDataResultKeys();

  /**
   * @return the set of result keys as they were actually resolved by the system (these will typically include extra meta-data in their ValueProperties
   * but will 'satisfy' the requested keys.
   */
  Set<ResultKey> getAllResolvedResultKeys();

  /**
   * Get all the results for a particular target, akin to getting all the columns for a row in a tabular result set.
   * @param targetKey  the target (portfolio node, position, trade, market data, etc)
   * @return a map of resultKeys to computed values, not null
   */
  Map<ResultKey, ComputedValueResult> getResultsForTarget(TargetKey targetKey);

  /**
   * Get a list of the target keys in the order of the portfolio.
   * If portfolio nodes are included these will interspersed at the top of each block of positions/trades.  Market data will follow
   * after the portfolio data if included together with portfolio data, followed by any legacy keys.  This should allow easy printing of
   * portfolio structure and/or import export.
   * @param includeTargets  an enumset of the types of target to include.
   * @return an ordered list of target keys
   */
  List<TargetKey> getTargetKeys(EnumSet<TargetType> includeTargets);

  /**
   * Corresponds to the type's TargetKey.
   */
  public enum TargetType {
    /**
     * PortfolioNodeTargetKey.
     */
    PORTFOLIO_NODE,
    /**
     * PositionTargetKey.
     */
    POSITION,
    /**
     * TradeTargetKey.
     */
    TRADE,
    /**
     * MarketDataKey.
     */
    MARKET_DATA,
    /**
     * Primitive.
     */
    PRIMITIVE,
    /**
     * LegacyKey.
     */
    LEGACY
  }
}
