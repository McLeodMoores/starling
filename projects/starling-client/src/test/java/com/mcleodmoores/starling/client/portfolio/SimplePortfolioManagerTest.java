/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.beans.JodaBeanUtils;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.portfolio.PortfolioWriter;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.portfolio.impl.InMemoryPortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.position.impl.MasterPositionSource;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link SimplePortfolioManager}.
 */
@Test(groups = TestGroup.UNIT)
public class SimplePortfolioManagerTest {
  /** A list of trades */
  private static final List<FXForwardTrade> TRADES = new ArrayList<>();

  static {
    TRADES.add(FXForwardTrade.builder().correlationId(ExternalId.of("Test", "1")).tradeDate(LocalDate.of(2016, 1, 1)).forwardDate(LocalDate.of(2016, 2, 1)).payCurrency(Currency.USD).payAmount(100000).receiveCurrency(Currency.JPY).receiveAmount(10000000).build());
    TRADES.add(FXForwardTrade.builder().correlationId(ExternalId.of("Test", "2")).tradeDate(LocalDate.of(2016, 1, 1)).forwardDate(LocalDate.of(2016, 3, 1)).payCurrency(Currency.JPY).payAmount(10000000).receiveCurrency(Currency.USD).receiveAmount(100000).build());
  }

  /**
   * Tests the behaviour when the tool context is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullToolContext() {
    new SimplePortfolioManager(null);
  }

  /**
   * Tests the behaviour when the portfolio name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPortfolioName1() {
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final ToolContext toolContext = new ToolContext();
    toolContext.setPortfolioMaster(portfolioMaster);
    toolContext.setPositionMaster(positionMaster);
    toolContext.setPositionSource(positionSource);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setSecuritySource(securitySource);
    final SimplePortfolioManager manager = new SimplePortfolioManager(toolContext);
    manager.updatePortfolio(null, TRADES);
  }

  /**
   * Tests the behaviour when the list of trades is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTrades() {
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final ToolContext toolContext = new ToolContext();
    toolContext.setPortfolioMaster(portfolioMaster);
    toolContext.setPositionMaster(positionMaster);
    toolContext.setPositionSource(positionSource);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setSecuritySource(securitySource);
    final SimplePortfolioManager manager = new SimplePortfolioManager(toolContext);
    manager.updatePortfolio("Test", null);
  }

  /**
   * Tests the behaviour when the portfolio name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPortfolioName2() {
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final ToolContext toolContext = new ToolContext();
    toolContext.setPortfolioMaster(portfolioMaster);
    toolContext.setPositionMaster(positionMaster);
    toolContext.setPositionSource(positionSource);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setSecuritySource(securitySource);
    final SimplePortfolioManager manager = new SimplePortfolioManager(toolContext);
    manager.deletePortfolio(null);
  }

  /**
   * Tests that the super portfolio is updated by adding a new child node and that the existing child nodes
   * are not changed.
   */
  @Test
  public void testUpdateExistingPortfolioWithNewName() {
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final ToolContext toolContext = new ToolContext();
    toolContext.setPortfolioMaster(portfolioMaster);
    toolContext.setPositionMaster(positionMaster);
    toolContext.setPositionSource(positionSource);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setSecuritySource(securitySource);
    final SimplePortfolio superPortfolio = new SimplePortfolio(SimplePortfolioManager.ALL_PORTFOLIOS_NAME);
    final SimplePortfolioNode superPortfolioNode = new SimplePortfolioNode("All");
    superPortfolio.setRootNode(superPortfolioNode);
    final Portfolio equityPortfolio = createEquityPortfolio();
    // add equity portfolio to super portfolio
    superPortfolioNode.addChildNode(equityPortfolio.getRootNode());
    final PortfolioWriter portfolioWriter = new PortfolioWriter(true, true, portfolioMaster, positionMaster, securityMaster);
    portfolioWriter.write(equityPortfolio);
    portfolioWriter.write(superPortfolio);
    final SimplePortfolioManager manager = new SimplePortfolioManager(toolContext);
    final String fxForwardPortfolioName = "FX Forward Portfolio";
    manager.updatePortfolio(fxForwardPortfolioName, TRADES);
    // FX forward portfolio should have been saved
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(fxForwardPortfolioName);
    PortfolioSearchResult result = portfolioMaster.search(request);
    assertEquals(result.getDocuments().size(), 1);
    final Portfolio savedFxForwardPortfolio = positionSource.getPortfolio(result.getFirstDocument().getPortfolio().getUniqueId(), VersionCorrection.LATEST);
    // all positions stored in root, so no child nodes
    assertEquals(savedFxForwardPortfolio.getRootNode().getChildNodes().size(), 0);
    final List<Position> fxForwardPositions = savedFxForwardPortfolio.getRootNode().getPositions();
    assertEquals(fxForwardPositions.size(), 2);
    // super portfolio should have been updated with a new child node
    request.setName(SimplePortfolioManager.ALL_PORTFOLIOS_NAME);
    result = portfolioMaster.search(request);
    assertEquals(result.getDocuments().size(), 1);
    final Portfolio resultPortfolio = positionSource.getPortfolio(result.getFirstDocument().getPortfolio().getUniqueId(), VersionCorrection.LATEST);
    // no positions in the root node
    assertTrue(resultPortfolio.getRootNode().getPositions().isEmpty());
    final List<PortfolioNode> childNodes = resultPortfolio.getRootNode().getChildNodes();
    assertEquals(childNodes.size(), 2);
    for (final PortfolioNode childNode : childNodes) {
      if (childNode.getName().equals(equityPortfolio.getName())) {
        assertEquals(childNode.getPositions().size(), 1);
      } else if (childNode.getName().equals(fxForwardPortfolioName)) {
        assertEquals(childNode.getPositions().size(), 2);
      } else {
        fail();
      }
    }
  }

