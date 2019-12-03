/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import static com.opengamma.lambdava.streams.Lambdava.merge;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link CompiledViewDefinition}.
 */
public class RemoteCompiledViewDefinitionWithGraphs implements CompiledViewDefinitionWithGraphs {

  private final URI _baseUri;
  private final FudgeRestClient _client;

  public RemoteCompiledViewDefinitionWithGraphs(final URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }

  private RemoteCompiledViewDefinitionWithGraphs(final URI baseUri, final FudgeRestClient client) {
    _baseUri = baseUri;
    _client = client;
  }

  @Override
  public VersionCorrection getResolverVersionCorrection() {
    throw new UnsupportedOperationException("TODO: Implement this method over REST");
  }

  @Override
  public String getCompilationIdentifier() {
    throw new UnsupportedOperationException("TODO: Implement this method over REST");
  }

  @Override
  public CompiledViewDefinitionWithGraphs withResolverVersionCorrection(final VersionCorrection versionCorrection) {
    return new RemoteCompiledViewDefinitionWithGraphs(_baseUri, _client) {
      @Override
      public VersionCorrection getResolverVersionCorrection() {
        return versionCorrection;
      }
    };
  }

  @Override
  public CompiledViewDefinitionWithGraphs withMarketDataManipulationSelections(final Map<String, DependencyGraph> newGraphsByConfig,
      final Map<String, Map<DistinctMarketDataSelector, Set<ValueSpecification>>> selectionsByConfig,
      final Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> paramsByConfig) {
    throw new UnsupportedOperationException("TODO: Implement this method over REST");
  }

  @Override
  public ViewDefinition getViewDefinition() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_VIEW_DEFINITION).build();
    return _client.accessFudge(uri).get(ViewDefinition.class);
  }

  @Override
  public Portfolio getPortfolio() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_PORTFOLIO).build();
    return _client.accessFudge(uri).get(Portfolio.class);
  }

  @Override
  public CompiledViewCalculationConfiguration getCompiledCalculationConfiguration(final String viewCalculationConfiguration) {
    final URI baseUri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_COMPILED_CALCULATION_CONFIGURATIONS).build();
    final URI uri = DataCompiledViewDefinitionUris.uriCompiledCalculationConfiguration(baseUri, viewCalculationConfiguration);
    return _client.accessFudge(uri).get(CompiledViewCalculationConfiguration.class);
  }

  @Override
  public Collection<CompiledViewCalculationConfiguration> getCompiledCalculationConfigurations() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_COMPILED_CALCULATION_CONFIGURATIONS).build();
    return _client.accessFudge(uri).get(List.class);
  }

  @Override
  public Map<String, CompiledViewCalculationConfiguration> getCompiledCalculationConfigurationsMap() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_COMPILED_CALCULATION_CONFIGURATIONS_MAP).build();
    return _client.accessFudge(uri).get(Map.class);
  }

  @Override
  public Set<ValueSpecification> getMarketDataRequirements() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_MARKET_DATA_REQUIREMENTS).build();
    return _client.accessFudge(uri).get(Set.class);
  }

  @Override
  public Set<ComputationTargetSpecification> getComputationTargets() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_COMPUTATION_TARGETS).build();
    return _client.accessFudge(uri).get(Set.class);
  }

  @Override
  public Instant getValidFrom() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_VALID_FROM).build();
    return _client.accessFudge(uri).get(Instant.class);
  }

  @Override
  public Instant getValidTo() {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_VALID_TO).build();
    return _client.accessFudge(uri).get(Instant.class);
  }

  @Override
  public Collection<DependencyGraphExplorer> getDependencyGraphExplorers() {
    final Collection<CompiledViewCalculationConfiguration> configurations = getCompiledCalculationConfigurations();
    final List<DependencyGraphExplorer> explorers = new ArrayList<>(configurations.size());
    for (final CompiledViewCalculationConfiguration configuration : configurations) {
      explorers.add(getDependencyGraphExplorer(configuration.getName()));
    }
    return explorers;
  }

  @Override
  public DependencyGraphExplorer getDependencyGraphExplorer(final String calcConfig) {
    final URI uri = UriBuilder.fromUri(_baseUri).path(DataCompiledViewDefinitionUris.PATH_GRAPHS).path(calcConfig).build();
    return new RemoteDependencyGraphExplorer(uri);
  }

  @Override
  public Map<ComputationTargetReference, UniqueId> getResolvedIdentifiers() {
    throw new UnsupportedOperationException("TODO: Implement this method over REST");
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalValuesRequirements() {
    final Map<ValueSpecification, Set<ValueRequirement>> terminalValuesRequirements = new HashMap<>();
    final Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations = getCompiledCalculationConfigurations();
    for (final CompiledViewCalculationConfiguration compiledCalculationConfiguration : compiledCalculationConfigurations) {
      merge(terminalValuesRequirements, compiledCalculationConfiguration.getTerminalOutputSpecifications());
    }
    return terminalValuesRequirements;
  }
}
