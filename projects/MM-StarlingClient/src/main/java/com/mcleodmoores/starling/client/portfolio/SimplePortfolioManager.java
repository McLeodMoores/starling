/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.ArgumentChecker;

import java.util.List;

/**
 * Class to auto-build portfolio structure and maintain portfolio containing all positions under a hierarchy.
 */
public class SimplePortfolioManager {
  private static final String ALL_PORTFOLIOS_NAME = "ALL_PORTFOLIOS";
  private final ToolContext _toolContext;
  private final PortfolioManager _utils;

  /**
   * Public constructor.
   * @param toolContext  the tool context
   */
  public SimplePortfolioManager(final ToolContext toolContext) {
    _toolContext = ArgumentChecker.notNull(toolContext, "toolContext");
    _utils = new PortfolioManager(toolContext);
  }

  /**
   * Update a portfolio and update it's node in the super-portfolio.
   * @param portfolioName  the name of the portfolio
   * @param trades  a list of trades
   */
  public void updatePortfolio(final String portfolioName, final List<FXForwardTrade> trades) {
    SimplePortfolio portfolio = new SimplePortfolio(portfolioName);
    SimplePortfolioNode portfolioNode = portfolio.getRootNode();
    for (FXForwardTrade trade : trades) {
      portfolioNode.addPosition(trade.toPosition());
    }
    portfolioNode.setName(portfolioName);
    _utils.savePortfolio(portfolio);
    Portfolio mainPortfolio = _utils.loadPortfolio(PortfolioKey.of(ALL_PORTFOLIOS_NAME));
    final PortfolioNode rootNode = mainPortfolio.getRootNode();
    PortfolioNode targetNode = null;
    for (PortfolioNode node : rootNode.getChildNodes()) {
      if (node.getName().equals(portfolioName)) {
        targetNode = node;
        break;
      }
    }
    if (targetNode != null) {
      rootNode.getChildNodes().remove(targetNode);
    }
    rootNode.getChildNodes().add(portfolioNode);
    _utils.savePortfolio(mainPortfolio);
  }

  /**
   * Delete a named portfolio and remove from super portfolio.
   * @param portfolioName  the portfolio name
   */
  public void deletePortfolio(final String portfolioName) {
    _utils.deletePortfolio(PortfolioKey.of(portfolioName));
    Portfolio mainPortfolio = _utils.loadPortfolio(PortfolioKey.of(ALL_PORTFOLIOS_NAME));
    final PortfolioNode rootNode = mainPortfolio.getRootNode();
    PortfolioNode targetNode = null;
    for (PortfolioNode node : rootNode.getChildNodes()) {
      if (node.getName().equals(portfolioName)) {
        targetNode = node;
        break;
      }
    }
    if (targetNode != null) {
      rootNode.getChildNodes().remove(targetNode);
    }
    _utils.savePortfolio(mainPortfolio);
  }
}
