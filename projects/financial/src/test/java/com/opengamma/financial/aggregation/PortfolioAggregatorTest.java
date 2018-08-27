/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioAggregatorTest {

  @Test(enabled = false)
  public void multipleInstancesOfSamePosition() {
    final SimplePortfolio portfolio = new SimplePortfolio(id("portfolio"), "portfolio");
    final SimplePortfolioNode root = new SimplePortfolioNode(id("root"), "root");
    final SimplePortfolioNode node1 = new SimplePortfolioNode(id("node1"), "node1");
    final SimplePortfolioNode node2 = new SimplePortfolioNode(id("node2"), "node2");
    final ExternalId securityId = ExternalId.of("sec", "123");
    final SimplePosition position = new SimplePosition(id("position"), BigDecimal.ONE, securityId);
    final SimpleCounterparty counterparty = new SimpleCounterparty(ExternalId.of("cpty", "123"));
    final SimpleSecurityLink securityLink = new SimpleSecurityLink(securityId);
    final Trade trade = new SimpleTrade(securityLink, BigDecimal.ONE, counterparty, LocalDate.now(), OffsetTime.now());
    position.addTrade(trade);
    portfolio.setRootNode(root);
    node1.addPosition(position);
    node2.addPosition(position);
    root.addChildNode(node1);
    root.addChildNode(node2);

    final CounterpartyAggregationFunction fn = new CounterpartyAggregationFunction();
    final Portfolio aggregate = new PortfolioAggregator(fn).aggregate(portfolio);
    final PortfolioNode aggregateRoot = aggregate.getRootNode();
    assertEquals(1, aggregateRoot.getChildNodes().size());
    final PortfolioNode node = aggregateRoot.getChildNodes().get(0);
    assertEquals(1, node.getPositions().size());
  }

  private static UniqueId id(final String value) {
    return UniqueId.of("scheme", value);
  }
}
