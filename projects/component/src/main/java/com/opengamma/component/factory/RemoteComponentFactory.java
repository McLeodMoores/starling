/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentServer;
import com.opengamma.component.rest.RemoteComponentServer;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.RemoteConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.convention.impl.RemoteConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.exchange.impl.RemoteExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.RemoteHistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.RemoteHolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.legalentity.impl.RemoteLegalEntitySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.RemoteMarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.RemotePositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.RemoteRegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.RemoteSecuritySource;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.financial.currency.rest.RemoteCurrencyMatrixSource;
import com.opengamma.financial.function.rest.RemoteFunctionConfigurationSource;
import com.opengamma.financial.security.RemoteFinancialSecuritySource;
import com.opengamma.financial.view.rest.RemoteAvailableOutputsProvider;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.RemoteConfigMaster;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.RemoteConventionMaster;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.impl.RemoteExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.RemoteHolidayMaster;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.impl.RemoteLegalEntityMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.RemoteMarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.RemoteRegionMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.RemoteSecurityLoader;
import com.opengamma.masterdb.portfolio.RemoteDbPortfolioMaster;
import com.opengamma.masterdb.position.RemoteDbPositionMaster;
import com.opengamma.masterdb.security.RemoteDbSecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * Constructs components exposed by a remote component server.
 */
public class RemoteComponentFactory {

  /**
   * The base URI.
   */
  private final URI _baseUri;
  /**
   * The component server.
   */
  private final ComponentServer _componentServer;

  /**
   * Constructs an instance.
   *
   * @param componentServerUri
   *          the URI of the remote component server, not null
   */
  public RemoteComponentFactory(final String componentServerUri) {
    this(URI.create(componentServerUri));
  }