  /**
   * Tests that the super portfolio is updated with a new portfolio when it is updated with a child node with the same name
   * as one of the existing nodes.
   */
  @Test
  public void testUpdateExistingPortfolioWithSameName() {
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final ToolContext toolContext = new ToolContext();
    toolContext.setPortfolioMaster(portfolioMaster);
    toolContext.setPositionMaster(positionMaster);
    toolContext.setPositionSource(positionSource);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setSecuritySource(securitySource);
    final SimplePortfolio superPortfolio = new SimplePortfolio(SimplePortfolioManager.ALL_PORTFOLIOS_NAME);
    final SimplePortfolioNode superPortfolioNode = new SimplePortfolioNode("All");
    superPortfolio.setRootNode(superPortfolioNode);
    final Portfolio equityPortfolio = createEquityPortfolio();
    final Portfolio originalFxForwardPortfolio = createFxForwardPortfolio();
    // add portfolios to super portfolio
    superPortfolioNode.addChildNode(equityPortfolio.getRootNode());
    superPortfolioNode.addChildNode(originalFxForwardPortfolio.getRootNode());
    final PortfolioWriter portfolioWriter = new PortfolioWriter(true, true, portfolioMaster, positionMaster, securityMaster);
    portfolioWriter.write(equityPortfolio);
    portfolioWriter.write(originalFxForwardPortfolio);
    portfolioWriter.write(superPortfolio);
    final SimplePortfolioManager manager = new SimplePortfolioManager(toolContext);
    manager.updatePortfolio(originalFxForwardPortfolio.getName(), Arrays.asList(TRADES.get(0)));
    // FX forward portfolio should have been overwritten
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(originalFxForwardPortfolio.getName());
    PortfolioSearchResult result = portfolioMaster.search(request);
    assertEquals(result.getDocuments().size(), 1);
    final Portfolio savedFxForwardPortfolio = positionSource.getPortfolio(result.getFirstDocument().getPortfolio().getUniqueId(), VersionCorrection.LATEST);
    // all positions stored in root, so no child nodes
    assertEquals(savedFxForwardPortfolio.getRootNode().getChildNodes().size(), 0);
    final List<Position> fxForwardPositions = savedFxForwardPortfolio.getRootNode().getPositions();
    assertEquals(fxForwardPositions.size(), 1);
    // super portfolio should have been updated with a new child node
    request.setName(SimplePortfolioManager.ALL_PORTFOLIOS_NAME);
    result = portfolioMaster.search(request);
    assertEquals(result.getDocuments().size(), 1);
    final Portfolio resultPortfolio = positionSource.getPortfolio(result.getFirstDocument().getPortfolio().getUniqueId(), VersionCorrection.LATEST);
    // no positions in the root node
    assertTrue(resultPortfolio.getRootNode().getPositions().isEmpty());
    final List<PortfolioNode> childNodes = resultPortfolio.getRootNode().getChildNodes();
    assertEquals(childNodes.size(), 2);
    for (final PortfolioNode childNode : childNodes) {
      if (childNode.getName().equals(equityPortfolio.getName())) {
        assertEquals(childNode.getPositions().size(), 1);
      } else if (childNode.getName().equals(originalFxForwardPortfolio.getName())) {
        assertEquals(childNode.getPositions().size(), 1);
      } else {
        fail();
      }
    }
  }

