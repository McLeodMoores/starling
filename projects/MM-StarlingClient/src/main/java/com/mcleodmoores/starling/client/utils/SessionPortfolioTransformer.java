package com.mcleodmoores.starling.client.utils;

import com.mcleodmoores.starling.client.portfolio.Trade;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import org.joda.beans.JodaBeanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jim on 29/05/15.
 */
public class SessionPortfolioTransformer {
  private static final String CORRELATION_KEY = ManageableTrade.meta().providerId().name();

  public static SimplePortfolio buildSessionCopy(SimplePortfolio simplePortfolio, ExternalScheme scheme, String sessionPrefix) {
    return copy(simplePortfolio, scheme, sessionPrefix);
  }

  public static SimplePortfolio copy(SimplePortfolio source, ExternalScheme scheme, String prefix) {
    SimplePortfolio destination = new SimplePortfolio(prefix + source.getName());
    destination.setAttributes(copy(source.getAttributes()));
    destination.setRootNode(copy(source.getRootNode(), scheme, prefix));
    return destination;
  }

  private static Map<String, String> copy(Map<String, String> attributes) {
    return new HashMap<>(attributes);
  }

  private static Map<String, String> transform(Map<String, String> attributes, ExternalScheme scheme, String sessionPrefix) {
    Map<String, String> copy = new HashMap<>(attributes);
    if (copy.containsKey(CORRELATION_KEY)) {
      ExternalId id = ExternalId.parse(copy.get(CORRELATION_KEY));
      copy.put(CORRELATION_KEY, id.getScheme().getName() + "~" + sessionPrefix + id.getValue());
    }
    return copy;
  }


  private static SimplePortfolioNode copy(SimplePortfolioNode portfolioNode, ExternalScheme scheme, String sessionPrefix) {
    SimplePortfolioNode copy = new SimplePortfolioNode(portfolioNode.getName());
    for (Position position : portfolioNode.getPositions()) {
      copy.addPosition(copy((SimplePosition) position, scheme, sessionPrefix));
    }
    for (PortfolioNode childNode : portfolioNode.getChildNodes()) {
      copy.addChildNode(copy((SimplePortfolioNode) childNode, scheme, sessionPrefix));
    }
    return copy;
  }

  private static SimplePosition copy(SimplePosition position, ExternalScheme scheme, String sessionPrefix) {
    SimplePosition copy = new SimplePosition(position.getQuantity(), position.getSecurityLink().getExternalId());
    copy.setSecurityLink(copy(position.getSecurityLink(), scheme, sessionPrefix));
    copy.setAttributes(transform(position.getAttributes(), scheme, sessionPrefix));
    for (com.opengamma.core.position.Trade trade : position.getTrades()) {
      copy.addTrade(copy((SimpleTrade) trade, scheme, sessionPrefix));
    }
    return copy;
  }

  private static SecurityLink copy(SecurityLink securityLink, ExternalScheme scheme, String sessionPrefix) {
    return SimpleSecurityLink.of(copy((ManageableSecurity) securityLink.getTarget(), scheme, sessionPrefix));
  }

  private static Security copy(ManageableSecurity security, ExternalScheme scheme, String sessionPrefix) {
    ManageableSecurity copy = JodaBeanUtils.clone(security);
    final ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
    copy.setExternalIdBundle(transform(externalIdBundle, scheme, sessionPrefix));
    return copy;
  }

  private static SimpleTrade copy(SimpleTrade trade, ExternalScheme scheme, String sessionPrefix) {
    SimpleTrade copy = new SimpleTrade(copy(trade.getSecurityLink(), scheme, sessionPrefix),trade.getQuantity(), trade.getCounterparty(), trade.getTradeDate(), trade.getTradeTime());
    copy.setAttributes(transform(trade.getAttributes(), scheme, sessionPrefix));
    return copy;
  }

  private static ExternalIdBundle transform(ExternalIdBundle bundle, ExternalScheme scheme, String sessionPrefix) {
    ExternalId replacementId = ExternalId.of(scheme, sessionPrefix + bundle.getValue(scheme));
    return bundle.withoutScheme(scheme).withExternalId(replacementId);
  }
}
