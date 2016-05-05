/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.utils;

import java.util.HashMap;
import java.util.Map;

import org.joda.beans.JodaBeanUtils;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Created by jim on 29/05/15.
 */
public class PortfolioCopier {

  /**
   * This method does an extreme deep copy - Securities are copied as well.
   * @param source
   * @return
   */
  public static SimplePortfolio copy(final SimplePortfolio source) {
    final SimplePortfolio destination = new SimplePortfolio(source.getName());
    if (source.getUniqueId() != null) {
      destination.setUniqueId(source.getUniqueId());
    }
    destination.setAttributes(copy(source.getAttributes()));
    destination.setRootNode(copy(source.getRootNode()));
    return destination;
  }

  private static Map<String, String> copy(final Map<String, String> attributes) {
    return new HashMap<>(attributes);
  }

  private static SimplePortfolioNode copy(final SimplePortfolioNode portfolioNode) {
    final SimplePortfolioNode copy = new SimplePortfolioNode(portfolioNode.getName());
    if (portfolioNode.getParentNodeId() != null) {
      copy.setParentNodeId(portfolioNode.getParentNodeId());
    }
    if (portfolioNode.getUniqueId() != null) {
      copy.setUniqueId(portfolioNode.getUniqueId());
    }
    for (final Position position : portfolioNode.getPositions()) {
      copy.addPosition(copy((SimplePosition) position));
    }
    for (final PortfolioNode childNode : portfolioNode.getChildNodes()) {
      copy.addChildNode(copy((SimplePortfolioNode) childNode));
    }
    return copy;
  }

  private static SimplePosition copy(final SimplePosition position) {
    final SimplePosition copy = new SimplePosition(position.getQuantity(), position.getSecurityLink().getExternalId());
    if (position.getUniqueId() != null) {
      copy.setUniqueId(position.getUniqueId());
    }
    copy.setSecurityLink(copy(position.getSecurityLink()));
    //TODO attributes not copied - why not?
    for (final com.opengamma.core.position.Trade trade : position.getTrades()) {
      copy.addTrade(copy((SimpleTrade) trade));
    }
    return copy;
  }

  private static SecurityLink copy(final SecurityLink securityLink) {
    return SimpleSecurityLink.of(copy((ManageableSecurity) securityLink.getTarget()));
  }

  private static Security copy(final ManageableSecurity security) {
    return JodaBeanUtils.clone(security);
  }

  private static SimpleTrade copy(final SimpleTrade trade) {
    final SimpleTrade copy = new SimpleTrade(copy(trade.getSecurityLink()), trade.getQuantity(), trade.getCounterparty(), trade.getTradeDate(), trade.getTradeTime());
    copy.setAttributes(copy(trade.getAttributes()));
    return copy;
  }
}
