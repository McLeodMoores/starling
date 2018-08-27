/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.financial.livedata.rest.RemoteLiveDataInjector;
import com.opengamma.id.UniqueId;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Provides access to a remote {@link ViewProcess}.
 */
public class RemoteViewProcess implements ViewProcess {

  private final URI _baseUri;
  private final FudgeRestClient _client;

  public RemoteViewProcess(final URI baseUri) {
    this(baseUri, FudgeRestClient.create());
  }

  public RemoteViewProcess(final URI baseUri, final FudgeRestClient client) {
    _baseUri = baseUri;
    _client = client;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId getUniqueId() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessUris.PATH_UNIQUE_ID).build();
    return _client.accessFudge(uri).get(UniqueId.class);
  }

  @Override
  public UniqueId getDefinitionId() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessUris.PATH_DEFINITION_ID).build();
    return _client.accessFudge(uri).get(UniqueId.class);
  }

  @Override
  public ViewDefinition getLatestViewDefinition() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessUris.PATH_DEFINITION).build();
    return _client.accessFudge(uri).get(ViewDefinition.class);
  }

  @Override
  public ViewProcessState getState() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessUris.PATH_STATE).build();
    return _client.accessFudge(uri).get(ViewProcessState.class);
  }

  @Override
  public MarketDataInjector getLiveDataOverrideInjector() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewProcessUris.PATH_LIVE_DATA_OVERRIDE_INJECTOR).build();
    return new RemoteLiveDataInjector(uri, _client);
  }

  @Override
  public void shutdown() {
    _client.accessFudge(_baseUri).delete();
  }

}
