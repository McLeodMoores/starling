/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;

/**
 * A simple implementation of {@code PortfolioNodeTraversalCallback} with no behavior.
 * Subclasses should override the particular methods they require.
 */
public class AbstractPortfolioNodeTraversalCallback implements PortfolioNodeTraversalCallback {

  @Override
  public void postOrderOperation(final PortfolioNode portfolioNode) {
  }

  @Override
  public void postOrderOperation(final PortfolioNode parentNode, final Position position) {
  }

  @Override
  public void preOrderOperation(final PortfolioNode portfolioNode) {
  }

  @Override
  public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
  }

}
