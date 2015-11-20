package com.mcleodmoores.starling.client.portfolio;

import com.mcleodmoores.starling.client.utils.TestUtils;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import org.joda.beans.JodaBeanUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Created by jim on 10/06/15.
 */
@Test(singleThreaded = true)
public class PortfolioManagerTest {

  private ToolContext _toolContext;

  @BeforeTest
  public void setUp() {
    _toolContext = TestUtils.getToolContext();
  }

  private Position makeTestPosition(String correlationId) {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    builder.tradeDate(LocalDate.now());
    builder.payCurrency(Currency.AUD);
    builder.forwardDate(LocalDate.now().plusMonths(1));
    builder.receiveAmount(1000000d);
    builder.correlationId(ExternalId.of("A", correlationId));
    builder.counterparty("MyBroker");
    builder.payAmount(1100000d);
    builder.receiveCurrency(Currency.NZD);
    final FXForwardTrade fxForwardTrade = builder.build();
    return fxForwardTrade.toPosition();
  }

  private Portfolio makeTestPortfolio(String name, String correlationId) {
    SimplePortfolio portfolio = new SimplePortfolio(name);
    SimplePortfolioNode node = new SimplePortfolioNode("Sub-node 1");
    portfolio.getRootNode().addChildNode(node);
    node.addPosition(makeTestPosition(correlationId));
    return portfolio;
  }


  @Test
  public void testSavePortfolio() throws Exception {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    final Portfolio testPortfolio1 = makeTestPortfolio("Test1", "001");
    final PortfolioKey key1 = portfolioManager.savePortfolio(testPortfolio1);
    final PositionSource positionSource = _toolContext.getPositionSource();
    Portfolio portfolio1 = positionSource.getPortfolio(key1.getUniqueId(), VersionCorrection.LATEST);
    Portfolio resolvedPortfolio1 = PortfolioCompiler.resolvePortfolio(portfolio1, Executors.newSingleThreadExecutor(), _toolContext.getSecuritySource());
    Assert.assertEquals(testPortfolio1.getName(), resolvedPortfolio1.getName());
    Assert.assertEquals(testPortfolio1.getAttributes(), resolvedPortfolio1.getAttributes());
    Assert.assertEquals(testPortfolio1.getRootNode().getName(), resolvedPortfolio1.getRootNode().getName());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().size(), resolvedPortfolio1.getRootNode().getChildNodes().size());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getName(), resolvedPortfolio1.getRootNode().getChildNodes().get(0).getName());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().size(), resolvedPortfolio1.getRootNode().getChildNodes().get(0).getPositions().size());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getQuantity(), resolvedPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getQuantity());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getAttributes(), resolvedPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getAttributes());
    Assert.assertTrue(JodaBeanUtils.equalIgnoring((ManageableSecurity) testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getSecurity(),
        (ManageableSecurity) resolvedPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getSecurity(), ManageableSecurity.meta().uniqueId()));
    portfolioManager.savePortfolio(makeTestPortfolio("Test2", "002"));
  }

  @Test(dependsOnMethods = { "testSavePortfolio" })
  public void testLoadPortfolio() throws Exception {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    final Portfolio testPortfolio1 = portfolioManager.loadPortfolio(PortfolioKey.of("Test1"));
    Assert.assertEquals(testPortfolio1.getName(), "Test1");
    Assert.assertEquals(testPortfolio1.getAttributes(), new HashMap<String, String>());
    Assert.assertEquals(testPortfolio1.getRootNode().getName(), "");
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().size(), 1);
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getName(), "Sub-node 1");
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().size(), 1);
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getQuantity(), BigDecimal.ONE);
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getAttributes().size(), 1);
    Assert.assertTrue(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getSecurity() instanceof ManageableSecurity);
  }

  @Test(dependsOnMethods = { "testLoadPortfolio" })
  public void testLoadPortfolio1() throws Exception {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    final Portfolio testPortfolio1 = portfolioManager.loadPortfolio(PortfolioKey.of("Test1"), Instant.now());
    Assert.assertEquals(testPortfolio1.getName(), "Test1");
    Assert.assertEquals(testPortfolio1.getAttributes(), new HashMap<String, String>());
    Assert.assertEquals(testPortfolio1.getRootNode().getName(), "");
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().size(), 1);
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getName(), "Sub-node 1");
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().size(), 1);
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getQuantity(), BigDecimal.ONE);
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getAttributes().size(), 1);
    Assert.assertTrue(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getSecurity() instanceof ManageableSecurity);
  }


  @Test(dependsOnMethods = { "testLoadPortfolio1" })
  public void testPortfolioExists() throws Exception {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    Assert.assertTrue(portfolioManager.portfolioExists(PortfolioKey.of("Test1")));
    Assert.assertTrue(portfolioManager.portfolioExists(PortfolioKey.of("Test2")));
    Assert.assertFalse(portfolioManager.portfolioExists(PortfolioKey.of("Test3")));
    final Portfolio testPortfolio1 = portfolioManager.loadPortfolio(PortfolioKey.of("Test1"));
    Assert.assertTrue(portfolioManager.portfolioExists(PortfolioKey.of("Test1", testPortfolio1.getUniqueId())));
    Assert.assertFalse(portfolioManager.portfolioExists(PortfolioKey.of("Test3", testPortfolio1.getUniqueId())));
  }


  @Test(dependsOnMethods = { "testPortfolioExists" })
  public void testGetPortfolioList() throws Exception {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    final Set<PortfolioKey> portfolioList = portfolioManager.getPortfolioList();
    Assert.assertEquals(portfolioList.size(), 2);
    Assert.assertEqualsNoOrder(portfolioList.toArray(), new PortfolioKey[] { PortfolioKey.of("Test1"), PortfolioKey.of("Test2") });
  }


  @Test(dependsOnMethods = { "testGetPortfolioList" })
  public void testDeletePortfolio() throws Exception {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    portfolioManager.deletePortfolio(PortfolioKey.of("Test1"));
    Assert.assertFalse(portfolioManager.portfolioExists(PortfolioKey.of("Text1")));
    Assert.assertEquals(portfolioManager.getPortfolioList().size(), 1);
    final PortfolioKey test2Key = portfolioManager.getPortfolioList().iterator().next();
    portfolioManager.deletePortfolio(test2Key);
    Assert.assertFalse(portfolioManager.portfolioExists(test2Key));
    Assert.assertEquals(portfolioManager.getPortfolioList().size(), 0);
  }
}