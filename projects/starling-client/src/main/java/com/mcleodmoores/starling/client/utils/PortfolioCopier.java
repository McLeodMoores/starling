package com.mcleodmoores.starling.client.utils;

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
import org.joda.beans.JodaBeanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jim on 29/05/15.
 */
public class PortfolioCopier {

  /**
   * This method does an extreme deep copy - Securities are copied as well.
   * @param source
   * @return
   */
  public static SimplePortfolio copy(SimplePortfolio source) {
    SimplePortfolio destination = new SimplePortfolio(source.getName());
    if (source.getUniqueId() != null) {
      destination.setUniqueId(source.getUniqueId());
    }
    destination.setAttributes(copy(source.getAttributes()));
    destination.setRootNode(copy(source.getRootNode()));
    return destination;
  }

  private static Map<String, String> copy(Map<String, String> attributes) {
    return new HashMap<>(attributes);
  }

  private static SimplePortfolioNode copy(SimplePortfolioNode portfolioNode) {
    SimplePortfolioNode copy = new SimplePortfolioNode(portfolioNode.getName());
    if (portfolioNode.getParentNodeId() != null) {
      copy.setParentNodeId(portfolioNode.getParentNodeId());
    }
    if (portfolioNode.getUniqueId() != null) {
      copy.setUniqueId(portfolioNode.getUniqueId());
    }
    for (Position position : portfolioNode.getPositions()) {
      copy.addPosition(copy((SimplePosition) position));
    }
    for (PortfolioNode childNode : portfolioNode.getChildNodes()) {
      copy.addChildNode(copy((SimplePortfolioNode) childNode));
    }
    return copy;
  }

  private static SimplePosition copy(SimplePosition position) {
    SimplePosition copy = new SimplePosition(position.getQuantity(), position.getSecurityLink().getExternalId());
    if (position.getUniqueId() != null) {
      copy.setUniqueId(position.getUniqueId());
    }
    copy.setSecurityLink(copy(position.getSecurityLink()));
    for (com.opengamma.core.position.Trade trade : position.getTrades()) {
      copy.addTrade(copy((SimpleTrade) trade));
    }
    return copy;
  }

  private static SecurityLink copy(SecurityLink securityLink) {
    return SimpleSecurityLink.of(copy((ManageableSecurity) securityLink.getTarget()));
  }

  private static Security copy(ManageableSecurity security) {
    return JodaBeanUtils.clone(security);
  }

  private static SimpleTrade copy(SimpleTrade trade) {
    SimpleTrade copy = new SimpleTrade(copy(trade.getSecurityLink()),trade.getQuantity(), trade.getCounterparty(), trade.getTradeDate(), trade.getTradeTime());
    copy.setAttributes(copy(trade.getAttributes()));
    return copy;
  }
}
