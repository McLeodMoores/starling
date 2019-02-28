/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.portfolio;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebPortfoliosData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebPortfoliosDataTest extends AbstractBeanTestCase {
  private static final String NAME = "port";
  private static final String PORTFOLIO_URI = "portfolio";
  private static final String NODE_URI = "node";
  private static final String POSITION_URI = "position";
  private static final String VERSION_URI = "version=1";
  private static final ManageablePortfolio PORTFOLIO = new ManageablePortfolio(NAME);
  private static final ManageablePortfolioNode PARENT_NODE = new ManageablePortfolioNode("parent node");
  private static final ManageablePortfolioNode CHILD_NODE = new ManageablePortfolioNode("child node");
  private static final PortfolioDocument DOCUMENT = new PortfolioDocument();
  private static final PortfolioDocument VERSIONED = new PortfolioDocument();
  static {
    PORTFOLIO.setRootNode(PARENT_NODE);
    DOCUMENT.setPortfolio(PORTFOLIO);
    VERSIONED.setPortfolio(PORTFOLIO);
    VERSIONED.setVersionFromInstant(Instant.now());
  }
  private static final WebPortfoliosData DATA = new WebPortfoliosData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("prt", "0"));
    CHILD_NODE.setUniqueId(UniqueId.of("prt", "1"));
    DATA.setPortfolio(DOCUMENT);
    DATA.setNode(CHILD_NODE);
    DATA.setUriPortfolioId(PORTFOLIO_URI);
    DATA.setUriNodeId(NODE_URI);
    DATA.setUriPositionId(POSITION_URI);
    DATA.setUriVersionId(VERSION_URI);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebPortfoliosData.class,
        Arrays.asList("uriPortfolioId", "uriNodeId", "uriPositionId", "uriVersionId", "portfolio", "parentNode", "node", "versioned"),
        Arrays.asList(PORTFOLIO_URI, NODE_URI, POSITION_URI, VERSION_URI, DOCUMENT, PARENT_NODE, CHILD_NODE, VERSIONED),
        Arrays.asList(NODE_URI, POSITION_URI, VERSION_URI, PORTFOLIO_URI, VERSIONED, CHILD_NODE, PARENT_NODE, DOCUMENT));
  }

  /**
   * Tests getting the best portfolio if the override id is not null.
   */
  public void testBestPortfolioOverrideId() {
    final UniqueId uid = UniqueId.of("pos", "1");
    assertEquals(DATA.getBestPortfolioUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best portfolio if there is no portfolio document.
   */
  public void testBestPortfolioNoPortfolioDocument() {
    final WebPortfoliosData data = DATA.clone();
    data.setPortfolio(null);
    assertEquals(data.getBestPortfolioUriId(null), PORTFOLIO_URI);
  }

  /**
   * Tests getting the best portfolio from the document.
   */
  public void testBestPortfolioFromDocument() {
    assertEquals(DATA.getBestPortfolioUriId(null), DOCUMENT.getUniqueId().toString());
  }

  /**
   * Tests getting the best portfolio node if the override id is not null.
   */
  public void testBestPortfolioNodeOverrideId() {
    final UniqueId uid = UniqueId.of("pos", "1");
    assertEquals(DATA.getBestNodeUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best portfolio node if there is no node.
   */
  public void testBestNodeNoNodeDocument() {
    final WebPortfoliosData data = DATA.clone();
    data.setNode(null);
    assertEquals(data.getBestNodeUriId(null), NODE_URI);
  }

  /**
   * Tests getting the best portfolio node from the document.
   */
  public void testBestNodeFromDocument() {
    assertEquals(DATA.getBestNodeUriId(null), CHILD_NODE.getUniqueId().toString());
  }
}
