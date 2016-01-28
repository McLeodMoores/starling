/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to auto-build portfolio structure and maintain portfolio containing all positions under a hierarchy.
 */
public class SimplePortfolioManager {
  /**
   * The name of the portfolio holding all positions.
   */
  public static final String ALL_PORTFOLIOS_NAME = "ALL_PORTFOLIOS";
  /** Portfolio management utilities */
  private final PortfolioManager _utils;

  /**
   * Public constructor.
   * @param toolContext  the tool context, not null
   */
  public SimplePortfolioManager(final ToolContext toolContext) {
    _utils = new PortfolioManager(toolContext);
  }

  /**
   * Update a portfolio and update its node in the super-portfolio.
   * @param portfolioName  the name of the portfolio, not null
   * @param trades  a list of trades, not null
   */
  //TODO update to use other trade types.
  public void updatePortfolio(final String portfolioName, final List<FXForwardTrade> trades) {
    ArgumentChecker.notNull(portfolioName, "portfolioName");
    ArgumentChecker.notNull(trades, "trades");
    final SimplePortfolio portfolio = new SimplePortfolio(portfolioName);
    final SimplePortfolioNode portfolioNode = portfolio.getRootNode();
    for (final FXForwardTrade trade : trades) {
      portfolioNode.addPosition(trade.toPosition());
    }
    portfolioNode.setName(portfolioName);
    _utils.savePortfolio(portfolio);
    final Portfolio mainPortfolio = _utils.loadPortfolio(PortfolioKey.of(ALL_PORTFOLIOS_NAME));
    final PortfolioNode rootNode = mainPortfolio.getRootNode();
    // have to copy the portfolio as the original child node structure is an unmodifiable list, so matching nodes cannot be added
    final SimplePortfolioNode copyRootNode = new SimplePortfolioNode(rootNode.getName());
    copyRootNode.setParentNodeId(rootNode.getParentNodeId());
    copyRootNode.setUniqueId(rootNode.getUniqueId());
    final List<PortfolioNode> childNodes = new ArrayList<>(rootNode.getChildNodes());
    PortfolioNode targetNode = null;
    for (final PortfolioNode node : rootNode.getChildNodes()) {
      if (node.getName().equals(portfolioName)) {
        targetNode = node;
        break;
      }
    }
    if (targetNode != null) {
      // replace existing child of same name
      childNodes.remove(targetNode);
    }
    childNodes.add(portfolioNode);
    copyRootNode.addChildNodes(childNodes);
    final SimplePortfolio copyPortfolio = new SimplePortfolio(mainPortfolio.getName());
    copyPortfolio.setAttributes(mainPortfolio.getAttributes());
    copyPortfolio.setUniqueId(mainPortfolio.getUniqueId());
    copyPortfolio.setRootNode(copyRootNode);
    _utils.savePortfolio(copyPortfolio);
  }

  /**
   * Delete a named portfolio and remove from super portfolio.
   * @param portfolioName  the portfolio name
   */
  public void deletePortfolio(final String portfolioName) {
    ArgumentChecker.notNull(portfolioName, "portfolioName");
    _utils.deletePortfolio(PortfolioKey.of(portfolioName));
    final Portfolio mainPortfolio = _utils.loadPortfolio(PortfolioKey.of(ALL_PORTFOLIOS_NAME));
    final PortfolioNode rootNode = mainPortfolio.getRootNode();
    // have to copy portfolio as the original child node structure is an unmodifiable list, so matching nodes cannot be removed
    final SimplePortfolioNode copyRootNode = new SimplePortfolioNode(rootNode.getName());
    copyRootNode.setParentNodeId(rootNode.getParentNodeId());
    copyRootNode.setUniqueId(rootNode.getUniqueId());
    final List<PortfolioNode> childNodes = new ArrayList<>(rootNode.getChildNodes());
    PortfolioNode targetNode = null;
    for (final PortfolioNode node : childNodes) {
      if (node.getName().equals(portfolioName)) {
        targetNode = node;
        break;
      }
    }
    if (targetNode != null) {
      childNodes.remove(targetNode);
    }
    copyRootNode.addChildNodes(childNodes);
    final SimplePortfolio copyPortfolio = new SimplePortfolio(mainPortfolio.getName());
    copyPortfolio.setAttributes(mainPortfolio.getAttributes());
    copyPortfolio.setUniqueId(mainPortfolio.getUniqueId());
    copyPortfolio.setRootNode(copyRootNode);
    _utils.savePortfolio(copyPortfolio);
  }
}
