/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

/**
 * Utility for constructing a random FRA portfolio.
 */
public class FRAPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected FRASecurityGenerator createFRASecurityGenerator() {
    final FRASecurityGenerator securities = new FRASecurityGenerator();
    configure(securities);
    return securities;
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final FRASecurityGenerator securities = createFRASecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("FRAs"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final FRASecurityGenerator securities = createFRASecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("FRA"), positions, portfolioSize);
  }

}
