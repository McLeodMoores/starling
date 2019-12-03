/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.Duration;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ComputationCacheResponse;
import com.opengamma.engine.view.cycle.ComputationCycleQuery;
import com.opengamma.engine.view.cycle.ComputationResultsResponse;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleState;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link ViewCycle}.
 */
public class RemoteViewCycle implements ViewCycle {

  private final URI _baseUri;
  private final FudgeRestClient _client;

  public RemoteViewCycle(final URI baseUri) {
    this(baseUri, FudgeRestClient.create());
  }

  public RemoteViewCycle(final URI baseUri, final FudgeRestClient client) {
    _baseUri = baseUri;
    _client = client;
  }

  @Override
  public String getName() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_NAME).build();
    return _client.accessFudge(uri).get(String.class);
  }

  @Override
  public UniqueId getUniqueId() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_UNIQUE_ID).build();
    return _client.accessFudge(uri).get(UniqueId.class);
  }

  @Override
  public UniqueId getViewProcessId() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_VIEW_PROCESS_ID).build();
    return _client.accessFudge(uri).get(UniqueId.class);
  }

  @Override
  public ViewCycleState getState() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_STATE).build();
    return _client.accessFudge(uri).get(ViewCycleState.class);
  }

  @Override
  public Duration getDuration() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_DURATION).build();
    return _client.accessFudge(uri).get(Duration.class);
  }

  @Override
  public ViewCycleExecutionOptions getExecutionOptions() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_EXECUTION_OPTIONS).build();
    return _client.accessFudge(uri).get(ViewCycleExecutionOptions.class);
  }

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinition() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_COMPILED_VIEW_DEFINITION).build();
    return new RemoteCompiledViewDefinitionWithGraphs(uri);
  }

  @Override
  public ViewComputationResultModel getResultModel() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_RESULT).build();
    return _client.accessFudge(uri).get(ViewComputationResultModel.class);
  }

  @Override
  public ComputationCacheResponse queryComputationCaches(final ComputationCycleQuery query) {
    ArgumentChecker.notNull(query, "query");
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_QUERY_CACHES).build();
    return _client.accessFudge(uri).post(ComputationCacheResponse.class, query);
  }

  @Override
  public ComputationResultsResponse queryResults(final ComputationCycleQuery query) {
    ArgumentChecker.notNull(query, "query");
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataViewCycleUris.PATH_QUERY_RESULTS).build();
    return _client.accessFudge(uri).post(ComputationResultsResponse.class, query);
  }

}
