/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.target.logger.ResolutionLogger;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link PositionSource} based resolvers.
 */
@Test(groups = TestGroup.UNIT)
public class PositionSourceResolverTest {

  private static final SimplePortfolioNode NODE = new SimplePortfolioNode("Root");
  private static final SimplePosition POSITION = new SimplePosition();
  private static final SimpleTrade TRADE = new SimpleTrade();
  private static final SimplePortfolio PORTFOLIO = new SimplePortfolio("Test");
  private static final MockPositionSource POSITION_SOURCE = new MockPositionSource();
  private static final UniqueId BAD_ID = UniqueId.of("Missing", "0");

  public PositionSourceResolverTest() {
    POSITION.setQuantity(BigDecimal.ONE);
    POSITION.setSecurityLink(new SimpleSecurityLink(ExternalId.of("Security", "Foo")));
    TRADE.setSecurityLink(new SimpleSecurityLink(ExternalId.of("Security", "Foo")));
    TRADE.setQuantity(BigDecimal.ONE);
    POSITION.addTrade(TRADE);
    NODE.addPosition(POSITION);
    PORTFOLIO.setRootNode(NODE);
    PORTFOLIO.setUniqueId(UniqueId.of("Portfolio", "0"));
    POSITION_SOURCE.addPortfolio(PORTFOLIO);
  }

  private PositionSourceResolver resolver() {
    return new PositionSourceResolver(POSITION_SOURCE);
  }

  public void tradeObjectResolved() {
    assertEquals(resolver().trade().resolveObject(TRADE.getUniqueId(), VersionCorrection.LATEST), TRADE);
  }

  public void tradeObjectUnresolved() {
    assertEquals(resolver().trade().resolveObject(BAD_ID, VersionCorrection.LATEST), null);
  }

  public void tradeDeep() {
    assertNull(resolver().trade().deepResolver());
  }

  public void positionObjectResolved() {
    assertEquals(resolver().position().resolveObject(POSITION.getUniqueId(), VersionCorrection.LATEST), POSITION);
  }

  public void positionObjectUnresolved() {
    assertEquals(resolver().position().resolveObject(BAD_ID, VersionCorrection.LATEST), null);
  }

  public void positionIdentifierSingle() {
    assertEquals(resolver().position().resolveExternalId(ExternalIdBundle.EMPTY, VersionCorrection.LATEST), null);
  }

  public void positionIdentifierMultiple() {
    assertEquals(resolver().position().resolveExternalIds(Collections.singleton(ExternalIdBundle.EMPTY), VersionCorrection.LATEST), Collections.emptyMap());
  }

  public void positionUniqueidResolved() {
    assertEquals(resolver().position().resolveObjectId(POSITION.getUniqueId().getObjectId(), VersionCorrection.LATEST), POSITION.getUniqueId());
  }

  public void positionUniqueidUnresolved() {
    assertEquals(resolver().position().resolveObjectId(BAD_ID.getObjectId(), VersionCorrection.LATEST), null);
  }

  public void positionUniqueidMultiple() {
    final Set<ObjectId> request = new HashSet<>();
    request.add(POSITION.getUniqueId().getObjectId());
    request.add(BAD_ID.getObjectId());
    assertEquals(resolver().position().resolveObjectIds(request, VersionCorrection.LATEST),
        Collections.singletonMap(POSITION.getUniqueId().getObjectId(), POSITION.getUniqueId()));
  }

  public void positionDeep() {
    assertNull(resolver().position().deepResolver());
  }

  public void portfolioObjectResolved() {
    assertEquals(resolver().portfolio().resolveObject(PORTFOLIO.getUniqueId(), VersionCorrection.LATEST), PORTFOLIO);
  }

  public void portfolioObjectUnresolved() {
    assertEquals(resolver().portfolio().resolveObject(BAD_ID, VersionCorrection.LATEST), null);
  }

  public void portfolioIdentifierSingle() {
    assertEquals(resolver().portfolio().resolveExternalId(ExternalIdBundle.EMPTY, VersionCorrection.LATEST), null);
  }

  public void portfolioIdentifierMultiple() {
    assertEquals(resolver().portfolio().resolveExternalIds(Collections.singleton(ExternalIdBundle.EMPTY), VersionCorrection.LATEST), Collections.emptyMap());
  }

  public void portfolioUniqueidResolved() {
    assertEquals(resolver().portfolio().resolveObjectId(PORTFOLIO.getUniqueId().getObjectId(), VersionCorrection.LATEST), PORTFOLIO.getUniqueId());
  }

  public void portfolioUniqueidUnresolved() {
    assertEquals(resolver().portfolio().resolveObjectId(BAD_ID.getObjectId(), VersionCorrection.LATEST), null);
  }

  public void portfolioUniqueidMultiple() {
    final Set<ObjectId> request = new HashSet<>();
    request.add(PORTFOLIO.getUniqueId().getObjectId());
    request.add(BAD_ID.getObjectId());
    assertEquals(resolver().portfolio().resolveObjectIds(request, VersionCorrection.LATEST),
        Collections.singletonMap(PORTFOLIO.getUniqueId().getObjectId(), PORTFOLIO.getUniqueId()));
  }

  public void portfolioDeep() {
    final DeepResolver deep = resolver().portfolio().deepResolver();
    assertNotNull(deep);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final Portfolio portfolio = (Portfolio) deep.withLogger(PORTFOLIO, logger);
    assertNotNull(portfolio);
    assertNotSame(portfolio, PORTFOLIO);
    assertEquals(portfolio.getUniqueId(), PORTFOLIO.getUniqueId());
  }

  public void nodeObjectResolved() {
    assertEquals(resolver().portfolioNode().resolveObject(NODE.getUniqueId(), VersionCorrection.LATEST), NODE);
  }

  public void nodeObjectUnresolved() {
    assertEquals(resolver().portfolioNode().resolveObject(BAD_ID, VersionCorrection.LATEST), null);
  }

  public void nodeDeep() {
    final DeepResolver deep = resolver().portfolioNode().deepResolver();
    assertNotNull(deep);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final PortfolioNode node = (PortfolioNode) deep.withLogger(NODE, logger);
    assertNotNull(node);
    assertNotSame(node, NODE);
    assertEquals(node.getUniqueId(), NODE.getUniqueId());
  }

}