  /**
   * Tests that a list of FX forward trades is saved as a portfolio and that the super portfolio is updated.
   */
  @Test
  public void testUpdateEmptyPortfolio() {
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final ToolContext toolContext = new ToolContext();
    toolContext.setPortfolioMaster(portfolioMaster);
    toolContext.setPositionMaster(positionMaster);
    toolContext.setPositionSource(positionSource);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setSecuritySource(securitySource);
    // have to add this portfolio first - seems odd
    final SimplePortfolio superPortfolio = new SimplePortfolio(SimplePortfolioManager.ALL_PORTFOLIOS_NAME);
    final PortfolioWriter portfolioWriter = new PortfolioWriter(true, true, portfolioMaster, positionMaster, securityMaster);
    portfolioWriter.write(superPortfolio);
    final SimplePortfolioManager manager = new SimplePortfolioManager(toolContext);
    final String fxForwardPortfolioName = "FX Forward Portfolio";
    manager.updatePortfolio(fxForwardPortfolioName, TRADES);
    // FX forward portfolio should have been saved
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(fxForwardPortfolioName);
    PortfolioSearchResult result = portfolioMaster.search(request);
    assertEquals(result.getDocuments().size(), 1);
    final Portfolio savedFxForwardPortfolio = positionSource.getPortfolio(result.getFirstDocument().getPortfolio().getUniqueId(), VersionCorrection.LATEST);
    // all positions stored in root, so no child nodes
    assertEquals(savedFxForwardPortfolio.getRootNode().getChildNodes().size(), 0);
    final List<Position> fxForwardPositions = savedFxForwardPortfolio.getRootNode().getPositions();
    assertEquals(fxForwardPositions.size(), 2);
    // super portfolio should have been updated with a new child node
    request.setName(SimplePortfolioManager.ALL_PORTFOLIOS_NAME);
    result = portfolioMaster.search(request);
    assertEquals(result.getDocuments().size(), 1);
    final Portfolio resultPortfolio = positionSource.getPortfolio(result.getFirstDocument().getPortfolio().getUniqueId(), VersionCorrection.LATEST);
    // no positions in the root node
    assertTrue(resultPortfolio.getRootNode().getPositions().isEmpty());
    assertEquals(resultPortfolio.getRootNode().getChildNodes().size(), 1);
    // all positions stored in child node
    assertEquals(resultPortfolio.getRootNode().getChildNodes().get(0).getPositions().size(), 2);
  }

  /**
   * Tests that the named portfolio is deleted, and that the only positions deleted from the super portfolio were those in the deleted portfolio.
   */
  @Test
  public void testDeleteExistingPortfolio() {
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final ToolContext toolContext = new ToolContext();
    toolContext.setPortfolioMaster(portfolioMaster);
    toolContext.setPositionMaster(positionMaster);
    toolContext.setPositionSource(positionSource);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setSecuritySource(securitySource);
    final SimplePortfolio superPortfolio = new SimplePortfolio(SimplePortfolioManager.ALL_PORTFOLIOS_NAME);
    final SimplePortfolioNode superPortfolioNode = new SimplePortfolioNode("All");
    superPortfolio.setRootNode(superPortfolioNode);
    final Portfolio equityPortfolio = createEquityPortfolio();
    final Portfolio fxForwardPortfolio = createFxForwardPortfolio();
    // add child portfolios to super portfolio
    superPortfolioNode.addChildNode(fxForwardPortfolio.getRootNode());
    superPortfolioNode.addChildNode(equityPortfolio.getRootNode());
    final PortfolioWriter portfolioWriter = new PortfolioWriter(true, true, portfolioMaster, positionMaster, securityMaster);
    portfolioWriter.write(fxForwardPortfolio);
    portfolioWriter.write(equityPortfolio);
    portfolioWriter.write(superPortfolio);
    final SimplePortfolioManager manager = new SimplePortfolioManager(toolContext);
    manager.deletePortfolio(fxForwardPortfolio.getName());
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(fxForwardPortfolio.getName());
    // no FX forward portfolio after delete
    PortfolioSearchResult result = portfolioMaster.search(request);
    assertTrue(result.getDocuments().isEmpty());
    // super portfolio should contain only one equity position
    request.setName(SimplePortfolioManager.ALL_PORTFOLIOS_NAME);
    result = portfolioMaster.search(request);
    assertEquals(result.getDocuments().size(), 1);
    final Portfolio resultPortfolio = positionSource.getPortfolio(result.getFirstDocument().getPortfolio().getUniqueId(), VersionCorrection.LATEST);
    // no positions in the root node
    assertTrue(resultPortfolio.getRootNode().getPositions().isEmpty());
    final List<PortfolioNode> remainingChildNodes = resultPortfolio.getRootNode().getChildNodes();
    // only one child node containing equity position
    assertEquals(remainingChildNodes.size(), 1);
    final List<Position> remainingPositions = remainingChildNodes.get(0).getPositions();
    final EquitySecurity remainingSecurity = (EquitySecurity) remainingPositions.get(0).getSecurityLink().resolve(securitySource);
    // unique id is set when security is saved in the master, so ignore
    final EquitySecurity equitySecurity = (EquitySecurity) equityPortfolio.getRootNode().getPositions().get(0).getSecurity();
    JodaBeanUtils.equalIgnoring(equitySecurity, remainingSecurity, EquitySecurity.meta().uniqueId());
  }