  /**
   * Constructs an instance.
   *
   * @param componentServerUri
   *          the URI of the remote component server, not null
   */
  public RemoteComponentFactory(final URI componentServerUri) {
    ArgumentChecker.notNull(componentServerUri, "componentServerUri");
    final RemoteComponentServer remoteComponentServer = new RemoteComponentServer(componentServerUri);
    _baseUri = componentServerUri;
    _componentServer = remoteComponentServer.getComponentServer();
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the base URI.
   *
   * @return the base URI, not null
   */
  public URI getBaseUri() {
    return _baseUri;
  }

  private ComponentInfo getTopLevelComponent(final List<String> preferenceList, final Class<?> type) {
    if (preferenceList != null) {
      for (final String preference : preferenceList) {
        try {
          final ComponentInfo componentInfo = getComponentServer().getComponentInfo(type, preference);
          if (componentInfo != null) {
            return componentInfo;
          }
        } catch (final IllegalArgumentException iae) {
          // do nothing and try the next one.
        }
      }
    }
    final List<ComponentInfo> componentInfos = getComponentServer().getComponentInfos();
    return componentInfos.size() == 0 ? null : componentInfos.get(0);
  }

  // -------------------------------------------------------------------------
  public RemoteViewProcessor getViewProcessor(final String vpId) {
    final ComponentInfo info = getComponentServer().getComponentInfo(ViewProcessor.class, "main");
    final URI uri = info.getUri();
    final JmsConnector jmsConnector = getJmsConnector(info);
    return new RemoteViewProcessor(uri, jmsConnector, Executors.newSingleThreadScheduledExecutor());
  }

  public List<RemoteViewProcessor> getViewProcessors() {
    final List<RemoteViewProcessor> result = new ArrayList<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(ViewProcessor.class)) {
      final URI uri = info.getUri();
      final JmsConnector jmsConnector = getJmsConnector(info);
      final RemoteViewProcessor vp = new RemoteViewProcessor(uri, jmsConnector, Executors.newSingleThreadScheduledExecutor());
      result.add(vp);
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Configs
  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public ConfigMaster getConfigMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, ConfigMaster.class).getUri();
    return new RemoteConfigMaster(uri);
  }

  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ConfigMaster getConfigMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(ConfigMaster.class, name).getUri();
    return new RemoteConfigMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ConfigMaster> getConfigMasters() {
    final Map<String, ConfigMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(ConfigMaster.class)) {
      result.put(info.getClassifier(), new RemoteConfigMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching master available
   */
  public ConfigSource getConfigSource(final List<String> preferredClassifiers) {
    final ComponentInfo componentInfo = getTopLevelComponent(preferredClassifiers, ConfigSource.class);
    return new RemoteConfigSource(componentInfo.getUri());
  }

  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ConfigSource getConfigSource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(ConfigSource.class, name).getUri();
    return new RemoteConfigSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ConfigSource> getConfigSources() {
    final Map<String, ConfigSource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(ConfigSource.class)) {
      result.put(info.getClassifier(), new RemoteConfigSource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Portfolios
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public PortfolioMaster getPortfolioMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(PortfolioMaster.class, name).getUri();
    return new RemoteDbPortfolioMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public PortfolioMaster getPortfolioMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, PortfolioMaster.class).getUri();
    return new RemoteDbPortfolioMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, PortfolioMaster> getPortfolioMasters() {
    final Map<String, PortfolioMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(PortfolioMaster.class)) {
      result.put(info.getClassifier(), new RemoteDbPortfolioMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Positions
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public PositionMaster getPositionMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(PositionMaster.class, name).getUri();
    return new RemoteDbPositionMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public PositionMaster getPositionMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, PositionMaster.class).getUri();
    return new RemoteDbPositionMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, PositionMaster> getPositionMasters() {
    final Map<String, PositionMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(PositionMaster.class)) {
      result.put(info.getClassifier(), new RemoteDbPositionMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public PositionSource getPositionSource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(PositionSource.class, name).getUri();
    return new RemotePositionSource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public PositionSource getPositionSource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, PositionSource.class).getUri();
    return new RemotePositionSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, PositionSource> getPositionSources() {
    final Map<String, PositionSource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(PositionSource.class)) {
      result.put(info.getClassifier(), new RemotePositionSource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Securities
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public SecuritySource getSecuritySource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(SecuritySource.class, name).getUri();
    return new RemoteFinancialSecuritySource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public SecuritySource getSecuritySource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, SecuritySource.class).getUri();
    return new RemoteSecuritySource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, SecuritySource> getSecuritySources() {
    final Map<String, SecuritySource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(SecuritySource.class)) {
      result.put(info.getClassifier(), new RemoteSecuritySource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public SecurityMaster getSecurityMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(SecurityMaster.class, name).getUri();
    return new RemoteDbSecurityMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public SecurityMaster getSecurityMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, SecurityMaster.class).getUri();
    return new RemoteDbSecurityMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, SecurityMaster> getSecurityMasters() {
    final Map<String, SecurityMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(SecurityMaster.class)) {
      result.put(info.getClassifier(), new RemoteDbSecurityMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Conventions
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ConventionSource getConventionSource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(ConventionSource.class, name).getUri();
    return new RemoteConventionSource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public ConventionSource getConventionSource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, ConventionSource.class).getUri();
    return new RemoteConventionSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ConventionSource> getConventionSources() {
    final Map<String, ConventionSource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(LegalEntitySource.class)) {
      result.put(info.getClassifier(), new RemoteConventionSource(info.getUri()));
    }
    return result;
  }
  // -------------------------------------------------------------------------

  /**
   * @param name
   *          the classifier name of the object you want to retrieve >>>>>>> master
   * @return the interface requested, or null if not present
   */
  public ConventionMaster getConventionMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(ConventionMaster.class, name).getUri();
    return new RemoteConventionMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public ConventionMaster getConventionMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, ConventionMaster.class).getUri();
    return new RemoteConventionMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ConventionMaster> getConventionMasters() {
    final Map<String, ConventionMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(ConventionMaster.class)) {
      result.put(info.getClassifier(), new RemoteConventionMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Organizations/Obligors
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public LegalEntitySource getLegalEntitySource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(LegalEntitySource.class, name).getUri();
    return new RemoteLegalEntitySource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public LegalEntitySource getLegalEntitySource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, LegalEntitySource.class).getUri();
    return new RemoteLegalEntitySource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, LegalEntitySource> getLegalEntitySources() {
    final Map<String, LegalEntitySource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(LegalEntitySource.class)) {
      result.put(info.getClassifier(), new RemoteLegalEntitySource(info.getUri()));
    }
    return result;
  }
  // -------------------------------------------------------------------------

  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public LegalEntityMaster getLegalEntityMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(LegalEntityMaster.class, name).getUri();
    return new RemoteLegalEntityMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public LegalEntityMaster getLegalEntityMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, LegalEntityMaster.class).getUri();
    return new RemoteLegalEntityMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, LegalEntityMaster> getLegalEntityMasters() {
    final Map<String, LegalEntityMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(LegalEntityMaster.class)) {
      result.put(info.getClassifier(), new RemoteLegalEntityMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public SecurityLoader getSecurityLoader(final String name) {
    final URI uri = getComponentServer().getComponentInfo(SecurityLoader.class, name).getUri();
    return new RemoteSecurityLoader(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public SecurityLoader getSecurityLoader(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, SecurityLoader.class).getUri();
    return new RemoteSecurityLoader(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, SecurityLoader> getSecurityLoaders() {
    final Map<String, SecurityLoader> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(SecurityLoader.class)) {
      result.put(info.getClassifier(), new RemoteSecurityLoader(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Market Data Snapshots
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(MarketDataSnapshotMaster.class, name).getUri();
    return new RemoteMarketDataSnapshotMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, MarketDataSnapshotMaster.class).getUri();
    return new RemoteMarketDataSnapshotMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, MarketDataSnapshotMaster> getMarketDataSnapshotMasters() {
    final Map<String, MarketDataSnapshotMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(MarketDataSnapshotMaster.class)) {
      result.put(info.getClassifier(), new RemoteMarketDataSnapshotMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public MarketDataSnapshotSource getMarketDataSnapshotSource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(MarketDataSnapshotSource.class, name).getUri();
    return new RemoteMarketDataSnapshotSource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public MarketDataSnapshotSource getMarketDataSnapshotSource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, MarketDataSnapshotSource.class).getUri();
    return new RemoteMarketDataSnapshotSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, MarketDataSnapshotSource> getMarketDataSnapshotSources() {
    final Map<String, MarketDataSnapshotSource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(MarketDataSnapshotSource.class)) {
      result.put(info.getClassifier(), new RemoteMarketDataSnapshotSource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Historical Time Series
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(HistoricalTimeSeriesSource.class, name).getUri();
    return new RemoteHistoricalTimeSeriesSource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, HistoricalTimeSeriesSource.class).getUri();
    return new RemoteHistoricalTimeSeriesSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HistoricalTimeSeriesSource> getHistoricalTimeSeriesSources() {
    final Map<String, HistoricalTimeSeriesSource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(HistoricalTimeSeriesSource.class)) {
      result.put(info.getClassifier(), new RemoteHistoricalTimeSeriesSource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(HistoricalTimeSeriesMaster.class, name).getUri();
    return new RemoteHistoricalTimeSeriesMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, HistoricalTimeSeriesMaster.class).getUri();
    return new RemoteHistoricalTimeSeriesMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HistoricalTimeSeriesMaster> getHistoricalTimeSeriesMasters() {
    final Map<String, HistoricalTimeSeriesMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(HistoricalTimeSeriesMaster.class)) {
      result.put(info.getClassifier(), new RemoteHistoricalTimeSeriesMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HistoricalTimeSeriesLoader getHistoricalTimeSeriesLoader(final String name) {
    final URI uri = getComponentServer().getComponentInfo(HistoricalTimeSeriesLoader.class, name).getUri();
    return new RemoteHistoricalTimeSeriesLoader(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HistoricalTimeSeriesLoader getHistoricalTimeSeriesLoader(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, HistoricalTimeSeriesLoader.class).getUri();
    return new RemoteHistoricalTimeSeriesLoader(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HistoricalTimeSeriesLoader> getHistoricalTimeSeriesLoaders() {
    final Map<String, HistoricalTimeSeriesLoader> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(HistoricalTimeSeriesLoader.class)) {
      result.put(info.getClassifier(), new RemoteHistoricalTimeSeriesLoader(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Currency Matrices
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public CurrencyMatrixSource getCurrencyMatrixSource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(CurrencyMatrixSource.class, name).getUri();
    return new RemoteCurrencyMatrixSource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public CurrencyMatrixSource getCurrencyMatrixSource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, CurrencyMatrixSource.class).getUri();
    return new RemoteCurrencyMatrixSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, CurrencyMatrixSource> getCurrencyMatrixSources() {
    final Map<String, CurrencyMatrixSource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(CurrencyMatrixSource.class)) {
      result.put(info.getClassifier(), new RemoteCurrencyMatrixSource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Function Configurations
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public FunctionConfigurationSource getFunctionConfigurationSource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(FunctionConfigurationSource.class, name).getUri();
    return new RemoteFunctionConfigurationSource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public FunctionConfigurationSource getFunctionConfigurationSource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, FunctionConfigurationSource.class).getUri();
    return new RemoteFunctionConfigurationSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, FunctionConfigurationSource> getFunctionConfigurationSources() {
    final Map<String, FunctionConfigurationSource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(FunctionConfigurationSource.class)) {
      result.put(info.getClassifier(), new RemoteFunctionConfigurationSource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Exchanges
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ExchangeSource getExchangeSource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(ExchangeSource.class, name).getUri();
    return new RemoteExchangeSource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public ExchangeSource getExchangeSource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, ExchangeSource.class).getUri();
    return new RemoteExchangeSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ExchangeSource> getExchangeSources() {
    final Map<String, ExchangeSource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(ExchangeSource.class)) {
      result.put(info.getClassifier(), new RemoteExchangeSource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ExchangeMaster getExchangeMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(ExchangeMaster.class, name).getUri();
    return new RemoteExchangeMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public ExchangeMaster getExchangeMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, ExchangeMaster.class).getUri();
    return new RemoteExchangeMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ExchangeMaster> getExchangeMasters() {
    final Map<String, ExchangeMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(ExchangeMaster.class)) {
      result.put(info.getClassifier(), new RemoteExchangeMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Regions
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public RegionSource getRegionSource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(RegionSource.class, name).getUri();
    return new RemoteRegionSource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public RegionSource getRegionSource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, RegionSource.class).getUri();
    return new RemoteRegionSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, RegionSource> getRegionSources() {
    final Map<String, RegionSource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(RegionSource.class)) {
      result.put(info.getClassifier(), new RemoteRegionSource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public RegionMaster getRegionMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(RegionMaster.class, name).getUri();
    return new RemoteRegionMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public RegionMaster getRegionMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, RegionMaster.class).getUri();
    return new RemoteRegionMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, RegionMaster> getRegionMasters() {
    final Map<String, RegionMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(RegionMaster.class)) {
      result.put(info.getClassifier(), new RemoteRegionMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  // Holidays
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HolidaySource getHolidaySource(final String name) {
    final URI uri = getComponentServer().getComponentInfo(HolidaySource.class, name).getUri();
    return new RemoteHolidaySource(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HolidaySource getHolidaySource(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, HolidaySource.class).getUri();
    return new RemoteHolidaySource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HolidaySource> getHolidaySources() {
    final Map<String, HolidaySource> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(HolidaySource.class)) {
      result.put(info.getClassifier(), new RemoteHolidaySource(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  /**
   * @param name
   *          the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HolidayMaster getHolidayMaster(final String name) {
    final URI uri = getComponentServer().getComponentInfo(HolidayMaster.class, name).getUri();
    return new RemoteHolidayMaster(uri);
  }

  /**
   * @param preferredClassifiers
   *          a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HolidayMaster getHolidayMaster(final List<String> preferredClassifiers) {
    final URI uri = getTopLevelComponent(preferredClassifiers, HolidayMaster.class).getUri();
    return new RemoteHolidayMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HolidayMaster> getHolidayMasters() {
    final Map<String, HolidayMaster> result = new LinkedHashMap<>();
    for (final ComponentInfo info : getComponentServer().getComponentInfos(HolidayMaster.class)) {
      result.put(info.getClassifier(), new RemoteHolidayMaster(info.getUri()));
    }
    return result;
  }

  // -------------------------------------------------------------------------
  public AvailableOutputsProvider getAvailableOutputs(final String name) {
    final URI uri = getComponentServer().getComponentInfo(AvailableOutputsProvider.class, name).getUri();
    return new RemoteAvailableOutputsProvider(uri);
  }

  // -------------------------------------------------------------------------
  private JmsConnector getJmsConnector(final URI activeMQBrokerUri) {
    final ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(activeMQBrokerUri);
    final JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName(getClass().getSimpleName());
    factory.setConnectionFactory(cf);
    factory.setClientBrokerUri(activeMQBrokerUri);
    factory.setTopicName(getClass().getSimpleName());
    return factory.getObjectCreating();
  }

  private JmsConnector getJmsConnector(final ComponentInfo info) {
    final URI jmsBrokerUri = URI.create(info.getAttribute("jmsBrokerUri"));
    final JmsConnector jmsConnector = getJmsConnector(jmsBrokerUri);
    return jmsConnector;
  }

  private ComponentServer getComponentServer() {
    return _componentServer;
  }

}
