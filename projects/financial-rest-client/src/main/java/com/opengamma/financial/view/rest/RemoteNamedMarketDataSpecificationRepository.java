/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.List;

import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link NamedMarketDataSpecificationRepository}.
 *
 * @deprecated  This is only required for the legacy analytics UI.
 */
@Deprecated
public class RemoteNamedMarketDataSpecificationRepository implements NamedMarketDataSpecificationRepository {

  private final URI _baseUri;
  private final FudgeRestClient _client;

  public RemoteNamedMarketDataSpecificationRepository(final URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getNames() {
    final URI uri = DataNamedMarketDataSpecificationRepositoryUris.uriNames(_baseUri);
    return _client.accessFudge(uri).get(List.class);
  }

  @Override
  public MarketDataSpecification getSpecification(final String name) {
    final URI uri = DataNamedMarketDataSpecificationRepositoryUris.uriSpecification(_baseUri, name);
    return _client.accessFudge(uri).get(MarketDataSpecification.class);
  }

}
