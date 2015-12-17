package com.mcleodmoores.starling.client.stateless;

import org.joda.beans.JodaBeanUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.mcleodmoores.starling.client.portfolio.FXForwardTrade;
import com.mcleodmoores.starling.client.utils.SessionPortfolioTransformer;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Created by jim on 29/05/15.
 */
@Test(groups = TestGroup.UNIT)
public class SessionPortfolioTransformerTest {
  public void cloneTest() {
    final FXForwardTrade fxForwardTrade = FXForwardTrade.builder()
        .correlationId(ExternalId.of("MY_SCHEME", "001"))
        .counterparty("MyCounterparty")
        .forwardDate(LocalDate.now().plusMonths(3))
        .payCurrency(Currency.USD)
        .payAmount(1000000)
        .receiveCurrency(Currency.NZD)
        .receiveAmount(1600000)
        .tradeDate(LocalDate.now())
        .build();
    final SimplePortfolio portfolio = new SimplePortfolio("Test");
    final Position position = fxForwardTrade.toPosition();
    portfolio.getRootNode().addPosition(position);
    final SimplePortfolio portfolio2 = SessionPortfolioTransformer.buildSessionCopy(portfolio, ExternalScheme.of("MY_SCHEME"), "027");
    System.out.println(portfolio.toLongString());
    System.out.println("----------------");
    System.out.println(portfolio2.toLongString());
    Assert.assertNotEquals(portfolio, portfolio2);
    Assert.assertFalse(portfolio == portfolio2);
    Assert.assertFalse(portfolio.getRootNode() == portfolio2.getRootNode());
    Assert.assertNotEquals(portfolio.getRootNode(), portfolio2.getRootNode());
    final SimplePosition position1 = (SimplePosition) Iterables.getFirst(portfolio.getRootNode().getPositions(), null);
    Assert.assertNotNull(position1);
    final SimplePosition position2 = (SimplePosition) Iterables.getFirst(portfolio2.getRootNode().getPositions(), null);
    Assert.assertNotNull(position2);
    Assert.assertFalse(position1 == position2);
    Assert.assertNotEquals(position1, position2);
    Assert.assertTrue(JodaBeanUtils.equalIgnoring(position1, position2, SimplePosition.meta().securityLink(), SimplePosition.meta().trades(), SimplePosition.meta().attributes()));
    final ManageableSecurity security1 = (ManageableSecurity) position1.getSecurity();
    final ManageableSecurity security2 = (ManageableSecurity) position2.getSecurity();
    Assert.assertFalse(security1 == security2);
    Assert.assertNotEquals(security1, security2);
    Assert.assertTrue(JodaBeanUtils.equalIgnoring(security1, security2, ManageableSecurity.meta().externalIdBundle()));
    final SimpleTrade trade1 = (SimpleTrade) Iterables.getFirst(position1.getTrades(), null);
    Assert.assertNotNull(trade1);
    final SimpleTrade trade2 = (SimpleTrade) Iterables.getFirst(position2.getTrades(), null);
    Assert.assertNotNull(trade2);
    Assert.assertFalse(trade1 == trade2);
    Assert.assertNotEquals(trade1, trade2);
    Assert.assertTrue(JodaBeanUtils.equalIgnoring(trade1, trade2, SimpleTrade.meta().securityLink(), SimpleTrade.meta().attributes()));
  }
}