  /**
   * Tests the behaviour when the master contains the portfolio to be deleted, but the super portfolio is empty.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testEmptySuperPortfolio() {
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final ToolContext toolContext = new ToolContext();
    toolContext.setPortfolioMaster(portfolioMaster);
    toolContext.setPositionMaster(positionMaster);
    toolContext.setPositionSource(positionSource);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setSecuritySource(securitySource);
    final Portfolio portfolio = createFxForwardPortfolio();
    final PortfolioWriter portfolioWriter = new PortfolioWriter(true, false, portfolioMaster, positionMaster, securityMaster);
    portfolioWriter.write(portfolio);
    final SimplePortfolioManager manager = new SimplePortfolioManager(toolContext);
    manager.deletePortfolio(portfolio.getName());
  }

  /**
   * Tests the behaviour when the portfolio does not exist and there are no positions in the super portfolio.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testDeleteEmptyMaster() {
    final PortfolioMaster portfolioMaster = new InMemoryPortfolioMaster();
    final PositionMaster positionMaster = new InMemoryPositionMaster();
    final PositionSource positionSource = new MasterPositionSource(portfolioMaster, positionMaster);
    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final SecuritySource securitySource = new MasterSecuritySource(securityMaster);
    final ToolContext toolContext = new ToolContext();
    toolContext.setPortfolioMaster(portfolioMaster);
    toolContext.setPositionMaster(positionMaster);
    toolContext.setPositionSource(positionSource);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setSecuritySource(securitySource);
    final SimplePortfolioManager manager = new SimplePortfolioManager(toolContext);
    manager.deletePortfolio("Test");
  }

  /**
   * Creates a portfolio containing a single equity.
   * @return  the portfolio
   */
  private static Portfolio createEquityPortfolio() {
    final SimplePortfolioNode equityPortfolioNode = new SimplePortfolioNode("Equity Portfolio");
    final EquitySecurity equitySecurity = new EquitySecurity("EXCH", "NAME", "CO", Currency.USD);
    equitySecurity.addExternalId(ExternalSchemes.syntheticSecurityId("Equity"));
    final SimplePosition equityPosition = new SimplePosition();
    equityPosition.addTrade(new SimpleTrade(equitySecurity, BigDecimal.ONE, new SimpleCounterparty(ExternalId.of("Test", "Test")), LocalDate.of(2016, 1, 1), OffsetTime.now()));
    equityPosition.setSecurityLink(SimpleSecurityLink.of(equitySecurity));
    equityPosition.setQuantity(BigDecimal.ONE);
    equityPortfolioNode.addPosition(equityPosition);
    final SimplePortfolio equityPortfolio = new SimplePortfolio("Equity Portfolio");
    equityPortfolio.setRootNode(equityPortfolioNode);
    return equityPortfolio;
  }

  /**
   * Creates an FX forward portfolio.
   * @return  the portfolio
   */
  private static Portfolio createFxForwardPortfolio() {
    final SimplePortfolioNode fxPortfolioNode = new SimplePortfolioNode("FX Forward Portfolio");
    for (final FXForwardTrade trade : TRADES) {
      fxPortfolioNode.addPosition(trade.toPosition());
    }
    final SimplePortfolio fxPortfolio = new SimplePortfolio("FX Forward Portfolio");
    fxPortfolio.setRootNode(fxPortfolioNode);
    return fxPortfolio;
  }
}
