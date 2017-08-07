/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.id.ExternalScheme;

/**
 * Utility for constructing a random swap portfolio.
 */
public class SwapPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  private final boolean _useLastAvailableSwapRate;
  private final ExternalScheme _preferredScheme;

  public SwapPortfolioGeneratorTool() {
    this(false, null);
  }

  public SwapPortfolioGeneratorTool(final boolean useLastAvailableSwapRate, final ExternalScheme preferredScheme) {
    _useLastAvailableSwapRate = useLastAvailableSwapRate;
    _preferredScheme = preferredScheme;
  }

  protected SwapSecurityGenerator createSwapSecurityGenerator() {
    final SwapSecurityGenerator securities = new SwapSecurityGenerator();
    securities.useLastAvailableSwapRate(_useLastAvailableSwapRate);
    securities.setPreferredScheme(_preferredScheme);
    configure(securities);
    return securities;
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SwapSecurityGenerator securities = createSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaps"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SwapSecurityGenerator securities = createSwapSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaps"), positions, portfolioSize);
  }

}
