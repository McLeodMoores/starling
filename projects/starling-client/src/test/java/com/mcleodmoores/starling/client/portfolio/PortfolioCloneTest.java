/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the portfolio copier.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioCloneTest {
  
//  public void cloneTest() {
//    FXForwardTrade fxForwardTrade = FXForwardTrade.builder()
//        .correlationId(ExternalId.of("MY_SCHEME", "001"))
//        .counterparty("MyCounterparty")
//        .forwardDate(LocalDate.now().plusMonths(3))
//        .payCurrency(Currency.USD)
//        .payAmount(1000000)
//        .receiveCurrency(Currency.NZD)
//        .receiveAmount(1600000)
//        .tradeDate(LocalDate.now())
//        .build();
//    SimplePortfolio portfolio = new SimplePortfolio("Test");
//    Position position = fxForwardTrade.toPosition();
//    portfolio.getRootNode().addPosition(position);
//    SimplePortfolio portfolio2 = PortfolioCopier.copy(portfolio);
//    System.out.println(portfolio.toLongString());
//    System.out.println("----------------");
//    System.out.println(portfolio2.toLongString());
//    Assert.assertEquals(portfolio, portfolio2);
//    Assert.assertFalse(portfolio == portfolio2);
//    Assert.assertFalse(portfolio.getRootNode() == portfolio2.getRootNode());
//    Assert.assertEquals(portfolio.getRootNode(), portfolio2.getRootNode());
//    Position position1 = Iterables.getFirst(portfolio.getRootNode().getPositions(), null);
//    Assert.assertNotNull(position1);
//    Position position2 = Iterables.getFirst(portfolio2.getRootNode().getPositions(), null);
//    Assert.assertNotNull(position2);
//    Assert.assertFalse(position1 == position2);
//    Assert.assertEquals(position1, position2);
//    Security security1 = position1.getSecurity();
//    Security security2 = position2.getSecurity();
//    Assert.assertFalse(security1 == security2);
//    Assert.assertEquals(security1, security2);
//    com.opengamma.core.position.Trade trade1 = Iterables.getFirst(position1.getTrades(), null);
//    Assert.assertNotNull(trade1);
//    com.opengamma.core.position.Trade trade2 = Iterables.getFirst(position2.getTrades(), null);
//    Assert.assertNotNull(trade2);
//    Assert.assertFalse(trade1 == trade2);
//    Assert.assertEquals(trade1, trade2);
//  }
}
