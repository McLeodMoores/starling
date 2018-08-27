/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import org.fudgemsg.FudgeContext;
import org.threeten.bp.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to an {@link AvailableOutputsProvider}.
 */
public class RemoteAvailableOutputsProvider extends AbstractRemoteClient implements AvailableOutputsProvider {

  /**
   * Creates an instance.
   *
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteAvailableOutputsProvider(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public FudgeContext getFudgeContext() {
    return OpenGammaFudgeContext.getInstance();
  }

  //-------------------------------------------------------------------------
  @Override
  public AvailableOutputs getPortfolioOutputs(final Portfolio portfolio, final Instant instant) {
    return getPortfolioOutputs(portfolio, instant, null, null);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(final Portfolio portfolio, final Instant instant, final Integer maxNodes, final Integer maxPositions) {
    ArgumentChecker.notNull(portfolio, "portfolio");

    final URI uri = DataAvailablePortfolioOutputsUris.uri(getBaseUri(), instant, maxNodes, maxPositions, null);
    return accessRemote(uri).post(AvailableOutputs.class, portfolio);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(final UniqueId portfolioId, final Instant instant) {
    return getPortfolioOutputs(portfolioId, instant, null, null);
  }

  @Override
  public AvailableOutputs getPortfolioOutputs(final UniqueId portfolioId, final Instant instant, final Integer maxNodes, final Integer maxPositions) {
    ArgumentChecker.notNull(portfolioId, "portfolioId");

    final URI uri = DataAvailablePortfolioOutputsUris.uri(getBaseUri(), instant, maxNodes, maxPositions, portfolioId);
    return accessRemote(uri).get(AvailableOutputs.class);
  }

}
