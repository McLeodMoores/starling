/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */package com.mcleodmoores.starling.client.portfolio;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;

import org.joda.beans.JodaBeanUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.starling.client.utils.TestUtils;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.portfolio.PortfolioWriter;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.InMemoryPortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.position.impl.MasterPositionSource;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link PortfolioManager}.
 */
@Test(groups = TestGroup.UNIT, singleThreaded = true)
public class PortfolioManagerTest {
  /** The tool context */
  private ToolContext _toolContext;

  /**
   * Sets up the tool context.
   */
  @BeforeTest
  public void setUp() {
    _toolContext = TestUtils.getToolContext();
  }

  /**
   * Creates a FX forward position.
   * @param correlationId  the correlation id used in the trade
   * @return  the position
   */
  private static Position makeTestPosition(final String correlationId) {
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

  /**
   * Creates a portfolio containing a single FX forward trade. The portfolio has a single child node containing the position.
   * @param name  the portfolio name
   * @param correlationId  the correlation id used in the trade
   * @return  the portfolio
   */
  private static Portfolio makeTestPortfolio(final String name, final String correlationId) {
    final SimplePortfolio portfolio = new SimplePortfolio(name);
    final SimplePortfolioNode node = new SimplePortfolioNode("Sub-node 1");
    portfolio.getRootNode().addChildNode(node);
    node.addPosition(makeTestPosition(correlationId));
    return portfolio;
  }

  /**
   * Tests that a portfolio is saved correctly and that all positions have been saved in the master.
   */
  @Test
  public void testSavePortfolio() {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    final Portfolio testPortfolio1 = makeTestPortfolio("Test1", "001");
    final PortfolioKey key1 = portfolioManager.savePortfolio(testPortfolio1);
    final PositionSource positionSource = _toolContext.getPositionSource();
    final Portfolio portfolio1 = positionSource.getPortfolio(key1.getUniqueId(), VersionCorrection.LATEST);
    final Portfolio resolvedPortfolio1 = PortfolioCompiler.resolvePortfolio(portfolio1, Executors.newSingleThreadExecutor(), _toolContext.getSecuritySource());
    Assert.assertEquals(testPortfolio1.getName(), resolvedPortfolio1.getName());
    Assert.assertEquals(testPortfolio1.getAttributes(), resolvedPortfolio1.getAttributes());
    Assert.assertEquals(testPortfolio1.getRootNode().getName(), resolvedPortfolio1.getRootNode().getName());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().size(), resolvedPortfolio1.getRootNode().getChildNodes().size());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getName(), resolvedPortfolio1.getRootNode().getChildNodes().get(0).getName());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().size(), 
        resolvedPortfolio1.getRootNode().getChildNodes().get(0).getPositions().size());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getQuantity(), 
        resolvedPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getQuantity());
    Assert.assertEquals(testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getAttributes(), 
        resolvedPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getAttributes());
    Assert.assertTrue(JodaBeanUtils.equalIgnoring((ManageableSecurity) testPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getSecurity(),
        (ManageableSecurity) resolvedPortfolio1.getRootNode().getChildNodes().get(0).getPositions().get(0).getSecurity(), 
          ManageableSecurity.meta().uniqueId()));
    portfolioManager.savePortfolio(makeTestPortfolio("Test2", "002"));
  }

  /**
   * Tests that the manager loads the correct portfolio using a key where the name is set but the unique id of the portfolio is unknown.
   * The latest version of the portfolio will be loaded because the version correction was not supplied.
   */
  @Test(dependsOnMethods = { "testSavePortfolio" })
  public void testLoadPortfolio() {
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

  /**
   * Tests that the manager loads the correct portfolio using a key when the name is set but the unique id of the portfolio is unknown.
   */
  @Test(dependsOnMethods = { "testLoadPortfolio" })
  public void testLoadPortfolio1() {
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

  /**
   * Tests that the manager can determine whether a portfolio is present in the master.
   */
  @Test(dependsOnMethods = { "testLoadPortfolio1" })
  public void testPortfolioExists() {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    Assert.assertTrue(portfolioManager.portfolioExists(PortfolioKey.of("Test1")));
    Assert.assertTrue(portfolioManager.portfolioExists(PortfolioKey.of("Test2")));
    Assert.assertFalse(portfolioManager.portfolioExists(PortfolioKey.of("Test3")));
    final Portfolio testPortfolio1 = portfolioManager.loadPortfolio(PortfolioKey.of("Test1"));
    Assert.assertTrue(portfolioManager.portfolioExists(PortfolioKey.of("Test1", testPortfolio1.getUniqueId())));
    Assert.assertFalse(portfolioManager.portfolioExists(PortfolioKey.of("Test3", testPortfolio1.getUniqueId())));
  }

  /**
   * Tests that the manager returns the full list of portfolios that are visible for the user.
   */
  @Test(dependsOnMethods = { "testPortfolioExists" })
  public void testGetPortfolioList() {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    final Set<PortfolioKey> portfolioList = portfolioManager.getPortfolioList();
    Assert.assertEquals(portfolioList.size(), 2);
    Assert.assertEqualsNoOrder(portfolioList.toArray(), new PortfolioKey[] { PortfolioKey.of("Test1"), PortfolioKey.of("Test2") });
  }

  /**
   * Tests that portfolios can be deleted.
   */
  @Test(dependsOnMethods = { "testGetPortfolioList" })
  public void testDeletePortfolio() {
    final PortfolioManager portfolioManager = new PortfolioManager(_toolContext);
    portfolioManager.deletePortfolio(PortfolioKey.of("Test1"));
    Assert.assertFalse(portfolioManager.portfolioExists(PortfolioKey.of("Test1")));
    Assert.assertEquals(portfolioManager.getPortfolioList().size(), 1);
    final PortfolioKey test2Key = portfolioManager.getPortfolioList().iterator().next();
    portfolioManager.deletePortfolio(test2Key);
    Assert.assertFalse(portfolioManager.portfolioExists(test2Key));
    Assert.assertEquals(portfolioManager.getPortfolioList().size(), 0);
  }

  /**
   * Tests the save behaviour when there is already a portfolio with the same name in the master.
   */
  @Test
  public void testSaveOrUpdatePortfolio() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    final Portfolio originalPortfolio = makeTestPortfolio("TestSaveOrUpdate1", "001");
    final Portfolio replacementPortfolio = makeTestPortfolio("TestSaveOrUpdate1", "001");
    portfolioManager.savePortfolio(originalPortfolio);
    portfolioManager.savePortfolio(replacementPortfolio);
    Assert.assertTrue(portfolioManager.portfolioExists(PortfolioKey.of(replacementPortfolio.getName())));
    // portfolios have different uids so there are two
    Assert.assertEquals(portfolioManager.getPortfolioList().size(), 1);
  }

  /**
   * Tests that a portfolio can only be seen by a user with authorization.
   */
  @Test
  public void testPortfolioAuthorization() {
    final Portfolio portfolio = makeTestPortfolio("TestAuthorization", "001");
    final PortfolioWriter writer = new PortfolioWriter(true, true, _toolContext.getPortfolioMaster(), _toolContext.getPositionMaster(), 
        _toolContext.getSecurityMaster());
    final UniqueId id = writer.write(portfolio);
    final PortfolioDocument document = _toolContext.getPortfolioMaster().get(id);
    document.setVisibility(DocumentVisibility.HIDDEN);
    _toolContext.getPortfolioMaster().update(document);
    final PortfolioManager manager = new PortfolioManager(_toolContext);
    Assert.assertFalse(manager.portfolioExists(PortfolioKey.of(portfolio.getName())));
  }

  /**
   * Tests that the portfolio list does not include hidden portfolios.
   */
  @Test
  public void testListExcludesHidden() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final Portfolio visiblePortfolio = makeTestPortfolio("TestExcludesHidden1", "001");
    final Portfolio hiddenPortfolio = makeTestPortfolio("TestExcludesHidden2", "002");
    final PortfolioManager portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    portfolioManager.savePortfolio(visiblePortfolio);
    final PortfolioKey hiddenKey = portfolioManager.savePortfolio(hiddenPortfolio);
    // before visibility is set to hidden
    Assert.assertEquals(portfolioManager.getPortfolioList().size(), 2);
    final PortfolioDocument document = portfolioMaster.get(hiddenKey.getUniqueId());
    document.setVisibility(DocumentVisibility.HIDDEN);
    portfolioMaster.update(document);
    Assert.assertEquals(portfolioManager.getPortfolioList().size(), 1);
  }

  /**
   * Tests that the correct portfolio is returned when loading with a portfolio key with a unique id.
   */
  @Test
  public void testLoadPortfolioWithUid() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final Portfolio testPortfolio = makeTestPortfolio("Test1", "001");
    final PortfolioManager portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    final PortfolioKey key = portfolioManager.savePortfolio(testPortfolio);
    final Portfolio savedPortfolio = portfolioManager.loadPortfolio(key);
    Assert.assertEquals(savedPortfolio.getName(), "Test1");
    Assert.assertEquals(savedPortfolio.getAttributes(), new HashMap<String, String>());
    Assert.assertEquals(savedPortfolio.getRootNode().getName(), "");
    Assert.assertEquals(savedPortfolio.getRootNode().getChildNodes().size(), 1);
    Assert.assertEquals(savedPortfolio.getRootNode().getChildNodes().get(0).getName(), "Sub-node 1");
    Assert.assertEquals(savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().size(), 1);
    Assert.assertEquals(savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().get(0).getQuantity(), BigDecimal.ONE);
    Assert.assertEquals(savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().get(0).getAttributes().size(), 1);
    Assert.assertTrue(savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().get(0).getSecurity() instanceof ManageableSecurity);
  }

  /**
   * Tests the behaviour when attempting to load a portfolio that has not been saved.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testLoadUnavailablePortfolio() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager manager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    manager.loadPortfolio(PortfolioKey.of("Test"));
  }

  /**
   * Tests that "*" cannot be used in portfolio names when deleting.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoWildcardInPortfolioName1() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager manager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    manager.deletePortfolio(PortfolioKey.of("Portfolio*"));
  }

  /**
   * Tests that "*" cannot be used in portfolio names when deleting.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoWildcardInPortfolioName2() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager manager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    manager.delete(PortfolioKey.of("Portfolio*"), EnumSet.of(PortfolioManager.DeleteScope.PORTFOLIO));
  }

  /**
   * Tests the behaviour when attempting to delete a portfolio that has not been saved.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testDeleteUnavailablePortfolio1() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager manager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    manager.deletePortfolio(PortfolioKey.of("Not saved"));
  }

  /**
   * Tests the behaviour when attempting to delete a portfolio that has not been saved.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testDeleteUnavailablePortfolio2() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager manager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    manager.delete(PortfolioKey.of("Not saved"), EnumSet.of(PortfolioManager.DeleteScope.PORTFOLIO));
  }

  /**
   * Tests the behaviour when attempting to delete a portfolio that has not been saved.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testDeleteUnavailablePortfolio3() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager manager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    manager.deletePortfolio(PortfolioKey.of("Not saved", UniqueId.of("TEST", "1")));
  }

  /**
   * Tests the behaviour when attempting to delete a portfolio that has not been saved.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testDeleteUnavailablePortfolio4() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager manager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    manager.delete(PortfolioKey.of("Not saved", UniqueId.of("TEST", "1")), EnumSet.of(PortfolioManager.DeleteScope.PORTFOLIO));
  }

  /**
   * Tests the delete behaviour when only the portfolio is deleted. The unique id of the portfolio is used in this test.
   */
  @Test
  public void testDeletePortfolioOnly1() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    final Portfolio portfolio = makeTestPortfolio("TestDelete1", "001");
    PortfolioCompiler.resolvePortfolio(portfolio, Executors.newSingleThreadExecutor(), _toolContext.getSecuritySource());
    final PortfolioKey portfolioKey = portfolioManager.savePortfolio(portfolio);
    final Portfolio savedPortfolio = portfolioManager.loadPortfolio(portfolioKey);
    final SimplePosition savedPosition = (SimplePosition) savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().get(0);
    final SimpleTrade savedTrade = (SimpleTrade) savedPosition.getTrades().iterator().next();
    final FXForwardSecurity savedSecurity = (FXForwardSecurity) savedTrade.getSecurity();
    portfolioManager.delete(portfolioKey, EnumSet.of(PortfolioManager.DeleteScope.PORTFOLIO));
    final SimplePosition positionFromSource = new SimplePosition(positionSource.getPosition(savedPosition.getUniqueId()));
    final SimpleTrade tradeFromSource = new SimpleTrade(positionFromSource.getTrades().iterator().next());
    final FXForwardSecurity securityFromSource = (FXForwardSecurity) securitySource.get(savedSecurity.getUniqueId());
    Assert.assertFalse(portfolioManager.portfolioExists(portfolioKey));
    Assert.assertEquals(positionFromSource.getAttributes(), savedPosition.getAttributes());
    Assert.assertEquals(positionFromSource.getQuantity(), savedPosition.getQuantity());
    Assert.assertEquals(tradeFromSource.getAttributes(), savedTrade.getAttributes());
    Assert.assertEquals(tradeFromSource.getCounterparty(), savedTrade.getCounterparty());
    Assert.assertEquals(tradeFromSource.getPremium(), savedTrade.getPremium());
    Assert.assertEquals(tradeFromSource.getPremiumCurrency(), savedTrade.getPremiumCurrency());
    Assert.assertEquals(tradeFromSource.getPremiumDate(), savedTrade.getPremiumDate());
    Assert.assertEquals(tradeFromSource.getPremiumTime(), savedTrade.getPremiumTime());
    Assert.assertEquals(tradeFromSource.getQuantity(), savedTrade.getQuantity());
    Assert.assertEquals(securityFromSource.getAttributes(), savedSecurity.getAttributes());
    Assert.assertEquals(securityFromSource.getName(), savedSecurity.getName());
    Assert.assertEquals(securityFromSource.getPayAmount(), savedSecurity.getPayAmount());
    Assert.assertEquals(securityFromSource.getPayCurrency(), savedSecurity.getPayCurrency());
    Assert.assertEquals(securityFromSource.getReceiveAmount(), savedSecurity.getReceiveAmount());
    Assert.assertEquals(securityFromSource.getReceiveCurrency(), savedSecurity.getReceiveCurrency());
    Assert.assertEquals(securityFromSource.getRegionId(), savedSecurity.getRegionId());
  }

  /**
   * Tests the delete behaviour when only the portfolio is deleted. The unique id of the portfolio is ignored.
   */
  @Test
  public void testDeletePortfolioOnly2() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    final Portfolio portfolio = makeTestPortfolio("TestDelete1", "001");
    final PortfolioKey portfolioKey = portfolioManager.savePortfolio(portfolio);
    final Portfolio savedPortfolio = portfolioManager.loadPortfolio(portfolioKey);
    final SimplePosition savedPosition = (SimplePosition) savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().get(0);
    final SimpleTrade savedTrade = (SimpleTrade) savedPosition.getTrades().iterator().next();
    final FXForwardSecurity savedSecurity = (FXForwardSecurity) savedTrade.getSecurity();
    portfolioManager.delete(PortfolioKey.of(portfolioKey.getName()), EnumSet.of(PortfolioManager.DeleteScope.PORTFOLIO));
    final SimplePosition positionFromSource = new SimplePosition(positionSource.getPosition(savedPosition.getUniqueId()));
    final SimpleTrade tradeFromSource = new SimpleTrade(positionFromSource.getTrades().iterator().next());
    final FXForwardSecurity securityFromSource = (FXForwardSecurity) securitySource.get(savedSecurity.getUniqueId());
    Assert.assertFalse(portfolioManager.portfolioExists(portfolioKey));
    Assert.assertEquals(positionFromSource.getAttributes(), savedPosition.getAttributes());
    Assert.assertEquals(positionFromSource.getQuantity(), savedPosition.getQuantity());
    Assert.assertEquals(tradeFromSource.getAttributes(), savedTrade.getAttributes());
    Assert.assertEquals(tradeFromSource.getCounterparty(), savedTrade.getCounterparty());
    Assert.assertEquals(tradeFromSource.getPremium(), savedTrade.getPremium());
    Assert.assertEquals(tradeFromSource.getPremiumCurrency(), savedTrade.getPremiumCurrency());
    Assert.assertEquals(tradeFromSource.getPremiumDate(), savedTrade.getPremiumDate());
    Assert.assertEquals(tradeFromSource.getPremiumTime(), savedTrade.getPremiumTime());
    Assert.assertEquals(tradeFromSource.getQuantity(), savedTrade.getQuantity());
    Assert.assertEquals(securityFromSource.getAttributes(), savedSecurity.getAttributes());
    Assert.assertEquals(securityFromSource.getName(), savedSecurity.getName());
    Assert.assertEquals(securityFromSource.getPayAmount(), savedSecurity.getPayAmount());
    Assert.assertEquals(securityFromSource.getPayCurrency(), savedSecurity.getPayCurrency());
    Assert.assertEquals(securityFromSource.getReceiveAmount(), savedSecurity.getReceiveAmount());
    Assert.assertEquals(securityFromSource.getReceiveCurrency(), savedSecurity.getReceiveCurrency());
    Assert.assertEquals(securityFromSource.getRegionId(), savedSecurity.getRegionId());
  }

  /**
   * Tests the delete behaviour when the portfolio and positions are deleted. The unique id of the portfolio is used in this test.
   */
  @Test
  public void testDeletePortfolioAndPositionsOnly1() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    final Portfolio portfolio = makeTestPortfolio("TestDelete2", "001");
    final PortfolioKey portfolioKey = portfolioManager.savePortfolio(portfolio);
    final Portfolio savedPortfolio = portfolioManager.loadPortfolio(portfolioKey);
    final SimplePosition savedPosition = (SimplePosition) savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().get(0);
    final SimpleTrade savedTrade = (SimpleTrade) savedPosition.getTrades().iterator().next();
    final FXForwardSecurity savedSecurity = (FXForwardSecurity) savedTrade.getSecurity();
    portfolioManager.delete(portfolioKey, EnumSet.of(PortfolioManager.DeleteScope.PORTFOLIO, PortfolioManager.DeleteScope.POSITION));
    try {
      positionSource.getPosition(savedPosition.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
    try {
      positionSource.getTrade(savedTrade.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
    final FXForwardSecurity securityFromSource = (FXForwardSecurity) securitySource.get(savedSecurity.getUniqueId());
    Assert.assertEquals(securityFromSource.getAttributes(), savedSecurity.getAttributes());
    Assert.assertEquals(securityFromSource.getName(), savedSecurity.getName());
    Assert.assertEquals(securityFromSource.getPayAmount(), savedSecurity.getPayAmount());
    Assert.assertEquals(securityFromSource.getPayCurrency(), savedSecurity.getPayCurrency());
    Assert.assertEquals(securityFromSource.getReceiveAmount(), savedSecurity.getReceiveAmount());
    Assert.assertEquals(securityFromSource.getReceiveCurrency(), savedSecurity.getReceiveCurrency());
    Assert.assertEquals(securityFromSource.getRegionId(), savedSecurity.getRegionId());
  }

  /**
   * Tests the delete behaviour when the portfolio and positions are deleted. The unique id of the portfolio is ignored.
   */
  @Test
  public void testDeletePortfolioAndPositionsOnly2() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    final Portfolio portfolio = makeTestPortfolio("TestDelete2", "001");
    final PortfolioKey portfolioKey = portfolioManager.savePortfolio(portfolio);
    final Portfolio savedPortfolio = portfolioManager.loadPortfolio(portfolioKey);
    final SimplePosition savedPosition = (SimplePosition) savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().get(0);
    final SimpleTrade savedTrade = (SimpleTrade) savedPosition.getTrades().iterator().next();
    final FXForwardSecurity savedSecurity = (FXForwardSecurity) savedTrade.getSecurity();
    portfolioManager.delete(PortfolioKey.of(portfolioKey.getName()), EnumSet.of(PortfolioManager.DeleteScope.PORTFOLIO, PortfolioManager.DeleteScope.POSITION));
    try {
      positionSource.getPosition(savedPosition.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
    try {
      positionSource.getTrade(savedTrade.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
    final FXForwardSecurity securityFromSource = (FXForwardSecurity) securitySource.get(savedSecurity.getUniqueId());
    Assert.assertEquals(securityFromSource.getAttributes(), savedSecurity.getAttributes());
    Assert.assertEquals(securityFromSource.getName(), savedSecurity.getName());
    Assert.assertEquals(securityFromSource.getPayAmount(), savedSecurity.getPayAmount());
    Assert.assertEquals(securityFromSource.getPayCurrency(), savedSecurity.getPayCurrency());
    Assert.assertEquals(securityFromSource.getReceiveAmount(), savedSecurity.getReceiveAmount());
    Assert.assertEquals(securityFromSource.getReceiveCurrency(), savedSecurity.getReceiveCurrency());
    Assert.assertEquals(securityFromSource.getRegionId(), savedSecurity.getRegionId());
  }

  /**
   * Tests the delete behaviour when the portfolio, positions and securities are deleted. The unique id of the portfolio is used in this test.
   */
  @Test
  public void testDeletePortfolioPositionsAndSecurities1() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    final Portfolio portfolio = makeTestPortfolio("TestDelete3", "001");
    final PortfolioKey portfolioKey = portfolioManager.savePortfolio(portfolio);
    final Portfolio savedPortfolio = portfolioManager.loadPortfolio(portfolioKey);
    final SimplePosition savedPosition = (SimplePosition) savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().get(0);
    final SimpleTrade savedTrade = (SimpleTrade) savedPosition.getTrades().iterator().next();
    final FXForwardSecurity savedSecurity = (FXForwardSecurity) savedTrade.getSecurity();
    portfolioManager.delete(portfolioKey, EnumSet.of(PortfolioManager.DeleteScope.PORTFOLIO, PortfolioManager.DeleteScope.POSITION, 
        PortfolioManager.DeleteScope.SECURITY));
    try {
      positionSource.getPosition(savedPosition.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
    try {
      positionSource.getTrade(savedTrade.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
    try {
      securitySource.get(savedSecurity.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
  }

  /**
   * Tests the delete behaviour when the portfolio, positions and securities are deleted. The unique id of the portfolio is not used.
   */
  @Test
  public void testDeletePortfolioPositionsAndSecurities2() {
    // creating in-test masters and sources to avoid collisions of portfolios and positions with _toolContext
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final PortfolioManager portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    final Portfolio portfolio = makeTestPortfolio("TestDelete3", "001");
    final PortfolioKey portfolioKey = portfolioManager.savePortfolio(portfolio);
    final Portfolio savedPortfolio = portfolioManager.loadPortfolio(portfolioKey);
    final SimplePosition savedPosition = (SimplePosition) savedPortfolio.getRootNode().getChildNodes().get(0).getPositions().get(0);
    final SimpleTrade savedTrade = (SimpleTrade) savedPosition.getTrades().iterator().next();
    final FXForwardSecurity savedSecurity = (FXForwardSecurity) savedTrade.getSecurity();
    portfolioManager.delete(PortfolioKey.of(portfolioKey.getName()), EnumSet.of(PortfolioManager.DeleteScope.PORTFOLIO, PortfolioManager.DeleteScope.POSITION, 
        PortfolioManager.DeleteScope.SECURITY));
    try {
      positionSource.getPosition(savedPosition.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
    try {
      positionSource.getTrade(savedTrade.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
    try {
      securitySource.get(savedSecurity.getUniqueId());
      Assert.fail();
    } catch (final DataNotFoundException e) {
      // expected
    }
  }

}