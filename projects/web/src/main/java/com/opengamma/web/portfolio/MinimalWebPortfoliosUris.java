/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.portfolio;

import java.net.URI;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;

/**
 * URIs for web-based portfolios.
 */
public class MinimalWebPortfoliosUris {

  /**
   * The data.
   */
  private final WebPortfoliosData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public MinimalWebPortfoliosUris(final WebPortfoliosData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return portfolios();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI portfolios() {
    return MinimalWebPortfoliosResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI portfolio() {
    return MinimalWebPortfolioResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param portfolio  the portfolio, not null
   * @return the URI
   */
  public URI portfolio(final ManageablePortfolio portfolio) {
    return MinimalWebPortfolioResource.uri(_data, portfolio.getUniqueId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI node() {
    return MinimalWebPortfolioNodeResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param node  the node, not null
   * @return the URI
   */
  public URI node(final ManageablePortfolioNode node) {
    return MinimalWebPortfolioNodeResource.uri(_data, node.getUniqueId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI nodePositions() {
    return MinimalWebPortfolioNodePositionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param node  the node, not null
   * @return the URI
   */
  public URI nodePositions(final ManageablePortfolioNode node) {
    return MinimalWebPortfolioNodePositionsResource.uri(_data, node.getUniqueId());
  }

  /**
   * Gets the URI.
   * @param positionId  the position id, not null
   * @return the URI
   */
  public URI nodePosition(final ObjectIdentifiable positionId) {
    return MinimalWebPortfolioNodePositionResource.uri(_data, positionId);
  }

}
